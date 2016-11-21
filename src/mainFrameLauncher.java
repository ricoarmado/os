/**
 * Created by stanislavtyrsa on 21.11.16.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class mainFrameLauncher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("frame.fxml"));
        loader.setController(MainFormController.getInstance());
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
