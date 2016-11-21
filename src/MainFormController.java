import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by stanislavtyrsa on 30.10.16.
 */

public class MainFormController implements Initializable {
    private Sys grOS = null;
    //Singleton
    private static MainFormController instance;
    public static MainFormController getInstance(){
        if(instance == null){
            instance = new MainFormController();
        }
        return instance;
    }
    private MainFormController(){}
    //END
    boolean checkUser(String usr, String pwd) {
        return grOS.checkUser(usr);
    }
    boolean addUser(String usr,String pwd){
        if(!checkUser(usr, pwd)){
            return false;
        }
        return true;
    }
    boolean login(String usr,String pwd){
        grOS.Login(usr, pwd);
        return true;
    }
    @FXML
    private TreeView<String> fileSystemTreeView;

    @FXML
    private Label systemStatusLabel;

    @FXML
    void changeUserMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void usersMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void user(ActionEvent event) {

    }

    @FXML
    void exitMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void createDirMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void createFileMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void openMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void copyMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void pasteMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void delMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void renameMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void propertyMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void formatMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void plannerMenuItem_Click(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
         grOS = new Sys();
         try {
        if(grOS.init()){
           grOS.Initialize();
           Login login = new Login();
           login.run(false);
        }
        } catch (IOException e) {
            e.printStackTrace();
         }
    }
}
