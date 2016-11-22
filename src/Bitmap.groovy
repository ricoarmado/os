

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Bitmap {
    def setBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            _bits.set(i,bytes[i] == 1 as byte)

        }
    }

    int findFirstFreeCluster(boolean reserveCluster = false) {
        for(int i = 1; i < _bits.length()/2 +1; i++){
            if(!_bits.get(i)){
                if(reserveCluster){
                    setClusterState(i,ClusterState.Used)
                }
                return i
            }
        }
        -1
    }

    enum ClusterState { Free,Used }
    BitSet _bits

    Bitmap(int bitmapNumClusters){
        _bits = new BitSet(bitmapNumClusters);
    }
    byte getByIndex(int index){
        _bits.get(index) as byte
    }

    byte setClusterState(int clusterIndex, ClusterState state){
        boolean bit = !(state == ClusterState.Free)
        _bits.set(clusterIndex,bit)
        return state as byte
    }
    byte[] getBytes() {
        _bits.toByteArray()
    }

    boolean isEmpty(int num){
        !_bits.get(num)
    }
}
