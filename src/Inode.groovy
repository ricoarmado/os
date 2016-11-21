
import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.ByteBuffer
import java.nio.ByteOrder
/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Inode {
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
    byte[][] di_addr = new byte[12].each {_addr ->
        _addr = new byte[2]
    }

    byte[] getBytes() {
        ByteArrayBuffer buffer = new ByteArrayBuffer()
        buffer.write(number)
        buffer.write(chmod)
        int tmp = hiden?1:0
        buffer.write(tmp)
        tmp = system?1:0
        buffer.write(tmp)
        tmp = dir?1:0
        buffer.write(tmp)
        buffer.write()
        buffer.write(new String(createDate).getBytes())
        buffer.write(new String(editDate).getBytes())
        buffer.write(userID)
        buffer.write(groupID)
        buffer.write(size)
        di_addr.each {_addr -> buffer.write(_addr)}

        buffer.toByteArray()

    }
    def setBytes(byte[] bytes){
        byte[] tmp = bytes
        number = ByteBuffer.wrap(bytes,0,4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        chmod = ByteBuffer.wrap(bytes,4,4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        hiden = tmp[8].equals((byte)1)
        system = tmp[9].equals((byte)1)
        dir = tmp[10].equals((byte)1)
        readonly = tmp[11].equals((byte)1)
        createDate = ByteBuffer.wrap(bytes,12,8).array()
        editDate = ByteBuffer.wrap(bytes,20,8).array()
        userID = (short)(bytes[27] << 8 | bytes[28] & 0xFF)
        groupID = (short)(bytes[29] << 8 | bytes[30] & 0xFF)
        size = ByteBuffer.wrap(bytes,31,34).order(ByteOrder.LITTLE_ENDIAN).getInt()
        di_addr[0] = bytes.swap(35,36)
        di_addr[1] = bytes.swap(37,38)
        di_addr[2] = bytes.swap(39,40)
        di_addr[3] = bytes.swap(41,42)
        di_addr[4] = bytes.swap(43,44)
        di_addr[5] = bytes.swap(45,46)
        di_addr[6] = bytes.swap(47,48)
        di_addr[7] = bytes.swap(49,50)
        di_addr[8] = bytes.swap(51,52)
        di_addr[9] = bytes.swap(53,54)
        di_addr[10] = bytes.swap(55,56)
        di_addr[11] = bytes.swap(57,58)
    }
    def read(File f, int offset){
        RandomAccessFile file = new RandomAccessFile(f,"r")
        file.seek(offset)
        ByteArrayBuffer buffer = new ByteArrayBuffer()
        while (buffer.size() != 58){
            buffer.write(file.readByte())
        }
        this.setBytes(buffer.toByteArray())
        file.close()
    }
    def write(File f, int offset){
        RandomAccessFile file = new RandomAccessFile(f,"w")
        file.seek(offset)
        file.write(this.getBytes())
        file.close()
    }
    def setAddr(int index, short value){
        byte [] tmp = System.Utils.shortToBytes(value)
        di_addr[index][0] = tmp[0]
        di_addr[index][1] = tmp[1]
    }
}
