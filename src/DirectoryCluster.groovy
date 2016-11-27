
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
    char[] extension = new char[5]
    short inodeNumber
    short FREE_INODE_ID = -1
    long streamAddress
    String Path
    DirectoryCluster(short freeInodeId) {
        setInodeNumber(freeInodeId)
        extension = "".getChars()
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
        int max = filename.length < 32?filename.length : 32
        for (int i = 0; i < max; i++) {
            arr[i] = filename[i] as byte
        }
        max = extension.length < 5 ? extension.length : 5
        for (int i = 32; i < 32 + max - 1;i++){
            arr[i] = extension[i] as byte
        }
        def sh = ByteBuffer.allocate(2).putShort(inodeNumber).array()
        arr[37] = sh[0]
        arr[38] = sh[1]
        return arr
    }
    DirectoryCluster find(String name){
        directories.find { s -> s.filename.toString() == name } as DirectoryCluster
    }

    def add(DirectoryCluster directoryCluster){
        directories.add(directoryCluster)
    }
    def addMany(DirectoryCluster cluster, int firstElem){
        if(firstElem > 0)
            firstElem--
        def split = cluster.filename.toString().split("/")
        String path = "/"+ split[firstElem]
        if(this.filename.toString().trim() == path){
            add(cluster)
        }
        def find = directories.find { _dir -> _dir.filename.toString().trim() == path } as DirectoryCluster
        if(find != null){
            find.addMany(cluster,++firstElem)
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
        byte[] tmp = bytes[0..31]
        filename = new String(tmp).getChars()
        tmp = bytes[32..36] as byte[]
        extension = new String(tmp).getChars()
        tmp = bytes[37..38]
        inodeNumber = (short) (tmp[0]<<8 | tmp[1]);

    }
    List<String> list(){
        List<String> tmp = new ArrayList<>()
        directories.each {_d -> tmp.add(new String(_d.filename ) + extension )}
        tmp
    }
    boolean isEmpty(){
        filename[0].equals(Character.MIN_VALUE)
    }


}
