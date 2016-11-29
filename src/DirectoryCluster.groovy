
import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.ByteBuffer

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class DirectoryCluster {
    final String NEWLINE = System.getProperty("line.separator");
    int number
    int chmod
    boolean hiden
    boolean system
    boolean dir
    boolean readonly
    def createDate = new char[8]
    def editDate = new char[8]
    short userID
    short groupID
    int size

    List<DirectoryCluster> directories = new ArrayList<>()

    char[] filename = new char[32]
    short inodeNumber


    short FREE_INODE_ID = -1
    long streamAddress

    String Path
    DirectoryCluster(short freeInodeId) {
        setInodeNumber(freeInodeId)
        filename = "".getChars()
    }
    public DirectoryCluster(){
        super()
        filename = "/".getChars()
    }
    char [] getFileName(){
        return new String(filename).getChars()
    }
    byte[] getBytes(){
        byte[] arr = new byte[39]
        String fullname = new String(fileName)
        int max = fullname.length() < 37 ? fullname.length() : 37
        for (int i = 0; i < max; i++) {
            arr[i] = fullname.getChars()[i] as byte
        }
        def sh = Utils.shortToBytes(inodeNumber)
        arr[37] = sh[0]
        arr[38] = sh[1]
        return arr
    }
    DirectoryCluster find(String name){
        directories.find { s -> new String(s.filename).trim() == name } as DirectoryCluster
    }

    def add(DirectoryCluster directoryCluster){
        directories.add(directoryCluster)
    }
    def addMany(DirectoryCluster cluster, int firstElem){
        boolean toRoot = false
        String path
        //if(firstElem > 0)
        //    firstElem--
        def split = new String(cluster.getFileName()).trim().split("/")
        if(split[0] == "")
            path = split[firstElem + 1]
        else
            path = split[firstElem]
        def find = directories.find { _dir -> new String(_dir.filename).trim().contains(path) } as DirectoryCluster
        if(find != null){
            find.addMany(cluster,++firstElem)
        }
        else {
            add(cluster)
        }
    }
    List<DirectoryCluster>getDirectories(){
        return this.directories
    }
    def clear() {
        directories.clear()
    }
    def setDirectories(List<DirectoryCluster>directoryClusters){

    }
    def setBytes(byte[] bytes){
        byte[] tmp = bytes[0..36]
        filename = new String(tmp).getChars()
        tmp = bytes[37..38]
        inodeNumber = Utils.bytesToShort(tmp)
    }
    List<String> list(){
        List<String> tmp = new ArrayList<>()
        directories.each {_d -> tmp.add(new String(_d.filename ))}
        tmp
    }
    boolean isEmpty(){
        filename[0].equals(Character.MIN_VALUE)
    }
    DirectoryCluster lookupDir(String path,int pos){
        String [] names = path.split("/")
        DirectoryCluster toReturn = null
        int count = names.length
        if(pos == count){
            return this
        }
        if(names[pos] == "")
            pos++
        for(DirectoryCluster dir : directories){
            if(new String(dir.getFilename()).trim().contains(names[pos])){
                toReturn = dir.lookupDir(path,++pos)
                pos--
            }
        }
        toReturn
    }
    def replace(DirectoryCluster oldField, DirectoryCluster newField){
        if(directories.contains(oldField)){
            def of = directories.indexOf(oldField)
            directories.set(of,newField)
        }
        for (DirectoryCluster dir : directories){
            dir.replace(oldField,newField)
        }

    }
    char[] getCreateDate(){
        createDate
    }
    char[] getEditDate(){
        editDate
    }
    def write(File file, int offset){
        RandomAccessFile f = new RandomAccessFile(file,"rw")
        f.seek(offset)
        f.write(this.getBytes())
        f.close()
    }
    def rename(DirectoryCluster oldField, String newField, int offset, File file){
        DirectoryCluster tmp = directories.find {_dir -> new String(_dir.getFilename()).trim() == new String(oldField.getFilename()).trim()}
        if(tmp != null){
            def of = directories.indexOf(tmp)
            def toSet = directories.get(of)
            String oldname = new String(toSet.getFilename()).trim()
            toSet.setFilename(newField.getChars())
            if (toSet.isDir())
                toSet.flushNames(oldname,newField, 65500 + 59*32729 + 32768, file)
            directories.set(of,toSet)
            toSet.write(file,offset)

        }else {
            for (DirectoryCluster dir : directories){
                dir.rename(oldField,newField,offset,file)
            }
        }

    }
    def flushNames(String oldname,String newname, int offset, File file){
        for (int i = 0; i < directories.size(); i++) {
            DirectoryCluster tmp = directories.get(i)
            String str = new String(tmp.getFilename()).trim()
            def replace = new StringBuffer(str).replaceAll(oldname,newname)
            tmp.setFilename(replace.getChars())
            directories.set(i,tmp)
            tmp.write(file, offset + 39*tmp.inodeNumber)
            tmp.flushNames(oldname,newname ,offset,file)
        }
    }
}
