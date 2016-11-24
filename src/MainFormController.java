import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by stanislavtyrsa on 30.10.16.
 */

public class MainFormController implements Initializable {
    private Sys grOS = null;
    //Singleton
    private static MainFormController instance;
    private Stage stage;

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
        try {
            File file = new File("settings.json");
            if (!file.exists()) {//Форматирование
                format();
            }
            grOS = new Sys();
            try {
                if(grOS.init()){
                    grOS.Initialize();
                    login();
                }
                else {
                    buildNode(null);
                }
                } catch (IOException e) {
                    Runtime.getRuntime().exit(0);
                }

        }catch (Exception ex){
            new Alert(AlertType.ERROR,ex.getMessage());
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
                    errorMessage("2 раза", AlertType.INFORMATION);
                }
                else {
                    errorMessage("1 раз", AlertType.INFORMATION);
                }
            });
            metaFileNode.getChildren().add(tmp);
        }
        if(metaFileNode.getChildren().size() == 0){
            metaFileNode.getChildren().add(new TreeItem<>("<пусто>"));
        }

        this.fileSystemTreeView.setRoot(metaFileNode);
    }
    private void format() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Форматирование ФС");
        dialog.setHeaderText("Пожалуйста, нажмите кнопку \"Форматировать\" и выберите каталог для размещения ФС");

        ButtonType loginButtonType = new ButtonType("Форматировать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType);
        dialog.setResultConverter(dialogButton -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File file = chooser.showDialog(new Stage());
            return file.getPath();
        });
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String s = result.get();
            FSImage image = new FSImage();
            image.export(s + "/image");
            JSONObject object = new JSONObject();
            object.put("image",s + "/image");
            object.put("path",s);
            object.put("systemPath", s + "/groovyos");
            object.put("users", s + "/groovyos/users");
            object.put("groups", s + "/groovyos/groups");
            try(FileWriter fileWriter = new FileWriter("settings.json")) {
                fileWriter.write(object.toJSONString());
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void login() throws IOException {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("GROOVYFS");
        dialog.setHeaderText("АВТОРИЗАЦИЯ");
        //dialog.setOnCloseRequest(event -> Runtime.getRuntime().exit(0));
        ButtonType loginButtonType = new ButtonType("Логин", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("например, root:");
        PasswordField password = new PasswordField();
        password.setPromptText("например, password:");

        grid.add(new Label("Имя пользователя"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Пароль"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(username::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            else return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        if(result.isPresent()){
            Pair<String, String> usernamePassword = result.get();
            if(MainFormController.getInstance().login(usernamePassword.getKey(),usernamePassword.getValue())){
                errorMessage("Вы вошли",AlertType.INFORMATION);
            }
            else {
                errorMessage("Ошибка",AlertType.ERROR);
                throw new IOException();
            }
        }
        else {
            errorMessage("Ошибка",AlertType.ERROR);
            throw new IOException();
        }


    }
    private void errorMessage(String message, AlertType type){
        String headerText;
        switch (type){
            case CONFIRMATION: headerText = "Системное сообщение:"; break;
            case  ERROR: headerText = "Системная ошибка:"; break;
            default: headerText="Сообщение:"; break;
        }
        Alert alert = new Alert(type);
        alert.setTitle("GROOVYFS");
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
