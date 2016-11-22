import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
public class launcher {
    public static void main(String[] args){
        try {
            File file = new File("settings.json");
            if(!file.exists()){
                Format f = new Format();
                f.main(null);
            }
            else {
                mainFrameLauncher.main(null);
            }

        } catch (Exception e) {
            Format f = new Format();
            f.main(null);
        }

    }
}
