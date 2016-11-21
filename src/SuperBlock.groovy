
import com.sun.xml.internal.ws.util.ByteArrayBuffer
/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class SuperBlock {
    def fsType = new char[16]
    short clusterSize
    int clusterCount
    int clusterEmptyCount
    int inodeCount
    def inodeEmptyList = new byte[64558]
    short ilistOffset
    short bitmapOffset
    short rootOffset
    byte[] getBytes(){
        ByteArrayBuffer buff = new ByteArrayBuffer()
        buff.write(fsType as byte[])
        buff.write(clusterSize)
        buff.write(clusterCount)
        buff.write(clusterEmptyCount)
        buff.write(inodeCount)
        buff.write(inodeEmptyList)
        buff.write(ilistOffset)
        buff.write(bitmapOffset)
        buff.write(rootOffset)
        buff.toByteArray()
    }
    def setBytes(byte[] bytes){

    }
    def markInodeAsBusy(int pos){
        inodeEmptyList[i] = 1
    }
    boolean isInodeBusy(int pos){
        (inodeEmptyList[pos] == 1 as byte)
    }
    int findFreeInode(){
        for(byte b in inodeEmptyList){
            if(b == 1 as byte){
                return b
            }
        }
        -1
    }
}
