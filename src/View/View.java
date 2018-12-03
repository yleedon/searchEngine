package View;


import Model.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class View {
    //    public ProgressBar progressBar;
    public TextField fld_text;
    public TextField fld_path;
    public Button btn_testParse;
    public Button btn_runTests;
    public CheckBox btn_stemmingBox;
    public TextField fld_outputPath;
    public TextField fld_corpusPath;
    public Button btn_reset;
    private Map<String, DicEntry> dictianary;


    public View() {
        fld_outputPath = new TextField("d:\\documents\\users\\danavra\\Documents\\IR - output");
        fld_corpusPath = new TextField("d:\\documents\\users\\danavra\\Documents\\corpus");
    }

    public void testParse() {
        fld_text.setOpacity(0.3);
        if ((fld_text.getText().equals("")))
            return;

        Parse parser = new Parse(fld_corpusPath.getText(), fld_text.getText(), btn_stemmingBox.isSelected());
        try {
            parser.parse();
            parser.printIndex();
//            System.out.println("Number set: ");
//            parser.printNumberSet();
        } catch (Exception e) {
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
    public void runTests() {

        Map<String, String> tests = new HashMap<>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("parseTests.txt").getFile());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] cut;
            while ((line = br.readLine()) != null) {
                if (line.contains("=") && line.charAt(0) != '#') {
                    if (line.contains("#"))
                        line = line.substring(0, line.indexOf("#") - 1);
                    cut = line.split("=");
                    tests.put(cut[0], cut[1]);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("idiot! the test file is bad!!");
            return;
        }
        try {
            String outPut;
            boolean failed = false;
            String ans = "Format: [input] != [wantedOutput] --> [actualOutput]\n\nResults:\n";
            Parse parser = new Parse(fld_corpusPath.getText(), "", btn_stemmingBox.isSelected());
            int i = 0;
            for (String input : tests.keySet()) {
                i++;
                parser.setTxt(input,"");
                parser.parse();

                if (!parser.toString().equals(""))
                    outPut = removBraces(parser.toString().substring(0, parser.toString().length() - 1));
                else outPut = "";
                if (!outPut.equals(tests.get(input))) {
                    failed = true;
                    ans += "Test(" + i + "): [" + input + "] != [" + tests.get(input) + "] --> [" + outPut + "]\n";
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

            } else {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.getDialogPane().setMinWidth(300);
                a.setTitle("SUCCESS!");
                a.setContentText("it works!! it actually works!!");
                a.setHeaderText("All tests passed!!");

            }
            a.showAndWait();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String removBraces(String s) {
        s = s.replace("{", "");
        s = s.replace("}", "");
        return s;
    }

    //<editor-fold desc="ReadFile testing">

    //</editor-fold>

    public void textPress() {
        fld_text.setOpacity(1);
    }

    public void onSearchPressed(KeyEvent event) {
        fld_text.setOpacity(1);
        if (event != null && event.getCode().getName().equals("Enter"))
            testParse();
    }

    public void testIndexer() {
        File corpue = new File(fld_corpusPath.getText());
        File output = new File(fld_outputPath.getText());
        if (!corpue.exists() || !output.exists()) {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("ILLEGAL PATH");
            alert.setHeaderText("bad path given");
            alert.setContentText("please enter a valid path");
            alert.show();
            return;
        }
        long end;
        long start = System.nanoTime();

        int numOfDocsProcessed = testReadFile();

        end = System.nanoTime();
        long time = (end - start) / 1000000000;

        showIndexSummary(numOfDocsProcessed, time);
        System.out.println("total index time:  " + time);
//        Indexer indexer = new Indexer(rf);
//        indexer.parse();

    }


    public int testReadFile() {
        ReadFile readF = new ReadFile(fld_corpusPath.getText(), fld_outputPath.getText(), btn_stemmingBox.isSelected());
        System.out.println("Indexing started");
        dictianary = readF.readDirectory();
//        readF=null;
//
        return readF.numOfDocsProcessed();
    }

    private void showIndexSummary(int numOfDocsProcessed, long time) {
        Alert summary = createAlert();
        summary.setHeaderText("INDEX SUMMARY");
        summary.setTitle("SUMMARY");
        summary.setHeaderText("INDEX CREATION COMPLETE");
        summary.setContentText("DOCS PROCESSED: " + numOfDocsProcessed + "\n" +
                "DICTIANARY SIZE: " + dictianary.size() + " terms\n" +
                "INDEXING TIME: " + time + "sec");
        summary.show();

    }


    public void testGetDoc() {
        try {


            ReadFile rf;
            rf = new ReadFile(fld_corpusPath.getText(), fld_outputPath.getText(), btn_stemmingBox.isSelected());
            if (!fld_path.getText().equals("")) {

//            int iDoc = Integer.valueOf(fld_path.getText());
                MyDocument document = rf.getDocument(fld_path.getText());// ("LA122790-0222");
                System.out.println(document.getDoc());
//            testReadFileAlert(document.getDocId(), document.getTxt());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void browse(ActionEvent event) {
//        System.out.println(event.);

        DirectoryChooser chooser = new DirectoryChooser();
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int button = Integer.parseInt(data);
        if (button == 1) {// corpusBrowse
            chooser.setTitle("SELECT CORPUS DIRECTORY");
        }
        if (button == 2) {// outputBrowse
            chooser.setTitle("SELECT OUTPUT DIRECTORY");
        }

        File selectedDirectory = chooser.showDialog(new Stage());
        if (selectedDirectory == null)
            return;
        if (button == 1)
            fld_corpusPath.setText(selectedDirectory.getPath());
        if (button == 2)
            fld_outputPath.setText(selectedDirectory.getPath());
    }

    public void showDictianary() {
        String stem = "stemmed";
        if (!btn_stemmingBox.isSelected())
            stem = "not stemmed";
        try {
            File f = new File(fld_outputPath.getText() + "/dataBase/" + stem + "/dictionary.txt");
            if (!f.exists())
                throw new Exception("error dic not found");
            Process process = Runtime.getRuntime().exec("notepad " + f.getPath());
            process.waitFor();
            System.out.println("finish");

        } catch (Exception e) {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("ERROR 123456FO65");
            alert.setContentText("dictionary not found. try a different path");
            alert.show();
            return;
        }

    }


    public void loadDictionary() {

        String stemm = "stemmed";
        if (!btn_stemmingBox.isSelected())
            stemm = "not stemmed";
        File file = new File(fld_outputPath.getText() + "/dataBase/" + stemm + "/dictionary.txt");
        if (!file.exists()) {
            Alert alert = createAlert();
            alert.setHeaderText("dictionary not loaded");
            alert.setContentText("\"" + stemm + "\" dictionary not found");
            alert.show();
            return;
        }
        dictianary = new TreeMap<String, DicEntry>();
        Alert a = createAlert();
        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            String[] line;
            String[] data;
            while ((st = br.readLine()) != null) {

                if (st.equals("")) {
                    continue;
                }

                line = st.split(":");
                data = line[1].split(",");
                DicEntry entry = new DicEntry(Integer.valueOf(data[0]));
                entry.numOfDocs = Integer.valueOf(data[1]);
                entry.totalTermFrequency = Integer.valueOf(data[2]);
                dictianary.put(line[0], entry);
            }


            br.close();
            a.setHeaderText("dictionary loaded successful");
            a.setContentText("total terms loaded: " + dictianary.size());
            testLoadDic();
            a.show();
        } catch (Exception e) {
            a.setContentText("dictianary was not loaded");
            a.show();
        }
    }

    /**
     * creates an alert with colors
     *
     * @return
     */
    private Alert createAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("Alert.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");
        return alert;
    }

    public void tempTest() throws Exception {
        System.out.println("Beginning...");
        long start = System.nanoTime();
        testMerge("C:\\Users\\Dan\\Desktop");
        long end = System.nanoTime();
        System.out.println(String.format("testMerge: %d milliseconds", (end-start)/1000000));
        System.out.println("Done!");

    }

    private void testMerge(String path) {
        try {
            MergeFile m = new MergeFile(path + "\\dataBase\\stemmed\\waitingList", path + "\\dataBase\\stemmed\\postingList");
            m.merge();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * when pressed will delete dictionary in memory and
     * delete zv folder "dataBase"
     */
    public void reset() {
        dictianary = null;
        Alert alert = createAlert();
        System.out.println("resetPresed");
        File file = new File(fld_outputPath.getText() + "/dataBase");

        alert.setHeaderText("ERROR");
        alert.setContentText("no files were deleted");
        if (file.exists()) {
            if (deleteDir(file)) {
                alert.setContentText("dataBase ase been deleted");
                alert.setHeaderText("reset succeeded");
            }
        }
        alert.show();
        System.out.println("finished");
    }

    /**
     * this function givven a directorry will delete it recursivly
     *
     * @param dir - directory to be deleted
     * @return true if deleted
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete(); // The directory is empty now and can be deleted.
    }

    /**
     * opens a window where user enters corpus path and output path
     */
    public void runIndex() {
        // Custom dialog

        Dialog dialog = new Dialog();
        dialog.setHeaderText("Create DATABASE");
        dialog.setTitle("Create DATABASE");
        dialog.setResizable(true);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("Alert.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("myDialog");

        // Widgets
        Label lbl_corpusPath = new Label("corpus path:");
        Label lbl_outPath = new Label("output path:");

        TextField corpusPath = fld_corpusPath;
        TextField outputPath = fld_outputPath;//// make the same
        Button btn_index = new Button("start Indexing");
        Button btn_corpBrows = new Button();
        btn_corpBrows.setUserData("1");
        Button btn_outBrowse = new Button();
        btn_outBrowse.setUserData("2");
        CheckBox cb_useStemmer = new CheckBox("use stemmer");
        cb_useStemmer.setSelected(true);

        btn_outBrowse.setText("Browse");
        btn_corpBrows.setText("Browse");


        // Create layout and add to dialog
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 20, 50));
        grid.add(lbl_corpusPath, 1, 1); // col=1, row=1
        grid.add(corpusPath, 2, 1);
        grid.add(lbl_outPath, 1, 2); // col=1, row=2
        grid.add(outputPath, 2, 2);
        grid.add(btn_outBrowse, 3, 2);
        grid.add(btn_corpBrows, 3, 1);
        grid.add(btn_index, 1, 5);
        grid.add(cb_useStemmer, 1, 4);
        dialog.getDialogPane().setContent(grid);

//        dialog.getDialogPane().getButtonTypes().

        // Add button to dialog
        ButtonType btn_cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(btn_cancel);


        //on click handlers
        /**
         * opens the "createAccount" popup
         */
        btn_index.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                dialog.close();
                btn_stemmingBox.setSelected(cb_useStemmer.isSelected());
                testIndexer();
            }
        });

        btn_corpBrows.setOnAction(this::browse);///???
        btn_outBrowse.setOnAction(this::browse);

        // Show dialog
        dialog.showAndWait();
    }

    public void testLoadDic() {

        System.out.println("test result: " + dictianary.get("hotel"));
    }

}
