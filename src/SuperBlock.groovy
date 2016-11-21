
import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.ByteBuffer

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
    def setBytes(ByteArrayBuffer buffer){
        byte[] bytes = buffer.getRawData()
        byte[] tmp = new byte[16]
        buffer.write(tmp,0,16)
        fsType = tmp
        tmp = new byte[2]
        clusterSize = (short)(bytes[16] << 8 | bytes[17] & 0xFF)
        tmp = new byte[4]
        buffer.write(tmp,18,4)
        clusterCount = ByteBuffer.wrap(tmp).getInt()
        buffer.write(tmp,22,4)
        clusterEmptyCount = ByteBuffer.wrap(tmp).getInt()
        buffer.write(tmp,26,4)
        inodeCount = ByteBuffer.wrap(tmp).getInt()
        tmp = new byte[64558]
        buffer.write(tmp,30,64558)
        inodeEmptyList = tmp
        ilistOffset = (short)(bytes[64588] << 8 | bytes[64589] & 0xFF)
        bitmapOffset = (short)(bytes[64590] << 8 | bytes[64591] & 0xFF)
        rootOffset = (short)(bytes[64592] << 8 | bytes[64593] & 0xFF)
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
