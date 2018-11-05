package View;


import Model.MyDocument;
import Model.Parse;
import Model.ReadFile;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class View {
    public TextField fld_text;
    public TextField fld_path;
    public Button btn_testParse;
    public Button btn_runTests;
    public CheckBox btn_stemmingBox;

    public void testParse() {
        fld_text.setOpacity(0.3);
        if ((fld_text.getText().equals("")))
            return;

        Parse parser = new Parse(fld_text.getText(),btn_stemmingBox.isSelected());
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
            return;
        }
        try {
            String outPut;
            boolean failed = false;
            String ans = "Format: [input] != [wantedOutput] --> [actualOutput]\n\nResults:\n";
            Parse parser = new Parse("",btn_stemmingBox.isSelected());
            int i = 0;
            for (String input:tests.keySet()) {
                i++;
                parser.setTxt(input);
                parser.parse();

                if (!parser.toString().equals(""))
                    outPut = removBraces(parser.toString().substring(0,parser.toString().length()-1));
                else outPut = "";
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
                a.setAlertType(Alert.AlertType.ERROR);

            }
            else {
                a.setAlertType(Alert.AlertType.INFORMATION);
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

    public void testReadFile(){
//        ClassLoader classLoader = getClass().getClassLoader();
//        ReadFile rf = new ReadFile(classLoader.getResource("corpus").getFile());
        ReadFile rf = new ReadFile("C:\\Users\\Dan\\Desktop\\corpus");
//        rf.readDirectory();
        try {
            int iDoc = Integer.valueOf(fld_path.getText());
            MyDocument document= rf.getDocument("tst"+iDoc);
            testReadFileAlert(document.getDocId(), document.getTxt());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void testReadFileAlert(String docId, String txt){
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("The Text is:");
        a.setHeaderText(docId);
        a.setContentText(txt);
        a.show();
    }

    public void textPress(){
        fld_text.setOpacity(1);
    }

    public void onSearchPressed(KeyEvent event){
        fld_text.setOpacity(1);
        if(event!=null && event.getCode().getName().equals("Enter"))
            testParse();
    }

}
