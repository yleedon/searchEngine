package View;


import Model.Parse;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class View {
    public TextField fld_text;
    public Button btn_testParse;
    public Button btn_runTests;



    public void testParse() {
        if ((fld_text.getText().equals("")))
            return;

        Parse parser = new Parse(fld_text.getText());
        try {
            parser.parse();
            parser.printIndex();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = a.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("Alert.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");
        a.setContentText(parser.toString());
        a.showAndWait();

    }


    //for the test button
    public void runTests(){

        Map<String,String> tests = new HashMap<>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("parseTests.txt").getFile());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] cut;
            while ((line = br.readLine()) != null) {
                if(line.contains("=") && line.charAt(0) != '#') {
                    if (line.contains("#"))
                        line = line.substring(0,line.indexOf("#")-1);
                    cut = line.split("=");
                    tests.put(cut[0], cut[1]);
                }
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("idiot! the test file is bad!!");
        }




        try {
            String outPut;
            boolean failed = false;
            String ans = "Format: [input] != [wantedOutput] --> [actualOutput]\n\nResults:\n";
            Parse parser = new Parse("");
            int i = 0;
            for (String input:tests.keySet()) {
                i++;
                parser.setTxt(input);
                parser.parse();
                outPut = removBraces(parser.toString().substring(0,parser.toString().length()-1));
                if(!outPut.equals(tests.get(input))) {
                    failed = true;
                    ans +=  "Test(" + i + "): [" + input + "] != [" + tests.get(input)+"] --> [" + outPut+"]\n";
                }

            }



            Alert a = new Alert(Alert.AlertType.INFORMATION);
            DialogPane dialogPane = a.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("Alert.css").toExternalForm());
            dialogPane.getStyleClass().add("myDialog");

            if (failed) {
                a.getDialogPane().setMinWidth(800);
                a.setContentText(ans);
                a.setHeaderText("The following tests have failed:");
                a.setTitle("FAIL!!!");

            }
            else {
                a.getDialogPane().setMinWidth(300);
                a.setTitle("SUCCESS!");
                a.setContentText("YEY!!");
                a.setHeaderText("All tests passed!!");

            }
            a.showAndWait();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private String removBraces(String s){
        s = s.replace("{","");
        s = s.replace("}","");
        return s;
    }

}
