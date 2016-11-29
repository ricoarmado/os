import Inode
import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.file.NotDirectoryException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class FileSystem {
    final String NEWLINE = System.getProperty("line.separator");
    //int NUM_OF_FREE_DIR = 97
    int CLUSTER_SIZE = 1024
    int LAST_CLUSTER_ID = -1
    int FREE_DIRECTORY_RECORD = -1
    SuperBlock superBlock
    File file
    Bitmap bitmap
    short userId
    short groupId
    DirectoryCluster _currentDir
    DirectoryCluster _root

    FileSystem(File file, short userId, short groupId) {//Создает экземпляр класса фс, выполняя инициализацию структур в памяти
        this.file = file
        this.userId = userId
        this.groupId = groupId
        Initialize()
    }
    def Initialize(){
        RandomAccessFile file = new RandomAccessFile(file,"rw")
        ByteArrayBuffer buffer = new ByteArrayBuffer()// чтение суперблока
        while (buffer.size() != 65500){
            buffer.write(file.readByte())
        }
        superBlock = new SuperBlock()
        superBlock.setBytes(buffer)
        buffer.reset()

        //чтение битовой карты
        file.seek(superBlock.bitmapOffset)
        while (buffer.size() != 32768){
            buffer.write(file.readByte())
        }
        bitmap = new Bitmap(32768)
        bitmap.setBytes(buffer.toByteArray())
        buffer.reset()
        //чтение корневого каталога
        _root = readDirectoryClusters(0,null,true,null)
        _currentDir = _root

    }
    def flushNewFile(String fileName, String fileExtension, int freeDirectoryRecordAddress, int freeInodeAddress, int freeInodeId, int freeDataClusterIndex, DirectoryCluster current){

        def record = new DirectoryCluster()
        record.filename = fileName.getBytes()
        record.inodeNumber = freeInodeId
        def inode = new Inode()
        inode.number = freeInodeId
        inode.userID = userId
        inode.groupID = groupId
        inode.chmod = new AccessRights(true, true, true, true, false, true, true, false, true).toInt16()
        inode.hiden = false
        inode.system = false
        inode.dir = false
        inode.readonly = false
        inode.createDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        inode.editDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        inode.setAddr(0, (short)freeDataClusterIndex)// устанавливает адрес кластера
        record.chmod = inode.chmod
        record.userID = inode.userID
        record.groupID = inode.groupID
        record.createDate = inode.createDate
        record.editDate = inode.editDate
        record.system = inode.system
        record.hiden = inode.hiden
        record.readonly = inode.readonly
        record.dir = inode.dir
        current.add(record)
        RandomAccessFile file = new RandomAccessFile(file,"rw")
        file.seek(freeDirectoryRecordAddress)
        file.write(record.getBytes())
        file.seek(freeInodeAddress)
        file.write(inode.getBytes())
        bitmap.setClusterState(freeInodeId, Bitmap.ClusterState.Used)
        file.seek(superBlock.bitmapOffset + freeInodeId)
        file.write(1)
        superBlock.markInodeAsBusy(freeInodeId)
    }
    def flushNewDirectory(String dirName, int freeDirectoryRecordAddress, int freeInodeAddress, int freeInodeId, int freeDataClusterIndex, DirectoryCluster current){
        def record = new DirectoryCluster()

        record.filename = dirName.getBytes()
        record.inodeNumber = freeInodeId
        def inode = new Inode()
        inode.number = freeInodeId
        inode.userID = userId
        inode.groupID = groupId
        inode.chmod = new AccessRights(true, true, true, true, false, true, true, false, true).toInt16()
        inode.hiden = false
        inode.system = false
        inode.dir = true
        inode.readonly = false
        inode.createDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        inode.editDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        inode.setAddr(0, (short)freeDataClusterIndex)
        record.chmod = inode.chmod
        record.userID = inode.userID
        record.groupID = inode.groupID
        record.createDate = inode.createDate
        record.editDate = inode.editDate
        record.system = inode.system
        record.hiden = inode.hiden
        record.readonly = inode.readonly
        record.dir = inode.dir
        current.add(record)
        RandomAccessFile file = new RandomAccessFile(file,"rw")
        file.seek(freeDirectoryRecordAddress)
        file.write(record.getBytes())
        file.seek(freeInodeAddress)
        file.write(inode.getBytes())
        bitmap.setClusterState(freeInodeId, Bitmap.ClusterState.Used)
        file.seek(superBlock.bitmapOffset + freeInodeId)
        file.write(1)
        superBlock.markInodeAsBusy(freeInodeId)

    }

    def Flush() {
        RandomAccessFile file = new RandomAccessFile(this.file, "rw")
        file.write(superBlock.getBytes())
        file.seek(superBlock.bitmapOffset)
        file.write(bitmap.getBytes())
        file.close()
    }

    def CreateDirectory(String path){
        String parentPath = new File(path).getParent() == "\\" ? "/" : new File(path).getParent()
        String newDirName =  path
        DirectoryCluster backup = _currentDir
        parentPath = parentPath.replace('\\','/')
        if(parentPath.length() > 0 && parentPath.getChars()[0].equals(new String("/").getChars()[0])){
            parentPath = new StringBuilder(parentPath).deleteCharAt(0).toString()
        }
        _currentDir = OpenDirectory("/")
        _currentDir = OpenDirectory(parentPath == "" ? "/" : parentPath) as DirectoryCluster
        //ищем свободный инод
        int addressInodes = superBlock.ilistOffset
        int freeDataClusterIndex = bitmap.findFirstFreeCluster(false)
        int numOfFreeInode = superBlock.findFreeInode()
        if(numOfFreeInode == -1)
            throw new Exception("Нет свободного инода")
        int freeAddr = addressInodes + 59*numOfFreeInode
        int freePathAdr = superBlock.rootOffset + 39 * numOfFreeInode

        flushNewDirectory(newDirName,freePathAdr,freeAddr,numOfFreeInode,freeDataClusterIndex,_currentDir)
        superBlock.clusterEmptyCount--
        superBlock.inodeEmptyList[numOfFreeInode] = -1

        _currentDir = backup
        Flush()

    }
    def CreateFile(String path){
        String parentPath = new File(path).getParent() == "\\" ? "/" : new File(path).getParent()
        String newDirName =  path
        DirectoryCluster backup = _currentDir
        parentPath = parentPath.replace('\\','/')
        if(parentPath.length() > 0 && parentPath.getChars()[0].equals(new String("/").getChars()[0])){
            parentPath = new StringBuilder(parentPath).deleteCharAt(0).toString()
        }
        _currentDir = OpenDirectory("/")
        _currentDir = OpenDirectory(parentPath == "" ? "/" : parentPath) as DirectoryCluster
        //ищем свободный инод
        int addressInodes = superBlock.ilistOffset
        int freeDataClusterIndex = bitmap.findFirstFreeCluster(false)
        int numOfFreeInode = superBlock.findFreeInode()
        if(numOfFreeInode == -1)
            throw new Exception("Нет свободного инода")
        int freeAddr = addressInodes + 59*numOfFreeInode
        int freePathAdr = superBlock.rootOffset + 39 * numOfFreeInode

        flushNewFile(path, getExtension(path),freePathAdr,freeAddr,numOfFreeInode,freeDataClusterIndex,_currentDir)
        superBlock.clusterEmptyCount--
        superBlock.inodeEmptyList[numOfFreeInode] = -1
        _currentDir = backup
        Flush()

    }
    DirectoryCluster readDirectoryClusters(int clusterIndex, String fullPath, boolean isRoot, DirectoryCluster dirToFill){
        DirectoryCluster result = dirToFill? dirToFill : new DirectoryCluster()
        if(!dirToFill)
            result.clear()
        if(isRoot){
            result.Path = "/"
            Inode inode = new Inode()
            inode.read(file, superBlock.ilistOffset)
            result.createDate = inode.createDate
            result.filename = "/".getChars()
            result.groupID = inode.groupID
            result.inodeNumber = inode.number
            result.editDate = inode.editDate
            result.size = inode.size
            result.streamAddress = superBlock.rootOffset
            result.userID = inode.userID
            result.dir = inode.dir
            result.hiden = result.hiden
            result.system = inode.system
            result.chmod = inode.chmod
            result.readonly = inode.readonly
            superBlock.markInodeAsBusy(0)
            bitmap.setClusterState(0,Bitmap.ClusterState.Used)
        }
        int address = superBlock.rootOffset
        int k = 0
        Queue<DirectoryCluster> directories = new ArrayDeque<>()
        RandomAccessFile file = new RandomAccessFile(this.file,"rw")
        file.seek(address)
        while (k < 2048){
            file.seek(address)
            ByteArrayBuffer arrayBuffer = new ByteArrayBuffer()
            while (arrayBuffer.size() != 39){
                arrayBuffer.write(file.readByte())
            }
            DirectoryCluster dir = new DirectoryCluster()
            dir.setBytes(arrayBuffer.getRawData())
            if(!dir.isEmpty())
                directories.add(dir)
            address = address + 39
            k = k+39
        }
        for(DirectoryCluster directoryCluster : directories){
            int inodenum = directoryCluster.inodeNumber
            Inode inode = new Inode()
            inode.read(this.file,superBlock.ilistOffset + 59 * inodenum)
            directoryCluster.chmod = inode.chmod
            directoryCluster.createDate = inode.createDate
            directoryCluster.editDate = inode.editDate
            directoryCluster.hiden = inode.hiden
            directoryCluster.system = inode.system
            directoryCluster.dir = inode.dir
            directoryCluster.readonly = inode.readonly
            directoryCluster.userID = inode.userID
            directoryCluster.groupID = inode.groupID
        }
        result = regenerateTree(result,directories,isRoot)
        result
    }

     DirectoryCluster regenerateTree(DirectoryCluster root,Queue<DirectoryCluster> queue, boolean isRoot){
         DirectoryCluster result =  root
         if(result == null)
             return new DirectoryCluster()

         DirectoryCluster tmp
         while ((tmp = queue.poll() as DirectoryCluster) != null){
             result.addMany(tmp,0)
         }

         result
    }
    DirectoryCluster OpenDirectory(String path) {
        if(path == "/"){
            def clusters = readDirectoryClusters(0, null, true, null)
            return clusters
        }
        DirectoryCluster _dir = _root.lookupDir(path,0)

        _dir
    }
    def readAsDir(){
        _root = readDirectoryClusters(0,null,true,null)
    }
    static DirectoryCluster ReadDirectory(DirectoryCluster currentDirectory, String dirName){
        currentDirectory.find(dirName)
    }
    byte[] readFile(String path) {

        DirectoryCluster backup = _currentDir
        def file = OpenDirectory(path)

        if(file == null)
            return null
        if(!Utils.getAccess(userId,groupId,_currentDir.userID,_currentDir.groupID,new AccessRights((short)_currentDir.chmod)).canExecute || !Utils.getAccess(userId,groupId,file.userID,file.groupID,new AccessRights((short)file.chmod)).canRead){
            throw new Exception("У вас нет прав доступа")
        }
        int sizeOfInode = 59
        Inode inode = new Inode()
        inode.read(this.file,superBlock.ilistOffset + file.inodeNumber * sizeOfInode)
        List<Short>addr = new ArrayList<>()

        for (int i = 0; i < 12; i++) {
            short pos = (short)(inode.di_addr[i][0] << 8 | inode.di_addr[i][1] & 0xFF)
            if(pos != 0){
               addr.add(pos)
            }
        }
        ByteArrayBuffer data = new ByteArrayBuffer()
        RandomAccessFile f = new RandomAccessFile(this.file,"rw")
        for (int i = 0; i < addr.size(); i++) {
            int get = addr.get(i)
            f.seek(superBlock.rootOffset + 2048 + get*CLUSTER_SIZE)
            ByteArrayBuffer tmp = new ByteArrayBuffer()
            while (tmp.size() != 1024){
                tmp.write(f.readByte())
            }
            def split = new String(tmp.getRawData()).split(NEWLINE)
            for (String str : split)
            data.write(str.getBytes())
        }

        _currentDir = backup
        data.getRawData()
    }
    int writeFile(String path, byte[] data){

        //Каждый раз начинаем поиск метафайла с корня
        DirectoryCluster backup = _currentDir
        _currentDir = _root as DirectoryCluster
        DirectoryCluster file = OpenDirectory(path)
        if(file == null)
            throw new Exception("Файл не найден")
        if(!Utils.getAccess(userId,groupId,_currentDir.userID,_currentDir.groupID,new AccessRights((short)_currentDir.chmod)).canExecute || !Utils.getAccess(userId,groupId,file.userID,file.groupID,new AccessRights((short)file.chmod)).canWrite){
            throw new Exception("У вас нет прав доступа")
        }
        if(data == null || data.length == 0)
            throw new Exception("Данные не могут быть пустыми")
        int sizeOfInode = 59

        Inode inode = new Inode()
        inode.read(this.file,superBlock.ilistOffset + file.inodeNumber * sizeOfInode)

        //ищем свободные блоки данных
        int numNeedDataClusters = data.length / CLUSTER_SIZE + 1
        int numberFreeDataClusters = superBlock.clusterEmptyCount

        if(numberFreeDataClusters < numNeedDataClusters && numNeedDataClusters > 11)
            throw new Exception("Недостаточно свободного места")

        int[] freeDataClustersIndexes = new int[numNeedDataClusters]
        int nodeVal = 0//число уже занятых кластеров
        for (int i = 0; i < 12; i++) {
            nodeVal = Utils.bytesToShort(inode.di_addr[i])
            if(nodeVal == 0){
                nodeVal = i
                break
            }
        }
        for(int i = 0; i < nodeVal; i++){
            freeDataClustersIndexes[i] = Utils.bytesToShort(inode.di_addr[i])
        } // считываем уже занятые кластеры


        int newAllocatedCluster = 0
        ArrayList<Short>addr = new ArrayList<>()
        for (int i = 0; i < numNeedDataClusters; i++){
            if(freeDataClustersIndexes[i] == 0){
                int freeDataClusterIndex = bitmap.findFirstFreeCluster(true)
                if(freeDataClusterIndex < 0 )
                    throw new Exception("Недостаточно места на жестком диске")
                freeDataClustersIndexes[i] = freeDataClusterIndex
                newAllocatedCluster++
            }
            short sh = (short)freeDataClustersIndexes[i]
            addr.add(sh)
        }
        superBlock.clusterEmptyCount -=newAllocatedCluster

        byte[][] newData = new byte[addr.size()]
        for (int i = 0; i < newData.length; i+= 1024) {
            if(data.length - newData.length < 1024){
                byte [] tmp = data[i..(data.length - newData.length)]
                newData[i] = tmp
            }
            else {
                byte [] tmp = data[i..1024]
                newData[i] = tmp
            }
        }

        int numberWrittenBytes = 0
        RandomAccessFile f = new RandomAccessFile(this.file,"rw")
        for (int i = 0; i < addr.size(); i++) {
            def get = addr.get(i);
            def bytes = Utils.shortToBytes(get)
            inode.di_addr[i][0] = bytes[0]
            inode.di_addr[i][1] = bytes[1]
            f.seek(superBlock.rootOffset + 2048 + get*CLUSTER_SIZE)
            f.write(newData[i])
            numberWrittenBytes += newData[i].length
            f.write(NEWLINE.getBytes())
        }
        inode.editDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        inode.size += numberWrittenBytes
        inode.write(this.file,superBlock.ilistOffset + file.inodeNumber * sizeOfInode)
        _currentDir = backup
        Flush()
        numberWrittenBytes
    }

    def setAttributes(DirectoryCluster cluster, PropertyMap map){
        DirectoryCluster old = cluster
        cluster.chmod = new AccessRights(map.ur,map.uw,map.ux,map.gr,map.gw,map.gx,map.or,map.ow,map.ox).toInt16()
        cluster.hiden = map.hidden
        cluster.system = map.system
        cluster.readonly = map.readonly
        cluster.editDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        int numOfInode = cluster.inodeNumber
        Inode inode = new Inode()
        inode.read(this.file,superBlock.ilistOffset + 59*numOfInode)
        inode.chmod = cluster.chmod
        inode.hiden = cluster.hiden
        inode.system = inode.system
        inode.readonly = cluster.readonly
        inode.editDate = cluster.editDate
        inode.write(this.file,superBlock.ilistOffset + 59*numOfInode)
        _root.replace(old,cluster)
    }
    static String getExtension(String fileName) {
        char ch;
        int len;
        if(fileName==null ||
                (len = fileName.length())==0 || (ch = fileName.charAt(len-1))=='/' || ch=='\\' || //in the case of a directory
                ch=='.' ) //in the case of . or ..
            return "";
        int dotInd = fileName.lastIndexOf('.'),
            sepInd = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if( dotInd<=sepInd )
            return "";
        else
            return fileName.substring(dotInd+1).toLowerCase();
    }

    def rename(DirectoryCluster oldFile, String newPath) {
        def directory = OpenDirectory("/")
        directory.rename(oldFile,newPath,superBlock.rootOffset  + 39*oldFile.inodeNumber, this.file)
    }
    def deleteFile(DirectoryCluster file){
        int sizeOfInode = 59
        int inodenum = file.inodeNumber
        Inode inode = new Inode()
        inode.read(this.file,superBlock.ilistOffset + file.inodeNumber * sizeOfInode)
        List<Short>addr = new ArrayList<>()

        for (int i = 0; i < 12; i++) {
            short pos = (short)(inode.di_addr[i][0] << 8 | inode.di_addr[i][1] & 0xFF)
            if(pos != 0){
                addr.add(pos)
            }
        }
        file.setFilename(Character.MIN_VALUE)
        file.write(this.file,superBlock.rootOffset + inodenum * 39)
        inode.write(this.file,superBlock.ilistOffset + file.inodeNumber * sizeOfInode)

        superBlock.clusterEmptyCount += addr.size()
        superBlock.inodeEmptyList[inodenum] = 0
        bitmap.setClusterState(inodenum,Bitmap.ClusterState.Free)
        Flush()
    }
}
