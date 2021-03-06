

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class User {
    short UID
    short GRID
    char[] username = new char[30]
    byte[] password

    User(short UID, short GRID, char[] username, byte[] password) {
        this.UID = UID
        this.GRID = GRID
        this.username = username
        this.password = password
    }
}
class UserManager {
    List<User>users

    UserManager() {
        users = new ArrayList<>()
    }
    def add(User user){
        users.add(user)
    }
    User findUser(String name){
        users.find {u -> u.username == name.chars}
    }
    List<String>getList(){
        List<String>array = new ArrayList<>()
        users.each {_user -> array.add(new String(_user.getUsername()))}
        return array
    }
}
