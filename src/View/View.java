package View;


import Indexer.DicEntry;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import processing.ReadFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;

public class View {

    public CheckBox btn_stemmingBox;
    public TextField fld_outputPath;
    public TextField fld_corpusPath;
    public Button btn_reset;
    private Map<String, DicEntry> dictianary;
    public Button btn_corpusBrowse;
    public Button btn_outPutPath;

    /**
     * starts indexing the corpus - activated by the user (start indexing button)
     */
    public void startIndexing() {
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

        int numOfDocsProcessed = createDataBase();

        end = System.nanoTime();
        long time = (end - start) / 1000000000;

        showIndexSummary(numOfDocsProcessed, time);

    }

    /**
     * creates ReadFile and starts indexing the dataBAse
     * @return - number of documents processed
     */
    private int createDataBase() {
        ReadFile readF = new ReadFile(fld_corpusPath.getText(), fld_outputPath.getText(), btn_stemmingBox.isSelected());
        System.out.println("Indexing started");
        dictianary = readF.readDirectory();
        return readF.numOfDocsProcessed();
    }

    /**
     * shows:
     * number of documents processed
     * number of terms in dictionary
     * total elapsed time
     * @param numOfDocsProcessed - number of documents processed
     * @param time - total indexing time
     */
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

    /**
     * allows the oser to pick a folder with fileChooser
     * @param event - which button called the function
     */
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

    /**
     * opens the dictionary in notepad
     */
    public void showDictianary() {
        String stem = "stemmed";
        if (!btn_stemmingBox.isSelected())
            stem = "not stemmed";
        try {
            File f = new File(fld_outputPath.getText() + "\\dataBase\\" + stem + "\\dicToShow.txt");
            if (!f.exists())
                throw new Exception("error dic not found");
            Process process = Runtime.getRuntime().exec("notepad " + f.getPath());
//            process.waitFor();
//            System.out.println("finish");

        } catch (Exception e) {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("ERROR 123456FO65");
            alert.setContentText("dictionary not found. try a different path");
            alert.show();
            return;
        }

    }

    /**
     * loads the dictionary into the main memory
     */
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

    /**
     * when pressed will delete dictionary in memory and
     * delete zv folder "dataBase"
     */
    public void reset() {
        dictianary = null;

        Alert alert = createAlert();
//        System.out.println("resetPresed");
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
//        System.out.println("finished");
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
     * opens the readme file
     */
    public void helpPressed(){
        try {
            File f = new File("readMe.txt");
            if (!f.exists())
                throw new Exception("error readMe.txt not found");
            Process process = Runtime.getRuntime().exec("notepad " + f.getPath());
//            process.;
//            System.out.println("finish");

        } catch (Exception e) {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("ERROR 123456FO65 - 2");
            alert.setContentText("readme not found");
            alert.show();
            return;
        }

    }

}
