

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class RightsGroup {
    boolean canRead
    boolean canWrite
    boolean canExecute
    public RightsGroup(boolean r,boolean w,boolean x){
        canRead = r
        canWrite = w
        canExecute = x
    }
    public RightsGroup(){

    }
    RightsGroup get(){
        this
    }

    static RightsGroup get(boolean r, boolean w, boolean x){
        RightsGroup gr = new RightsGroup(r,w,x)
        return gr
    }

    boolean getCanRead() {
        return canRead
    }
    boolean getCanWrite() {
        return canWrite
    }
    boolean getCanExecute() {
        return canExecute
    }
}
