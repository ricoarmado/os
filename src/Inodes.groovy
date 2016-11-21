

import Inode
import com.sun.xml.internal.ws.util.ByteArrayBuffer
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
        ByteArrayBuffer inode
        inodes.each {_tmp ->
            inode = new ByteArrayBuffer()
            inode.write(_tmp.number)
            inode.write(_tmp.chmod)
            int tmp = _tmp.hiden?1:0
            inode.write(tmp)
            tmp = _tmp.system?1:0
            inode.write(tmp)
            tmp = _tmp.dir?1:0
            inode.write(tmp)
            tmp = _tmp.readonly?1:0
            inode.write(tmp)
            inode.write(new String(_tmp.createDate).getBytes())
            inode.write(new String(_tmp.editDate).getBytes())
            inode.write(_tmp.userID)
            inode.write(_tmp.groupID)
            inode.write(_tmp.size)
            for(byte [] addr in _tmp.di_addr){
                inode.write(addr)
            }
            _inodes.write(inode.toByteArray())
        }
        _inodes.toByteArray()
    }
    def setBytes(byte [] bytes){

    }
}
