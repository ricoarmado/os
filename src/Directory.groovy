

import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.util.function.Consumer

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Directory {
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
    def filename = new char[30]
    def extension = new char[5]
    short inodeNumber
    public Directory(){

    }
    public Directory(short freeInodeId){
        setInodeNumber(freeInodeId)
        extension = ""
        filename = ""
    }
    def setFullName(String filename, String extension){
        this.filename = filename.substring(0,29).toCharArray()
        this.extension = extension.substring(0,4).toCharArray()
    }
    byte[] getBytes(){
        def buffer = new ByteArrayBuffer()

        buffer.write(filename as byte[])
        buffer.write(extension as byte[])
        buffer.write(inodeNumber)
        return buffer.toByteArray()
    }
    List<String> list(){
        List<String> tmp = new ArrayList<>()
        directories.each {_d -> tmp.add(new String(_d.filename ) + extension )}
        tmp
    }
}
