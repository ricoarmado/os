import java.util.BitSet;

/**
 * Created by stanislavtyrsa on 26.11.16.
 */
public class Bitmap {
    enum ClusterState { Free,Used };
    private byte[] _bits;
    void setBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            _bits[i] = (byte) (bytes[i] == 1?1:0);
        }
    }
    public Bitmap(int bitmapNumClusters){
        _bits = new byte[bitmapNumClusters];
    }
    byte getByIndex(int index){
        return (byte) (_bits[index] == 1?1:0);
    }

    byte setClusterState(int clusterIndex, ClusterState state){
        boolean bit = !(state == ClusterState.Free);
        _bits[clusterIndex] = (byte) (bit?1:0);
        return (byte) (bit?1:0);
    }
    byte[] getBytes() {
        return _bits;
    }

    boolean isEmpty(int num){
        return !(_bits[num] == 1);
    }
    int size(){
        return _bits.length;
    }
    boolean isEmpty(){
        boolean empty = false;
        for(byte b :_bits){
            if(b == 1){
                empty = true;
                break;
            }
        }
        return empty;
    }
    int findFirstFreeCluster(boolean reserveCluster) {
        for(int i = 1; i < _bits.length/2 +1; i++){
            if(!(_bits[i] == 1)){
                if(reserveCluster){
                    setClusterState(i,ClusterState.Used);
                }
                return i;
            }
        }
        return -1;
    }

}
