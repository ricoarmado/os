

import Group
import GroupManager
import User
import UserManager
import com.sun.xml.internal.ws.util.ByteArrayBuffer
import org.apache.commons.codec.digest.DigestUtils
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import java.nio.ByteBuffer

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Sys {
    final String SYSTEM_DIRECTORY_PATH = "/";
    final String USERS_FILE_PATH = "/groovyos/users";
    final String GROUPS_FILE_PATH = "/groovyos/groups";
    //final String ImagePATH = "image";
    final String NEWLINE = System.getProperty("line.separator");
    //Root
    final String ROOT_USERNAME = "root";
    final short ROOT_USER_ID = 1;
    final String ROOT_GROUPNAME = "root";
    final short ROOT_GROUP_ID = 1;
    final byte[] ROOT_PASSWORD_HASH = DigestUtils.md5("root");
    FileSystem kernel;
    UserManager _userManager;
    GroupManager _groupManager;
    User rootUser;
    boolean loggedIn;
    User user;
    boolean isFirstRun(){
        try {
            String path = kernel.getFile().getParent() + "/groovyos/users"
            RandomAccessFile randomAccessFile = new RandomAccessFile(new File(path), "rw")
        }catch (Exception ex){
            return true;
        }
        return false;
    }
    public Sys(){
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(new FileReader(new File("settings.json")));
        String image = (String)object.get("image");
        kernel = new FileSystem(new File(image),ROOT_USER_ID,ROOT_GROUP_ID);
        _userManager = new UserManager();
        _groupManager = new GroupManager();
        loggedIn = false;
    }
    public boolean init() throws IOException {
        if(isFirstRun()){
            User root = new User(ROOT_USER_ID,ROOT_GROUP_ID,ROOT_USERNAME,ROOT_PASSWORD_HASH);
            _userManager.add(root);
            Group rootGroup = new Group(ROOT_GROUP_ID,ROOT_GROUPNAME);
            rootGroup.addUser(root);
            _groupManager.add(rootGroup);
            Flush()
            rootUser = root;
            user = root;
            loggedIn = false;
            return false;
        }
        else{
            return true;
        }
    }
    public void Initialize() {
        String parent = kernel.getFile().getParent()

        RandomAccessFile randomAccessFile = new RandomAccessFile(parent + USERS_FILE_PATH,"rw");
        String usersFileString = randomAccessFile.readLine()
        randomAccessFile.close()

        randomAccessFile = new RandomAccessFile(parent + GROUPS_FILE_PATH,"rw");
        String groupFileString = randomAccessFile.readLine()
        randomAccessFile.close()

        //Чтение групп
        String[] groupRecords = groupFileString.split(NEWLINE);
        for(String tmp : groupRecords){
            String[] fields = tmp.split(" ");
            short grid = (short) (fields[0].getBytes()[0]<<8 | fields[0].getBytes()[1]);
            String groupName = fields[1];
            Group group = new Group(grid,groupName);
            _groupManager.add(group);
        }
        //Чтение пользователей
        String[] userRecords = usersFileString.split(NEWLINE);
        for(String tmp : userRecords){
            String[] fields = tmp.split(" ");
            short uid = (short) (fields[0].getBytes()[0]<<8 | fields[0].getBytes()[1]);
            short grid = (short) (fields[1].getBytes()[0]<<8 | fields[1].getBytes()[1]);
            String username = fields[2];
            byte []passwd = fields[3].toCharArray() as byte[];
            User user = new User(uid,grid,username,passwd);
            if(uid == ROOT_USER_ID && grid == ROOT_GROUP_ID){
                rootUser = user;
            }
            _userManager.add(user);
            _groupManager.addUser(user.getGRID(),user);
        }

    }
    String[] getGroups(){
        return  _groupManager.getNames();
    }

    void CopyFile(String pathFrom, String pathTo){
        byte[] data = kernel.readFile(pathFrom);
        DirectoryCluster directory = (DirectoryCluster)kernel.OpenDirectory(pathTo);
        String fileFullName = Utils.getFileName(pathFrom);
        String path = pathTo + "/" + fileFullName;
        if(directory.find(fileFullName) == null){
            kernel.CreateFile(path);
            kernel.writeFile(path,data);
        }
    }
    public boolean Login(String usr, String pwd){
        User user = _userManager.findUser(usr);
        if(user == null)
            return false;
        byte [] firstHash = user.getPassword()
        byte [] secondHash = DigestUtils.md5(pwd);
        if(firstHash == secondHash){
            this.user = user;
            loggedIn = true;
            return true;
        }
        return false;
    }
    boolean Logout() throws IOException {
        Flush();
        this.user = rootUser;
        loggedIn = false;
        return true;
    }

    public boolean checkUser(String usr){
        return  _userManager.findUser(usr) == null;
    }


    void Flush() throws IOException {

        ByteArrayBuffer buffer = new ByteArrayBuffer()
        for(User user : _userManager.getUsers()){
            buffer.write(ByteBuffer.allocate(2).putShort(user.getUID()).array())
            buffer.write(new String(" ").getBytes())
            buffer.write(ByteBuffer.allocate(2).putShort(user.getGRID()).array())
            buffer.write(new String(" ").getBytes())
            buffer.write(new String(user.getUsername()).getBytes())
            buffer.write(new String(" ").getBytes())
            buffer.write(user.getPassword())
            buffer.write(NEWLINE.getBytes())
        }

        def file = kernel.getFile()
        String parent = file.getParent()


        if(!new File(parent + USERS_FILE_PATH).exists()){
            new File(parent + "/groovyos").mkdir()
            new File(parent+USERS_FILE_PATH).createNewFile()
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(parent + USERS_FILE_PATH,"rw");
        randomAccessFile.write(buffer.getRawData()[0..buffer.size()-1] as byte[]);
        randomAccessFile.close();

        //Groups

        buffer = new ByteArrayBuffer()
        for(Group group :  _groupManager.getGroups()){
            buffer.write(ByteBuffer.allocate(2).putShort(group.getGRID()).array())
            buffer.write(new String(" ").getBytes())
            buffer.write(new String(group.getGroupname()).getBytes())
            buffer.write(new String(" ").getBytes())
            buffer.write(NEWLINE.getBytes());
        }

        if(!new File(parent + GROUPS_FILE_PATH).exists()){
            new File(parent+GROUPS_FILE_PATH).createNewFile()
        }
        randomAccessFile = new RandomAccessFile(parent + GROUPS_FILE_PATH,"rw");
        randomAccessFile.write(buffer.getRawData()[0..buffer.size()-1] as byte[]);
        randomAccessFile.close();
    }
    List<String> openDirectory(String path){
        kernel.OpenDirectory(path).list()
    }
}
