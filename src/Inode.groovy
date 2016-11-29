
import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.ByteBuffer
import java.nio.ByteOrder
/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Inode {
    short number
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
    byte[][] di_addr = new byte[12][2]

    Inode() {

    }

    byte[] getBytes() {
        ByteArrayBuffer buffer = new ByteArrayBuffer()
        buffer.write(ByteBuffer.allocate(2).putShort(number).array())
        buffer.write(ByteBuffer.allocate(4).putInt(chmod).array())
        byte tmp = hiden?1:0
        buffer.write(tmp)
        tmp = system?1:0
        buffer.write(tmp)
        tmp = dir?1:0
        buffer.write(tmp)
        tmp = readonly?1:0
        buffer.write(tmp)
        buffer.write(new String(createDate).getBytes())
        buffer.write(new String(editDate).getBytes())
        buffer.write(ByteBuffer.allocate(2).putShort(userID).array())
        buffer.write(ByteBuffer.allocate(2).putShort(groupID).array())
        buffer.write(ByteBuffer.allocate(4).putInt(size).array())
        di_addr.each {
            _addr -> buffer.write(_addr)
        }

        buffer.toByteArray()

    }
    def setBytes(byte[] bytes){
        def arr = bytes[0..1] as byte[]
        number = ByteBuffer.wrap(arr).getShort();
        arr = bytes[2..5] as byte[]
        chmod = ByteBuffer.wrap(arr).getInt();
        hiden = bytes[6].equals((byte)1)
        system = bytes[7].equals((byte)1)
        dir = bytes[8].equals((byte)1)
        readonly = bytes[9].equals((byte)1)
        arr = bytes[10..17] as byte[]
        createDate = new String(arr)
        arr = bytes[18..25] as byte[]
        editDate = new String(arr)
        userID = (short)(bytes[26] << 8 | bytes[27] & 0xFF)
        groupID = (short)(bytes[28] << 8 | bytes[29] & 0xFF)
        arr = bytes[30..33] as byte[]
        size = ByteBuffer.wrap(arr).getInt()
        di_addr[0] = bytes[34..35]
        di_addr[1] = bytes[36..37]
        di_addr[2] = bytes[38..39]
        di_addr[3] = bytes[40..41]
        di_addr[4] = bytes[42..44]
        di_addr[5] = bytes[44..46]
        di_addr[6] = bytes[46..48]
        di_addr[7] = bytes[48..50]
        di_addr[8] = bytes[50..52]
        di_addr[9] = bytes[52..54]
        di_addr[10] = bytes[54..56]
        di_addr[11] = bytes[56..58]
    }
    def read(File f, int offset){
        RandomAccessFile file = new RandomAccessFile(f,"rw")
        file.seek(offset)
        ByteArrayBuffer buffer = new ByteArrayBuffer()
        while (buffer.size() != 59){
            buffer.write(file.readByte())
        }
        this.setBytes(buffer.toByteArray())
        file.close()
    }
    def write(File f, int offset){
        RandomAccessFile file = new RandomAccessFile(f,"rw")
        file.seek(offset)
        file.write(this.getBytes())
        file.close()
    }
    def setAddr(int index, short value){
        byte [] tmp = Utils.shortToBytes(value)
        di_addr[index][0] = tmp[0]
        di_addr[index][1] = tmp[1]
    }
}
