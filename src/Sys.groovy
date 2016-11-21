

import Group
import GroupManager
import User
import UserManager
import org.apache.commons.codec.digest.DigestUtils
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Sys {
    final String SYSTEM_DIRECTORY_PATH = "/groovyos";
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
            kernel.OpenDirectory(SYSTEM_DIRECTORY_PATH);
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
            rootUser = root;
            user = root;
            loggedIn = true;
            Install();
            loggedIn = false;
            return false;
        }
        else{
            return true;
        }
    }
    public void Initialize() {
        byte [] usersFileData = kernel.readFile(USERS_FILE_PATH);
        byte [] groupsFileData = kernel.readFile(GROUPS_FILE_PATH);

        String usersFileString = new String(usersFileData);
        String groupFileString = new String(groupsFileData);

        //Чтение групп
        String[] groupRecords = groupFileString.split(NEWLINE);
        for(String tmp : groupRecords){
            String[] fields = tmp.split(" ");
            short grid = Short.parseShort(fields[0]);
            String groupName = fields[1];
            Group group = new Group(grid,groupName);
            _groupManager.add(group);
        }
        //Чтение пользователей
        String[] userRecords = usersFileString.split(NEWLINE);
        for(String tmp : userRecords){
            String[] fields = tmp.split(" ");
            short uid = Short.parseShort(fields[0]);
            short grid = Short.parseShort(fields[1]);
            String username = fields[2];
            byte []passwd = fields[3].getBytes();
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
    void Install() throws IOException {
        kernel.CreateDirectory(SYSTEM_DIRECTORY_PATH);
        kernel.setAttributes(SYSTEM_DIRECTORY_PATH,new Attributes(false,true,false));
        kernel.CreateFile(USERS_FILE_PATH);
        kernel.setAttributes(USERS_FILE_PATH, new Attributes(false,true,false));
        StringBuffer buffer = new StringBuffer();
        char [] usersFileData;
        for(User user : _userManager.getUsers()){
            buffer.append(user.getUID());
            buffer.append(' ');
            buffer.append(user.getGRID());
            buffer.append(' ');
            buffer.append(user.getUsername());
            buffer.append(' ');
            buffer.append(user.getPassword());
            buffer.append(NEWLINE);
        }
        usersFileData = buffer.toString().toCharArray();
        RandomAccessFile randomAccessFile = new RandomAccessFile(USERS_FILE_PATH,"w");
        randomAccessFile.write(new String(usersFileData).getBytes());
        randomAccessFile.close();
        //Groups;
        kernel.CreateFile(GROUPS_FILE_PATH);
        kernel.setAttributes(GROUPS_FILE_PATH,new Attributes(false,true,false));
        char [] groupsFileData;
        buffer = new StringBuffer();
        for(Group group :  _groupManager.getGroups()){
            buffer.append(group.getGRID());
            buffer.append(' ');
            buffer.append(group.getGroupname());
            buffer.append(NEWLINE);
        }
        groupsFileData = buffer.toString().toCharArray();
        randomAccessFile = new RandomAccessFile(GROUPS_FILE_PATH,"w");
        randomAccessFile.write(new String(groupsFileData).getBytes());
        randomAccessFile.close();
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
        byte [] firstHash = new String(user.getPassword()).getBytes();
        byte [] secondHash = DigestUtils.md5(pwd);
        if(firstHash.equals(secondHash)){
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
        StringBuffer buffer = new StringBuffer();
        char [] usersFileData;
        for(User user : _userManager.getUsers()){
            buffer.append(user.getUID());
            buffer.append(' ');
            buffer.append(user.getGRID());
            buffer.append(' ');
            buffer.append(user.getUsername());
            buffer.append(' ');
            buffer.append(user.getPassword());
            buffer.append(NEWLINE);
        }
        usersFileData = buffer.toString().toCharArray();
        RandomAccessFile randomAccessFile = new RandomAccessFile(USERS_FILE_PATH,"w");
        randomAccessFile.write(new String(usersFileData).getBytes());
        randomAccessFile.close();
        //Groups
        kernel.CreateFile(GROUPS_FILE_PATH);
        kernel.setAttributes(GROUPS_FILE_PATH,new Attributes(false,true,false));
        char [] groupsFileData;
        buffer = new StringBuffer();
        for(Group group :  _groupManager.getGroups()){
            buffer.append(group.getGRID());
            buffer.append(' ');
            buffer.append(group.getGroupname());
            buffer.append(NEWLINE);
        }
        groupsFileData = buffer.toString().toCharArray();
        randomAccessFile = new RandomAccessFile(GROUPS_FILE_PATH,"w");
        randomAccessFile.write(new String(groupsFileData).getBytes());
        randomAccessFile.close();
    }
}
