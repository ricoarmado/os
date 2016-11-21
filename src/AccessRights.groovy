

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class AccessRights {
    RightsGroup user
    RightsGroup group
    RightsGroup others
    AccessRights(boolean ur, boolean uw, boolean ux, boolean gr, boolean gw, boolean gx, boolean or, boolean ow, boolean ox) {
        user.canRead = ur
        user.canWrite = uw
        user.canExecute = ux
        group.canRead = gr
        group.canWrite = gw
        group.canExecute = gx
        others.canRead = or
        others.canWrite = ow
        others.canExecute = ox
    }
    AccessRights(short permissions){
        user.canRead = (permissions & (1 << 8)) > 0
        user.canWrite = (permissions & (1 << 7)) > 0
        user.canExecute = (permissions & (1 << 6)) > 0
        group.canRead = (permissions & (1 << 5)) > 0
        group.canWrite = (permissions & (1 << 4)) > 0
        group.canExecute = (permissions & (1 << 3)) > 0
        others.canRead = (permissions & (1 << 2)) > 0
        others.canWrite = (permissions & (1 << 1)) > 0
        others.canExecute = (permissions & (1 << 0)) > 0

    }
    def setFullAccess(boolean user, boolean group , boolean others){
        if(user){
            this.user.canRead = true
            this.user.canExecute = true
            this.user.canWrite = true
        }
        if(group){
            this.group.canRead = true
            this.group.canExecute = true
            this.group.canWrite = true
        }
        if(others){
            this.others.canRead = true
            this.others.canExecute = true
            this.others.canWrite = true
        }
    }
    short toInt16(){
        (short) (((user.canRead?1:0) << 8) |
                ((user.canWrite?1:0) << 7) |
                ((user.canExecute?1:0) << 6) |
                ((group.canRead?1:0) << 5) |
                ((group.canWrite?1:0) << 4) |
                ((group.canExecute?1:0) << 3) |
                ((others.canRead?1:0) << 2) |
                ((others.canWrite?1:0) << 1) |
                ((others.canExecute?1:0) << 0))
    }
}
