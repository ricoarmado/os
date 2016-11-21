
import Directory
import com.sun.xml.internal.ws.util.ByteArrayBuffer
/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class DirectoryCluster extends Directory{
    List<Directory> directories = new ArrayList<>()
    short FREE_INODE_ID = -1
    long streamAddress
    String Path
    DirectoryCluster(short freeInodeId) {
        super(freeInodeId)
    }
    public DirectoryCluster(){
        super()
    }
    byte[] getBytes() {
        ByteArrayBuffer buffer = new ByteArrayBuffer()
        ByteArrayBuffer tmp
        buffer.write()
        for(Directory rec in directories){
            tmp = new ByteArrayBuffer()
            tmp.write(rec.filename as byte[])
            tmp.write(rec.extension as byte[])
            tmp.write(rec.inodeNumber)
            buffer.write(tmp.toByteArray())
        }
        buffer.toByteArray()
    }
    DirectoryCluster find(String name){
        directories.find { s -> s.filename.toString() == name } as DirectoryCluster
    }

    def add(DirectoryCluster directoryCluster){
        directories.add(directoryCluster)
    }

    def clear() {
        directories.clear()
    }
}
