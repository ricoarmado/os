

import Inode
import com.sun.xml.internal.ws.util.ByteArrayBuffer

import java.nio.ByteBuffer

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Inodes {
    Inode[] inodes // массив инодов
    public Inodes(int numOfInodes){
        def tmp = new Inode()
        inodes = new Inode[numOfInodes]

        for(int i = 0; i < inodes.length; i++){
            tmp.number = i + 1
            inodes[i] = tmp // присваиваем инодам по очереди номера
        }
    }
    def appendInode(Inode inode, int pos){
        inodes[pos] = inode
    }
    int getCount(){
        inodes.length
    }
    Inode getInode() {
        new Inode()
    }
    byte[] getBytes() {
        ByteArrayBuffer _inodes = new ByteArrayBuffer()
        inodes.each {_tmp ->
            _inodes.write(_tmp.getBytes())
        }
        _inodes.toByteArray()
    }
    def setBytes(byte [] bytes){

    }
}
