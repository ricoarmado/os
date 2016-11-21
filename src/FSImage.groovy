

import Inode

import java.time.LocalDate
import java.time.format.DateTimeFormatter
/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class FSImage {
    final short CLUSTER_LENGTH = 1024
    private long diskPartitionSize // Размер диска
    private int diskClusterSize
    private SuperBlock superBlock
    private Bitmap bitmap
    private Inodes inodes
    private DirectoryCluster rootDirectory
    private byte[] emptyData//Структуры данных

    FSImage() {
        this.diskPartitionSize = 32 * 1024 * 1024 // 32MB
        this.diskClusterSize = CLUSTER_LENGTH
        this.bitmap = new Bitmap((int) (diskPartitionSize / diskClusterSize))
        this.inodes = new Inodes((int) (diskPartitionSize / diskClusterSize))
        this.rootDirectory = new DirectoryCluster((short)diskClusterSize)
        this.emptyData = new byte[diskPartitionSize - 6238 * 1024]
        // Размер диска - кол-во блоков служ.области* р.кластера
        superBlock = new SuperBlock()
        superBlock.fsType = "GROOVYFS_TYRSASV"
        superBlock.clusterCount = (int) (diskPartitionSize / diskClusterSize)
        superBlock.clusterSize = CLUSTER_LENGTH
        superBlock.clusterEmptyCount = superBlock.clusterCount
        superBlock.inodeCount = 32729

        initializeFileSystem()
    }

    def initializeFileSystem() {
        Inode inode
        inode = new Inode()
        inode.dir = true // Каталог
        inode.chmod = 0x01FD// rwx|rwx|r-x
        inode.createDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        inode.editDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
        inode.size = diskClusterSize
        inode.readonly = true
        inode.hiden = false
        inode.system = true
        inode.userID = 1// root-user
        inode.groupID = 1// root-group
        inode.number = 1//root - каталог
        inodes.appendInode(inode,0)
        for(int i = 1; i < inodes.getCount(); i++){
            inode = new Inode()
            inode.number = i + 1
            inode.createDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
            inode.editDate = DateTimeFormatter.ofPattern("yyyMMdd").format(LocalDate.now())
            inodes.appendInode(inode,i)
        }
        superBlock.ilistOffset = 65494 + 1 // superblock
        superBlock.bitmapOffset = superBlock.ilistOffset + 58*32729 + 1 // superblock + ilist
        superBlock.rootOffset = superBlock.bitmapOffset + 2048 + 1
    }
    def export(String path){
        RandomAccessFile file = new RandomAccessFile(path,"rw")
        file.write(superBlock.getBytes())
        file.write(inodes.getBytes())
        file.write(bitmap.getBytes())
        file.write(rootDirectory.getBytes())
        file.write(emptyData)
    }
}
