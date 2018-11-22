package View;


import Model.Indexer;
import Model.MyDocument;
import Model.Parse;
import Model.ReadFile;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class View {
    //    public ProgressBar progressBar;
    public TextField fld_text;
    public TextField fld_path;
    public Button btn_testParse;
    public Button btn_runTests;
    public CheckBox btn_stemmingBox;
    public TextField fld_outputPath;
    public TextField fld_corpusPath;
    private String corpusPath;
    //    public ReadFile rf;
    public Button btn_testIndexer;
    public Button btn_corpusBrowse;
    public Button btn_outputBrowse;

    public void testParse() {
        fld_text.setOpacity(0.3);
        if ((fld_text.getText().equals("")))
            return;

        Parse parser = new Parse(fld_corpusPath.getText(),fld_text.getText(),btn_stemmingBox.isSelected());
        try {
            parser.parse();
            parser.printIndex();
            System.out.println("Number set: ");
            parser.printNumberSet();
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

    //parser auto test
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
            Parse parser = new Parse(fld_corpusPath.getText(),"",btn_stemmingBox.isSelected());
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
                a.setContentText("it works!! it actually works!!");
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

    //<editor-fold desc="ReadFile testing">



    private void testReadFileAlert(String docId, String txt){
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("The Text is:");
        a.setHeaderText(docId);
        a.setContentText(txt);
        a.show();
    }
    //</editor-fold>

    public void textPress(){
        fld_text.setOpacity(1);
    }

    public void onSearchPressed(KeyEvent event){
        fld_text.setOpacity(1);
        if(event!=null && event.getCode().getName().equals("Enter"))
            testParse();
    }

    public void testIndexer(){
        long end;
        long start = System.nanoTime();

        testReadFile();

        end = System.nanoTime();
        long time = (end-start)/1000000;
        System.out.println("total index time:  "+ time);
//        Indexer indexer = new Indexer(rf);
//        indexer.parse();

    }


    public void testReadFile() {
        ReadFile readF = new ReadFile(fld_corpusPath.getText(),fld_outputPath.getText(),btn_stemmingBox.isSelected());

//        ReadFile rf = new ReadFile("C:\\Users\\Dan\\Desktop\\corpus");
        readF.readDirectory();
        readF.reset();
        readF=null;
        Runtime r = Runtime.getRuntime();
        r.gc();

//        if(!fld_path.getText().equals(""))
//            testGetDoc(fld_path.getText());
    }


    public void testGetDoc(){
        try {


            ReadFile rf;
            rf = new ReadFile(fld_corpusPath.getText(),fld_outputPath.getText(),btn_stemmingBox.isSelected());
            if(!fld_path.getText().equals("")) {

//            int iDoc = Integer.valueOf(fld_path.getText());
                MyDocument document = rf.getDocument(fld_path.getText());// ("LA122790-0222");
                System.out.println(document.getTxt());
//            testReadFileAlert(document.getDocId(), document.getTxt());
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * 
     * @param event
     */
    public void browse(ActionEvent event){

        DirectoryChooser chooser = new DirectoryChooser();
        Node node = (Node) event.getSource() ;
        String data = (String) node.getUserData();
        int button = Integer.parseInt(data);
        if(button==1) {// corpusBrowse
            chooser.setTitle("SELECT CORPUS DIRECTORY");
            File defaultDirectory = new File("C:\\Users\\Yaniv\\Desktop\\searchproject\\searchEngine\\data\\corpus");
            chooser.setInitialDirectory(defaultDirectory);
        }
        if(button==2) {// outputBrowse
            chooser.setTitle("SELECT OUTPUT DIRECTORY");
            File defaultDirectory = new File("C:\\Users\\Yaniv\\Desktop\\searchproject\\searchEngine\\data");
            chooser.setInitialDirectory(defaultDirectory);
        }

        File selectedDirectory = chooser.showDialog(new Stage());
        if(selectedDirectory==null)
            return;
        if(button==1)
            fld_corpusPath.setText(selectedDirectory.getPath());
        if(button==2)
            fld_outputPath.setText(selectedDirectory.getPath());
    }
}
