

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class User {
    short UID
    short GRID
    char[] username = new char[30]
    char[] password = new char[128]

    User(short UID, short GRID, username, password) {
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
}
