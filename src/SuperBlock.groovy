
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
    def inodeEmptyList = new byte[65458]
    int ilistOffset
    int  bitmapOffset
    int rootOffset
    byte[] getBytes(){
        ByteArrayBuffer buff = new ByteArrayBuffer()
        buff.write(fsType as byte[])
        buff.write(ByteBuffer.allocate(2).putShort(clusterSize).array())
        buff.write(ByteBuffer.allocate(4).putInt(clusterCount).array())
        buff.write(ByteBuffer.allocate(4).putInt(clusterEmptyCount).array())
        buff.write(ByteBuffer.allocate(4).putInt(inodeCount).array())
        buff.write(inodeEmptyList)
        buff.write(ByteBuffer.allocate(4).putInt(ilistOffset).array())
        buff.write(ByteBuffer.allocate(4).putInt(bitmapOffset).array())
        buff.write(ByteBuffer.allocate(4).putInt(rootOffset).array())
        buff.toByteArray()
    }
    def setBytes(ByteArrayBuffer buffer){
        byte[] bytes = buffer.getRawData()
        ByteArrayBuffer buff = new ByteArrayBuffer(16)
        for(int i = 0; i < 16;i++){
            buff.write(bytes[i])
        }
        fsType = new String(buff.toByteArray())
        clusterSize = (short)(bytes[16] << 8 | bytes[17] & 0xFF)
        def arr = bytes[18..21]  as byte[]
        clusterCount = ByteBuffer.wrap(arr).getInt()
        arr = bytes[22..25] as byte[]
        clusterEmptyCount = ByteBuffer.wrap(arr).getInt()
        arr = bytes[26..30] as byte[]
        inodeCount = ByteBuffer.wrap(arr).getInt()
        arr = bytes[30..65487] as byte[]
        inodeEmptyList = arr
        arr = bytes[65488..65491] as byte[]
        ilistOffset = ByteBuffer.wrap(arr).getInt()
        arr = bytes[65492..65495] as byte[]
        bitmapOffset = ByteBuffer.wrap(arr).getInt()
        arr = bytes[65496..65499] as byte[]
        rootOffset = ByteBuffer.wrap(arr).getInt()
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
