import javax.swing.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;


import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

public class Format extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private FSImage image;
    public Format() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mainFrameLauncher.main(null);
                    }
                });
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите директорию для размещения системных файлов");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showDialog(null,"Открыть файл") == JFileChooser.APPROVE_OPTION) textField1.setText(fileChooser.getSelectedFile().getParent());
        else textField1.setText("");
        //dispose();
    }

    private void onCancel() {
        image = new FSImage();
        image.export(textField1.getText() + "/image");
        JSONObject object = new JSONObject();
        object.put("image",textField1.getText() + "/image");
        object.put("path",textField1.getText());
        object.put("systemPath", textField1.getText() + "/groovyos");
        object.put("users", textField1.getText() + "/groovyos/users");
        object.put("groups", textField1.getText() + "/groovyos/groups");
        try(FileWriter fileWriter = new FileWriter("settings.json")) {
            fileWriter.write(object.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //dispose();

    }

    public static void main(String[] args) {
        Format dialog = new Format();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
