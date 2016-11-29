

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Group {
    short GRID
    char[] groupname = new char[30]
    List<User>users
    Group(GRID, char []groupname) {
        this.GRID = GRID
        this.groupname = groupname
        users = new ArrayList<>()
    }
    def addUser(User user){
        users.add(user)
    }
}
class GroupManager{
    List<Group>groups = new ArrayList<>()
    def add(Group group){
        groups.add(group)
    }
    int getCount(){
        groups.size()
    }
    def addUser(short grid, User user){
        groups.find {_group -> _group.GRID == grid}.addUser(user)
    }
    String[] getNames(){
        List<String> tmp = new ArrayList<>()
        groups.each {_g -> tmp.add(new String(_g.groupname))}
        tmp.toArray() as String[]
    }
    List<String>getList(){
        List<String>array = new ArrayList<>()
        groups.each {_user -> array.add(new String(_user.getGroupname()))}
        return array
    }
    int getGrid(String grName){
        groups.find {_group -> new String(_group.groupname).trim() == grName}.getGRID()
    }
}
