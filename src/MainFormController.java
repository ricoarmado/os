import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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
    private DirectoryCluster toCopy = null;
    //END
    boolean checkUser(String usr, String pwd) {
        return grOS.checkUser(usr);
    }

    boolean login(String usr,String pwd){
        if(!grOS.Login(usr, pwd)){
            return false;
        }
        buildNode();
        return true;
    }

    @FXML
    private MenuItem copyMenuItem;

    @FXML
    private TreeView<String> fileSystemTreeView;

    @FXML
    private Label systemStatusLabel;

    @FXML
    void changeUserMenuItem_Click(ActionEvent event) {
        try {
            grOS.Logout();
            login();
        } catch (IOException e) {
            errorMessage(e.getMessage(),AlertType.ERROR);
        }
    }

    @FXML
    void usersMenuItem_Click(ActionEvent event) {//Пользователи...
        try {
            List<String> groupList = grOS.getGroupList();
            List<String> userList = grOS.getUserList();
            AnchorPane pane = new AnchorPane();
            pane.setPrefHeight(500);
            pane.setPrefWidth(800);
            HBox box = new HBox();
            ListView<String> listView = new ListView<>();
            listView.getItems().addAll("Пользователи","Группы");
            listView.getSelectionModel().select(0);
            Separator separator = new Separator(Orientation.VERTICAL);
            ListView<String>tableView = new ListView<>();
            tableView.getItems().setAll(userList);
            tableView.getSelectionModel().select(0);
            HBox.setHgrow(tableView,Priority.ALWAYS);
            listView.setOnMouseClicked(event1 -> {
                String selectedItem = listView.getSelectionModel().getSelectedItem();
                if(selectedItem == "Пользователи"){
                    tableView.getItems().setAll(userList);
                }
                else {
                    tableView.getItems().setAll(groupList);
                }
            });

            box.getChildren().addAll(listView,separator,tableView);
            AnchorPane.setBottomAnchor(box,0.0);
            AnchorPane.setLeftAnchor(box,0.0);
            AnchorPane.setRightAnchor(box,0.0);
            AnchorPane.setTopAnchor(box,0.0);
            pane.getChildren().add(box);

            ButtonType addUser = new ButtonType("Добавить пользователя", ButtonBar.ButtonData.APPLY);
            ButtonType addGroup = new ButtonType("Добавить группу", ButtonBar.ButtonData.APPLY);
            ButtonType cancel = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);


            Dialog<String>dialog = new Dialog<>();
            dialog.getDialogPane().setContent(pane);
            dialog.getDialogPane().getButtonTypes().addAll(addUser,addGroup,cancel);
            dialog.setResultConverter(button -> {
                if(button == addUser){
                    String s = this.addUserDialog(FXCollections.observableArrayList(groupList));
                    if(s != null){
                        String[] split = s.split(" | ");
                        if(userList.contains(split[0])){
                            Platform.runLater(()->errorMessage("Данное имя уже существует",AlertType.ERROR));
                            return null;
                        }else {
                            return "adduser" +  " | " + s;
                        }
                    }
                    else return null;
                }else if(button == addGroup){
                    String s = this.addGroupDialog(FXCollections.observableArrayList(groupList));
                    if(s != null){
                        return "addgroup" + " | " + s;
                    }
                    else return null;
                }else return null;
            });

            Optional<String> s = dialog.showAndWait();
            if(s.isPresent()){
                String[] tmp = s.get().split(" | ");
                if(tmp[0].equals("adduser")){
                    grOS.addUser(tmp[2],tmp[4],tmp[6]);
                }else if(tmp[0].equals("addgroup")){

                }
            }
            System.out.println();

        }catch (Exception ex){
            errorMessage(ex.getMessage(),AlertType.ERROR);
        }

    }


    @FXML
    void exitMenuItem_Click(ActionEvent event) {
        grOS.flush();
        try {
            grOS.Flush();
        } catch (IOException e) {
            e.getMessage();
        }
        Runtime.getRuntime().exit(0);
    }

    @FXML
    void createDirMenuItem_Click(ActionEvent event) { // Создание каталога
        TreeItem<String> parent = this.fileSystemTreeView.getSelectionModel().getSelectedItem();
        if(parent != null){
            String path = getPath(parent);
            if(path == "/")
                path = "";
            String name = editDialog();
            if(name != null && !name.contains("./")){
                if(parent.getParent() != null && parent.getParent().getChildren().filtered(p-> p.getValue().equals(name)).size() != 0){
                    errorMessage("Файл с таким названием уже есть",AlertType.ERROR);
                    return;
                }
                grOS.createDirectory(path + "/" + name);
                    TreeItem<String> item = new TreeItem<>(name);
                    parent.getChildren().add(item);
            }
            else errorMessage("Имя каталога не задано или содержит недопустимые символы",AlertType.ERROR);
        }
        else errorMessage("Выберите каталог", AlertType.ERROR);
    }

    @FXML
    void createFileMenuItem_Click(ActionEvent event) {//Создание файла
        TreeItem<String> parent = this.fileSystemTreeView.getSelectionModel().getSelectedItem();
        if(parent != null){
            String path = getPath(parent);
            if(path == "/")
                path = "";
            String name = editDialog();
            if(name != null && !name.contains("/")){
                if(parent.getParent().getChildren().filtered(p-> p.getValue().equals(name)).size() != 0){
                    errorMessage("Файл с таким названием уже есть",AlertType.ERROR);
                    return;
                }
                grOS.createFile(path + "/" + name);
                TreeItem<String> item = new TreeItem<>(name);
                parent.getChildren().add(item);
            }
            else errorMessage("Имя файла не задано или содержит недопустимые символы",AlertType.ERROR);
        }
        else errorMessage("Выберите каталог", AlertType.ERROR);
    }

    @FXML
    void openMenuItem_Click(ActionEvent event) {// чтение файла
        TreeItem<String> parent = this.fileSystemTreeView.getSelectionModel().getSelectedItem();
        if(parent != null){
            String path = getPath(parent);
            if(path == "/")
                path = "";
            DirectoryCluster directoryCluster = grOS.openDirectory(path);
            if(!directoryCluster.isDir()){
               byte[] tmp = grOS.openfile(path);
                byte[] bytes = openFile(new String(tmp).trim());
                if(bytes != null){
                    grOS.writeFile(path,bytes);
                }
            }
            else errorMessage("Это не файл",AlertType.ERROR);
        }
        else errorMessage("Выберите файл", AlertType.ERROR);
    }

    @FXML
    void copyMenuItem_Click(ActionEvent event) {
        TreeItem<String> parent = this.fileSystemTreeView.getSelectionModel().getSelectedItem();
        if(parent != null){
            String path = getPath(parent);
            if(path == "/")
                path = "";
            toCopy = grOS.openDirectory(path);
            copyMenuItem.setDisable(false);
        }
        else errorMessage("Выберите каталог", AlertType.ERROR);
    }


    @FXML
    void pasteMenuItem_Click(ActionEvent event) {
        if(toCopy != null){
            TreeItem<String> parent = this.fileSystemTreeView.getSelectionModel().getSelectedItem();
            if(parent != null){
                String path = getPath(parent);
                if(path == "/")
                    path = "";
                DirectoryCluster directoryCluster = grOS.openDirectory(path);
                if(directoryCluster.isDir()){
                    grOS.copyMetaFile(toCopy,path);
                    buildNode();
                }
                else errorMessage("Это не каталог",AlertType.ERROR);
            }
            else errorMessage("Выберите каталог", AlertType.ERROR);
            copyMenuItem.setDisable(true);
            toCopy = null;
        }
        else errorMessage("Буфер обмена пуст", AlertType.INFORMATION);
    }

    @FXML
    void delMenuItem_Click(ActionEvent event) {

    }

    @FXML
    void renameMenuItem_Click(ActionEvent event) {
        TreeItem<String> parent = this.fileSystemTreeView.getSelectionModel().getSelectedItem();
        if(parent != null){
            String path = getPath(parent);
            if(path == "/")
                path = "";
            DirectoryCluster directoryCluster = grOS.openDirectory(path);
            if(directoryCluster != null){
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("GROOVYOS");
                dialog.setHeaderText("ПЕРЕИМЕНОВАНИЕ ФАЙЛА");
                dialog.setContentText("Пожалуйста введите имя:");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    String newpath = getPath(parent.getParent()) + "/" +result.get();
                    if(parent.getParent().getChildren().filtered(p-> p.getValue().equals(result.get())).size() != 0){
                        errorMessage("Файл с таким названием уже есть",AlertType.ERROR);
                        return;
                    }
                    grOS.rename(directoryCluster,newpath);
                    grOS.readDir();
                    buildNode();
                }
            }
            else errorMessage("Это не файл",AlertType.ERROR);
        }
        else errorMessage("Выберите файл", AlertType.ERROR);
    }


    @FXML
    void propertyMenuItem_Click(ActionEvent event) { // Свойства
        TreeItem<String> parent = this.fileSystemTreeView.getSelectionModel().getSelectedItem();
        if(parent != null){
            String path = getPath(parent);
            if(path == "/")
                path = "";
            DirectoryCluster directoryCluster = grOS.openDirectory(path);
            AccessRights accessRights = new AccessRights((short) directoryCluster.getChmod());
            PropertyMap map = new PropertyMap(accessRights.getUser().getCanRead(),accessRights.getUser().getCanWrite(),accessRights.getUser().getCanExecute(),
                    accessRights.getGroup().getCanRead(),accessRights.getGroup().getCanWrite(),accessRights.getGroup().getCanExecute(),
                    accessRights.getOthers().getCanRead(),accessRights.getOthers().getCanWrite(),accessRights.getOthers().getCanExecute(),
                    directoryCluster.getHiden(),directoryCluster.getSystem(),directoryCluster.getReadonly());
            map = property(map,new String(directoryCluster.getCreateDate()).trim(),new String(directoryCluster.getEditDate()).trim(),directoryCluster.getDir());
            if(map == null)
                return;
            grOS.setAttributes(directoryCluster,map);
        }
        else errorMessage("Выберите каталог", AlertType.ERROR);
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
                    buildNode();
                }
                } catch (IOException e) {
                    Runtime.getRuntime().exit(0);
                }

        }catch (Exception ex){
            new Alert(AlertType.ERROR,ex.getMessage());
        }

    }
    private void buildNode(){
        TreeItem<String>metaFileNode = new TreeItem<>("/");
        DirectoryCluster cluster = grOS.openDirectory("/");
        TreeItem<String> item = buildFromItem(metaFileNode, cluster);
        this.fileSystemTreeView.setRoot(item);
    }


    private TreeItem<String>buildFromItem(TreeItem<String>item,DirectoryCluster cluster){
        for(DirectoryCluster tmp: cluster.getDirectories()){
            String[] split = new String(tmp.getFileName()).trim().split("/");
            TreeItem<String>item1 = new TreeItem<>(split[split.length - 1]);
            item1 = buildFromItem(item1,tmp);
            item.getChildren().add(item1);
        }
        return item;
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
    private PropertyMap property(PropertyMap map, String createDate, String editDate, boolean dir){
        Dialog<PropertyMap>dialog = new Dialog<>();
        dialog.setTitle("GROOVYOS");
        dialog.setHeaderText("Свойства документа");

        ButtonType okButton = new ButtonType("Ок", ButtonBar.ButtonData.OK_DONE);
        ButtonType close = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton,close);
        TabPane pane = new TabPane();

        Tab tab1 = new Tab("Права доступа");
        FlowPane flow1 = new FlowPane();
        flow1.setVgap(20);
        flow1.setHgap(500);
        CheckBox box = new CheckBox("Чтение");
        box.setSelected(map.isUr());
        CheckBox box2 = new CheckBox("Запись ");
        box2.setSelected(map.isUw());
        CheckBox box3 = new CheckBox("Исполнение");
        box3.setSelected(map.isUx());
        CheckBox box4 = new CheckBox("Чтение");
        box4.setSelected(map.isGr());
        CheckBox box5 = new CheckBox("Запись");
        box5.setSelected(map.isGw());
        CheckBox box6 = new CheckBox("Исполнение");
        box6.setSelected(map.isGx());
        CheckBox box7 = new CheckBox("Чтение");
        box7.setSelected(map.isOr());
        CheckBox box8 = new CheckBox("Запись");
        box8.setSelected(map.isOw());
        CheckBox box9 = new CheckBox("Исполнение");
        box9.setSelected(map.isOx());
        flow1.getChildren().addAll(new Label("Пользователь"),box,box2,box3,new Label("Группа"),box4,box5,box6,
                new Label("Другие"),box7,box8,box9);

        tab1.setContent(flow1);
        Tab tab2 = new Tab("Свойства");
        FlowPane flow2 = new FlowPane();
        flow2.setVgap(20);
        flow2.setHgap(500);
        CheckBox _box = new CheckBox("Системный");
        _box.setSelected(map.isSystem());
        CheckBox _box2 = new CheckBox("Скрытый");
        _box2.setSelected(map.isHidden());
        CheckBox _box3 = new CheckBox("Только для чтения");
        _box3.setSelected(map.isReadonly());
        String type = dir? "Каталог" : "Файл";
        StringBuilder sb = new StringBuilder(createDate);
        sb.insert(4,'.');
        sb.insert(7,'.');
        createDate = sb.toString();
        sb = new StringBuilder(editDate);
        sb.insert(4,'.');
        sb.insert(7,'.');
        editDate = sb.toString();
        flow2.getChildren().addAll(new Label("Тип документа"),new Label(type),new Label("Дата создания"),
                new Label(createDate), new Label("Дата изменения"),new Label(editDate),_box,_box2,_box3);
        tab2.setContent(flow2);
        pane.getTabs().addAll(tab1,tab2);
        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(button->{
            if(button == okButton){
                return new PropertyMap(box.isSelected(),box2.isSelected(),box3.isSelected(),box4.isSelected(),box5.isSelected(),
                        box6.isSelected(),box7.isSelected(),box8.isSelected(),box9.isSelected(),
                        _box2.isSelected(),_box.isSelected(),_box3.isSelected());
            }
            return null;
        });
        Optional<PropertyMap> propertyMap = dialog.showAndWait();
        if(propertyMap.isPresent()){
            return propertyMap.get();
        }
        return null;
    }
    private byte[] openFile(String str){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("GROOVYOS");
        dialog.setHeaderText("Правка документа");
        ButtonType okButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType close = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton,close);
        FlowPane pane = new FlowPane();
        pane.setHgap(500);
        pane.setVgap(500);
        TextArea area = new TextArea(str);
        pane.getChildren().addAll(area);
        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(button->{
            if(button == okButton){
                return area.getText();
            }else return null;
        });
        Optional<String> s = dialog.showAndWait();
        if(s.isPresent()){
            return s.get().getBytes();
        }
        else return null;
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
    private String getPath(TreeItem<String> parent) {
        String tmp = "";
        if(parent.getParent() != null){
            tmp = getPath(parent.getParent());
            return  tmp + "/" + parent.getValue();
        }
        else return "";
    }
    private String editDialog(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("GROOVYFS");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setHeaderText("Для создания файла/каталога необходимо ввести его название");
        dialog.setContentText("Пожалуйста, введите имя:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            return result.get();
        }
        else return null;
    }
    private String addUserDialog(ObservableList<String> groupNames){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("GROOVYFS");
        dialog.setHeaderText("Добавление пользователя");
        ButtonType okButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType close = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton,close);
        FlowPane pane = new FlowPane();
        pane.setHgap(80);
        pane.setVgap(10);
        TextField area = new TextField();
        area.setPromptText("например, admin");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("например, password");
        ComboBox<String>comboBox = new ComboBox<>(groupNames);
        comboBox.getSelectionModel().select(0);
        pane.getChildren().addAll(new Label("Логин"),area,new Label("Пароль"),passwordField,new Label("Группа"),comboBox);
        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(button->{
            if(button == okButton){
                return area.getText() + " | " + passwordField.getText() + " | " + comboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        if(result.isPresent()){
            return result.get();
        }
        return null;
    }
    private String addGroupDialog(ObservableList<String> groupNames){
        TextInputDialog dialog = new TextInputDialog("name");
        dialog.setTitle("GROOVYFS");
        dialog.setHeaderText("Добавление группы");
        dialog.setContentText("Пожалуйста введите название группы:");
        Optional<String> result = dialog.showAndWait();
        if(result.isPresent()){
            String res = result.get();
            if(groupNames.contains(res)){
                Platform.runLater(()->errorMessage("Группа с таким именем уже существует",AlertType.ERROR));
                return null;
            }
            else return res;
        }
        return null;
    }
}
