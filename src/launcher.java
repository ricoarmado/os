import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

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
                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(new FileReader(file));
                String image = (String)object.get("image");

                System.out.println();
            }

        } catch (Exception e) {
            Format f = new Format();
            f.main(null);
        }

    }
}
