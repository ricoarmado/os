

import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.ByteBuffer
import java.util.function.Consumer

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Directory {
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

    List<Directory> directories = new ArrayList<>()
    char[] filename = new char[32]
    char[] extension = new char[5]
    short inodeNumber
    public Directory(){

    }
    public Directory(short freeInodeId){
        setInodeNumber(freeInodeId)
        extension = "".getChars()
        filename = "".getChars()
    }
    def setFullName(String filename, String extension){
        this.filename = filename.substring(0,29).toCharArray()
        this.extension = extension.substring(0,4).toCharArray()
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
    List<Directory>getDirectories(){
        return this.directories
    }

}
