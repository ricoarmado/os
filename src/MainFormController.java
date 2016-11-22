import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

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
    static MainFormController getInstance(){
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
        ////////////////////ДОПИСАТЬ
        return true;
    }
    boolean login(String usr,String pwd){
        if(!grOS.Login(usr, pwd)){
            return false;
        }
        buildNode(null);
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
             else {
                 buildNode(null);
             }
        } catch (IOException e) {
            e.printStackTrace();
         }
    }
    private void buildNode(TreeItem<String>metaFileNode){
        if(metaFileNode == null){
            metaFileNode = new TreeItem<>("/");
        }
        if(metaFileNode.getValue().equals("<пусто>")){
            return;
        }
        for (String s : grOS.openDirectory(metaFileNode.getValue())) {
            TreeItem<String> tmp = new TreeItem<>(s);
            tmp.addEventHandler(MouseEvent.MOUSE_CLICKED,event -> {
                if(event.getClickCount() == 2){
                    new Alert(Alert.AlertType.CONFIRMATION,"2 раза").show();
                }
                else {
                    new Alert(Alert.AlertType.INFORMATION,"1 раз").show();
                }
            });
            metaFileNode.getChildren().add(tmp);
        }
        if(metaFileNode.getChildren().size() == 0){
            metaFileNode.getChildren().add(new TreeItem<>("<пусто>"));
        }

        this.fileSystemTreeView.setRoot(metaFileNode);
    }
}
