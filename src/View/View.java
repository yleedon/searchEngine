package View;


import Model.Parse;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class View {
    public TextField fld_text;
    public Button btn_testParse;



    public void testParse() {
        if ((fld_text.getText().equals("")))
            return;

        Parse parser = new Parse(fld_text.getText());
        try {
            parser.parse();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(parser.toString());
        a.showAndWait();


    }

}
