

import Directory
import Inode
import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.file.NotDirectoryException
/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class FileSystem {
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
        file.seek(65501+58*32729)
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
    def flushNewFile(String fileName, String fileExtension, int freeDirectoryRecordAddress, int freeInodeAddress, int freeInodeId, int freeDataClusterIndex){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); // same for minutes and seconds

        def record = new Directory()
        record.filename = fileName.getBytes()
        record.extension = fileExtension.getBytes()
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
        inode.createDate = today.toString()
        inode.editDate = today.toString()
        inode.setAddr(0, (short)freeDataClusterIndex)
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
    def flushNewDirectory(String dirName, int freeDirectoryRecordAddress, int freeInodeAddress, int freeInodeId, int freeDataClusterIndex){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); // same for minutes and seconds
        def record = new Directory()

        record.filename = dirName.getBytes()
        record.extension = ""
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
        inode.createDate = today.toString()
        inode.editDate = today.toString()
        inode.setAddr(0, (short)freeDataClusterIndex)
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
    def CreateDirectory(String path){
        String parentPath = _currentDir.filename
        String newDirName =  path
        Directory backup = _currentDir
        _currentDir = OpenDirectory(parentPath) as DirectoryCluster
        //ищем свободный инод
        int addressInodes = superBlock.ilistOffset

        RandomAccessFile file = new RandomAccessFile(file,"rw")
        file.seek(addressInodes)
        Inode freeInode = new Inode()
        int freeDataClusterIndex = bitmap.findFirstFreeCluster()
        int numOfFreeInode = superBlock.findFreeInode()
        if(numOfFreeInode == -1)
            throw new Exception("Нет свободного инода")
        file.seek(addressInodes + 58*numOfFreeInode)
        int freeAddr = addressInodes + 58*numOfFreeInode
        int freePathAdr = superBlock.rootOffset + 5* numOfFreeInode
        ByteArrayBuffer buffer = new ByteArrayBuffer()
        while (buffer.size() != 58){
            file.read(buffer)
        }
        freeInode.setBytes(buffer.toByteArray())

        flushNewDirectory(newDirName,freePathAdr,freeAddr,numOfFreeInode,freeDataClusterIndex)
        superBlock.clusterEmptyCount--
        superBlock.inodeEmptyList[numOfFreeInode] = -1
        _currentDir = backup

    }
    def CreateFile(String path){
        String fullpath = _currentDir.filename + path
        String parentPath = _currentDir.filename
        String newDirName =  path
        Directory backup = _currentDir
        _currentDir = OpenDirectory(parentPath) as DirectoryCluster
        //ищем свободный инод
        int addressFreeInode = -1
        int addressInodes = superBlock.ilistOffset
        int inodesCount = superBlock.clusterCount
        int numOfFreeInode = superBlock.findFreeInode()
        if(numOfFreeInode == -1)
            throw new Exception("Нет свободного инода")
        RandomAccessFile file = new RandomAccessFile(file,"rw")
        file.seek(addressInodes)
        Inode freeInode = new Inode()
        int freeDataClusterIndex = bitmap.findFirstFreeCluster()

        file.seek(addressInodes + 58*numOfFreeInode)
        int freeAddr = addressInodes + 58*numOfFreeInode
        int freePathAdr = superBlock.rootOffset + 5* numOfFreeInode
        ByteArrayBuffer buffer = new ByteArrayBuffer()
        while (buffer.size() != 58){
            file.read(buffer)
        }
        freeInode.setBytes(buffer.toByteArray())

        flushNewFile(new File(path).name, getExtension(path),freePathAdr,freeAddr,numOfFreeInode,freeDataClusterIndex)
        superBlock.clusterEmptyCount--
        superBlock.inodeEmptyList[numOfFreeInode] = -1
        _currentDir = backup

    }
    DirectoryCluster readDirectoryClusters(int clusterIndex, String fullPath, boolean isRoot, DirectoryCluster dirToFill){
        DirectoryCluster result = dirToFill? dirToFill : new DirectoryCluster()
        if(!dirToFill)
            result.clear()
        if (isRoot){
            result.Path = "/"
            Inode inode = new Inode()
            inode.read(file, superBlock.ilistOffset)
            result.createDate = inode.createDate
            result.extension = ""
            result.filename = "/"
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
        }
        else {
            result.Path = fullPath
            result.streamAddress = superBlock.rootOffset + 2048 + (clusterIndex - 1) * CLUSTER_SIZE
        }
        long address = _root? superBlock.rootOffset : superBlock.rootOffset + 2048 + (clusterIndex - 1) * CLUSTER_SIZE
        long dirRecordsBeginAddress
        int nextClusterIndex = LAST_CLUSTER_ID
        RandomAccessFile file = new RandomAccessFile(file,"rw")
        loop{
            file.seek(address)
            dirRecordsBeginAddress = address + 4
            nextClusterIndex = file.readInt()
            int sizeOfDir = 39
            Directory[] directories = new Directory[(CLUSTER_SIZE - 4)/sizeOfDir].each {_directory ->
                ByteArrayBuffer buffer = new ByteArrayBuffer()
                while (buffer.size() != 39)
                    buffer.write(file.readByte())
                _directory.filename = buffer.toByteArray().swap(0,31)
                _directory.extension = buffer.toByteArray().swap(32,36)
                _directory.inodeNumber = (short)(buffer.toByteArray()[37] << 8 | buffer.toByteArray()[38] & 0xFF)
            }
            int fileInodeId
            address = superBlock.ilistOffset
            file.seek(address)
            int sizeOfInode = 58
            DirectoryCluster meta
            Inode inode
            for(int i = 0; i< directories.length; i++){
                fileInodeId = directories[i].inodeNumber
                if(fileInodeId != FREE_DIRECTORY_RECORD){
                    file.seek(address + (fileInodeId-1)*sizeOfInode)
                    ByteArrayBuffer buffer = new ByteArrayBuffer()
                    while (buffer.size() != 58){
                        buffer.write(file.readByte())
                    }
                    inode = new Inode()
                    inode.setBytes(buffer.toByteArray())
                    meta = new DirectoryCluster()
                    meta.createDate = inode.createDate
                    meta.extension = directories[i].extension
                    meta.filename = directories[i].filename
                    meta.groupID = inode.groupID
                    meta.userID = inode.userID
                    meta.editDate = inode.editDate
                    meta.size = inode.size
                    meta.chmod = inode.chmod
                    meta.hiden = inode.hiden
                    meta.dir = inode.dir
                    meta.system = inode.size
                    meta.readonly = inode.readonly
                    result.add(meta)
                }
            }
            address = superBlock.rootOffset + 2048 + (nextClusterIndex - 1)*CLUSTER_SIZE
        }until {nextClusterIndex != LAST_CLUSTER_ID}
        result
    }

    Directory OpenDirectory(String path) {
        File file = new File(path)
        String[] list = file.list()
        if(path == "/" || list.length == 0){
            return readDirectoryClusters(0,null,true,null)
        }
        Directory _dir = _root
        Directory curr = _dir
        for(int i = 0; i < list.length; i++){
            _dir = ReadDirectory(_currentDir, list[i])
            curr = _dir
            if(_dir != null){
                _currentDir = _dir as DirectoryCluster
            }
            else {
                throw new NotDirectoryException("Директория не существует. Проверьте правильность пути.")
            }
        }
        _dir
    }

    static Directory ReadDirectory(DirectoryCluster currentDirectory, String dirName){
        currentDirectory.find(dirName)
    }
    byte[] readFile(String path, int offset = 0, int count = -1) {
        CheckPath(path)
        String fullPath = Utils.getFullPath(path,_currentDir.Path)
        String parentDirectoryPath = Utils.getDirectoryName(fullPath)
        String filename = Utils.getFileName(fullPath)
        String fileNameWithoutExtension = Utils.getFileNameWithoutExtension(fullPath)
        String extension = Utils.getExtension(fullPath)
        DirectoryCluster backup = _currentDir
        def file = _currentDir.find(filename)
        if(file == null)
            return null
        if(!Utils.getAccess(userId,groupId,_currentDir.userID,_currentDir.groupID,new AccessRights((short)_currentDir.chmod)).canExecute || !Utils.getAccess(userId,groupId,file.userID,file.groupID,new AccessRights((short)file.chmod)).canRead){
            throw new Exception("У вас нет прав доступа")
        }
        if(offset < 0)
            throw new Exception("Значение смещения не может быть отрицательным")
        if (count > file.size)
            throw new Exception("Значение кол-ва байтов превышает размер файла")
        long address = superBlock.rootOffset + 2048
        RandomAccessFile f = new RandomAccessFile(this.file,"rw")
        f.seek(address)
        int currentOffset = 0
        count = (count == -1) ? file.size : count
        byte[] data = new byte[count]
        while (currentOffset + CLUSTER_SIZE - 4 <= offset){
            currentOffset += CLUSTER_SIZE
        }
        int nextCluster = f.readInt()
        while (currentOffset - offset < count){
            if(count - currentOffset - offset >= CLUSTER_SIZE - 4){
                currentOffset += currentOffset += f.read(data, (currentOffset - offset), (CLUSTER_SIZE - 4))
            }
            else{
                currentOffset += f.read(data,currentOffset - offset,count - currentOffset - offset)
            }
            if(nextCluster != LAST_CLUSTER_ID){
                address = superBlock.rootOffset + 2048 + (nextCluster - 1) * CLUSTER_SIZE
                f.seek(address)
                nextCluster = f.readInt()
            }
        }
        _currentDir = backup
        data
    }
    int writeFile(String path, byte[] data){
        CheckPath(path)
        String fullPath = Utils.getFullPath(path,_currentDir.Path)
        String parentDirectoryPath = Utils.getDirectoryName(fullPath)
        String filename = Utils.getFileName(fullPath)
        String fileNameWithoutExtension = Utils.getFileNameWithoutExtension(fullPath)
        String extension = Utils.getExtension(fullPath)
        //Каждый раз начинаем поиск метафайла с корня
        DirectoryCluster backup = _currentDir
        _currentDir = _root as DirectoryCluster
        _currentDir = OpenDirectory(parentDirectoryPath) as DirectoryCluster
        DirectoryCluster file = _currentDir.find(filename)
        if(file == null)
            throw new Exception("Файл не найден")
        if(!Utils.getAccess(userId,groupId,_currentDir.userID,_currentDir.groupID,new AccessRights((short)_currentDir.chmod)).canExecute || !Utils.getAccess(userId,groupId,file.userID,file.groupID,new AccessRights((short)file.chmod)).canWrite){
            throw new Exception("У вас нет прав доступа")
        }
        if(data == null || data.length == 0)
            throw new Exception("Данные не могут быть пустыми")
        int sizeOfInode = 58

        Inode inode = new Inode()
        inode.read(this.file,superBlock.ilistOffset + (file.number - 1) * sizeOfInode)
        //ищем свободные блоки данных
        int numNeedDataClusters = data.length / (CLUSTER_SIZE - 4) + ((data.length % (CLUSTER_SIZE - 4) >0) ? 1 : 0)
        int numberExistDataClusters = inode.size / (CLUSTER_SIZE - 4)+ ((inode.size %(CLUSTER_SIZE - 4) >0) ? 1 : 0)
        numNeedDataClusters -= numberExistDataClusters + 1
        if(numNeedDataClusters < 0)
            numNeedDataClusters = 0
        int numberFreeDataClusters = superBlock.clusterEmptyCount
        if(numberFreeDataClusters < numNeedDataClusters && numNeedDataClusters > 11)
            throw new Exception("Недостаточно свободного места")
        int[] freeDataClustersIndexes = new int[1+numberExistDataClusters + numNeedDataClusters]
        int nextClusterValue = LAST_CLUSTER_ID
        long address = superBlock.rootOffset + 2048 + (Utils.bytesToShort(inode.di_addr[0]) - 1) * CLUSTER_SIZE
        RandomAccessFile f = new RandomAccessFile(this.file,"r")
        f.seek(address)
        int clusterIndex = 0
        freeDataClustersIndexes[clusterIndex] = Utils.bytesToShort(inode.di_addr[0])
        while ((nextClusterValue = f.readInt()) != LAST_CLUSTER_ID){
            address = superBlock.rootOffset + 2048 + (nextClusterValue - 1) * CLUSTER_SIZE
            f.seek(address)
            freeDataClustersIndexes[++clusterIndex] = nextClusterValue
        }
        ArrayList<Short>addr = new ArrayList<>()
        for (int i = 1 + numberExistDataClusters; i < 1+numberExistDataClusters + numNeedDataClusters; i++){
            int freeDataClusterIndex = bitmap.findFirstFreeCluster(true)
            if(freeDataClusterIndex < 0 )
                throw new Exception("Недостаточно места на жестком диске")
            freeDataClustersIndexes[i] = freeDataClusterIndex
            addr.add(freeDataClusterIndex as short)
            superBlock.clusterEmptyCount--
        }

        int numberWrittenBytes = 0
        nextClusterValue = LAST_CLUSTER_ID
        address = superBlock.rootOffset + 2048 + (Utils.bytesToShort(inode.di_addr[0]) - 1) * CLUSTER_SIZE
        f.seek(address)
        for(int clusterNumber = 1; clusterNumber <= numNeedDataClusters; clusterNumber++){
            nextClusterValue = freeDataClustersIndexes[clusterNumber]
            f.writeInt(nextClusterValue)
            f.write(data,numberWrittenBytes,CLUSTER_SIZE - 4)
            numberWrittenBytes += CLUSTER_SIZE - 4
            address = superBlock.rootOffset + 2048 + (nextClusterValue - 1) * CLUSTER_SIZE
            f.seek(address)
        }
        nextClusterValue = LAST_CLUSTER_ID
        f.writeInt(nextClusterValue)
        f.write(data,numberWrittenBytes, data.length - numberWrittenBytes)
        numberWrittenBytes += data.length - numberWrittenBytes
        inode.size = numberWrittenBytes
        for(int i = 0; i < 11; i++)
            inode.setAddr(i,addr.get(i))
        f.seek(superBlock.ilistOffset + (file.inodeNumber - 1) * 58)
        f.write(inode.getBytes())
        _currentDir = backup
        numberWrittenBytes
    }
    def rename(String path, String newName){///////////////////////////////////////////////////ДОПИСАТЬ
        DirectoryCluster current = _currentDir
        CheckPath(path)
        String fullPath = Utils.getFullPath(path, _currentDir.Path)
        String parentDirectoryPath = Utils.getDirectoryName(fullPath)
        String oldName = Utils.getFileName(fullPath)
        def directory = OpenDirectory(parentDirectoryPath) as DirectoryCluster
        if(!Utils.getAccess(userId,groupId,directory.userID,directory.groupID,new AccessRights((short)directory.chmod)).canRead || !Utils.getAccess(userId,groupId,directory.userID,directory.groupID,new AccessRights((short)directory.chmod)).canWrite){
            throw new Exception("У вас нет прав доступа")
        }
        def file = directory.find(oldName)
        if(file == null)
            throw new FileNotFoundException("Файл или каталог не существует")
        RandomAccessFile f  = new RandomAccessFile(this.file,"rw")
        //f.seek(file.)
    }
    def setAttributes(String path, Attributes attributes){
        DirectoryCluster current = _currentDir
        CheckPath(path)
        String fullPath = Utils.getFullPath(path,_currentDir.Path)
        String parent = Utils.getDirectoryName(fullPath)
        def directory = OpenDirectory(parent) as DirectoryCluster
        if(!Utils.getAccess(userId,groupId,directory.userID,directory.groupID,new AccessRights(directory.chmod as short)).canExecute){
            throw new Exception("У вас нет прав, желаем успеха в следующий раз :)")
        }
        String filename = Utils.getFileName(path)
        def file = directory.find(filename)
        if(file == null){
            throw new Exception("Каталог не существует")
        }
        Inode inode = new Inode()
        int sizeOfInode = 58
        inode.read(this.file,superBlock.ilistOffset + (file.inodeNumber - 1) * sizeOfInode)
        inode.system = attributes.system
        inode.dir = attributes.dir
        inode.hiden = attributes.hidden
        inode.readonly = attributes.readOnly
        inode.write()
        _currentDir = current
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
}
