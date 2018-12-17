package View;


import Indexer.DicEntry;
import Searcher.Searcher;
import View.Displayers.CitiesFilterDisplayer;
import View.Displayers.ResultDisplayer;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import processing.MyDocument;
import processing.ReadFile;

import java.io.*;
import java.util.*;

public class View {

    public CheckBox btn_stemmingBox;
    public TextField fld_outputPath;
    public TextField fld_corpusPath;
    public Button btn_reset;
    private Map<String, DicEntry> dictianary;
    public Button btn_corpusBrowse;
    public Button btn_outPutPath;
    public Button btn_search;
    public TextField fld_searchQuary;
    public CheckBox cb_semantics;
    private HashSet<String> selectedCitiesFilter;

    //<editor-fold desc="part A">

    /**
     * starts indexing the corpus - activated by the user (start indexing button)
     */
    public void startIndexing() {
        Alert processingAlert = createAlert();
        processingAlert.setAlertType(Alert.AlertType.INFORMATION);
        for (Node node : processingAlert.getDialogPane().getChildren()) {
            if (node instanceof ButtonBar) {
                node.setVisible(false);
            }
        }
        processingAlert.setTitle("Processing");
        processingAlert.setHeaderText("Analyzing corpus");
        processingAlert.setContentText("Please wait...");


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

        processingAlert.show();
        long end;
        long start = System.nanoTime();

        int numOfDocsProcessed = createDataBase();

        end = System.nanoTime();
        long time = (end - start) / 1000000000;

        processingAlert.close();
        showIndexSummary(numOfDocsProcessed, time);

    }

    /**
     * creates ReadFile and starts indexing the dataBAse
     *
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
     *
     * @param numOfDocsProcessed - number of documents processed
     * @param time               - total indexing time
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
     *
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
        System.gc();
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
    public void helpPressed() {
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
    //</editor-fold>


    public View() {
        try {
            FileReader configFile = new FileReader("config");
        } catch (Exception e) {
            System.out.println("config file not found");
            try {
                PrintWriter pw = new PrintWriter("config");
//                FileWriter fw = new FileWriter("config");

                pw.println("corpus=");
                pw.println("outPut=");
                pw.println("stemmer=true");
                pw.close();
                System.out.println("default config file created");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void searchPressed() {


        try {
            Searcher searcher = new Searcher(fld_searchQuary.getText(), fld_corpusPath.getText(), btn_stemmingBox.isSelected(), fld_outputPath.getText(), cb_semantics.isSelected(), selectedCitiesFilter);
            System.out.println("unimplemented searchPressed");
        } catch (Exception e) {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }

    public void getConfig() {
        try {
            FileReader configFile = new FileReader("config");
            Properties properties = new Properties();
            properties.load(configFile);
            String s = properties.getProperty("corpus");
            fld_corpusPath.setText(properties.getProperty("corpus"));
            fld_outputPath.setText(properties.getProperty("outPut"));
            boolean stemmer = false;
            if (properties.getProperty("stemmer").equals("true"))
                stemmer = true;
            btn_stemmingBox.setSelected(stemmer);
        } catch (Exception e) {
            System.out.println("wtf get conig error");
        }
    }

    public void setConfig() {
        try {
            PrintWriter pw = new PrintWriter("config");
//                FileWriter fw = new FileWriter("config");

            pw.println("corpus=" + fld_corpusPath.getText().replace("\\", "\\\\"));
            pw.println("outPut=" + fld_outputPath.getText().replace("\\", "\\\\"));
            pw.println("stemmer=" + btn_stemmingBox.isSelected());
            pw.close();
            System.out.println("config file updated");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void buttonTestPressed() {
        testResult();
    }

    /**
     * open popup and let the user choose the cities for filtering the query
     */
    public void onClickedCityFilter() {
        Collection<String> sCities = getCitiesFromIndex();
        if (sCities == null || sCities.size() == 0)
            return;
        CitiesFilterDisplayer cities = new CitiesFilterDisplayer(sCities, selectedCitiesFilter);

        //opens popup
        final Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(cities);

        GridPane gp_btns = new GridPane();
//        gp_btns.getColumnConstraints().add(new ColumnConstraints(100));
//        gp_btns.getColumnConstraints().add(new ColumnConstraints(100));
        Button btn_cancel = new Button("Cancel");
        Button btn_resetSelection = new Button("Reset");
        Button btn_selectAll = new Button("Select All");
        Button btn_select = new Button("Select");
        btn_cancel.setOnAction(event -> {
            dialog.close();
        });
        btn_resetSelection.setOnAction(event -> {
            cities.setSelectionToAll(false);
        });
        btn_select.setOnAction(event -> {
            selectedCitiesFilter = cities.getSelectedCities();
            dialog.close();
        });
        btn_selectAll.setOnAction(event -> {
            cities.setSelectionToAll(true);
        });
        gp_btns.add(btn_select, 0, 0);
        gp_btns.add(btn_selectAll, 1, 0);
        gp_btns.add(btn_resetSelection, 2, 0);
        gp_btns.add(btn_cancel, 3, 0);

        dialogVbox.getChildren().add(gp_btns);

        Scene dialogScene = new Scene(dialogVbox, 500, 500);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    /**
     * read the cities from the cityIndex
     *
     * @return a collection of cities (as a String) from the cityIndex
     */
    private Collection<String> getCitiesFromIndex() {
        String sCityIdx = fld_outputPath.getText();
        sCityIdx += btn_stemmingBox.isSelected() ? "\\dataBase\\stemmed\\cityIndex.txt" : "\\dataBase\\not stemmed\\cityIndex.txt";

        //read file
        try {
            File fCityIdx = new File(sCityIdx);
            FileReader fr = new FileReader(fCityIdx);
            BufferedReader reader = new BufferedReader(fr);

            //create the collection
            Collection<String> ans = new TreeSet<>();
            String city;
            while (reader.ready()) {
                city = reader.readLine().split("=")[0];
                ans.add(city);
            }
            return ans;
        } catch (Exception e) {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("could not find path!\n" + sCityIdx);
            alert.showAndWait();
            return null;

        }
    }

    private void testResult(){
        PriorityQueue<MyDocument> documents = new PriorityQueue<>((o1, o2) -> (int)(o2.getRank()-o1.getRank()));
        ReadFile rf = new ReadFile(fld_corpusPath.getText(), fld_outputPath.getText(), btn_stemmingBox.isSelected());
        MyDocument md;
        for (int i=1; i<=25; i++) {
            md = rf.getDocument(i+"");
            md.setDocId(i);
            md.setRank(Math.random());
            documents.add(md);
        }
        showResults(documents);
    }

    private void showResults(PriorityQueue<MyDocument> documents){
        ResultDisplayer result = new ResultDisplayer(documents);

        for(Label lbl: result.getDocs()){
            lbl.setOnContextMenuRequested(event -> {
                getContextMenu(result.getDocumentID(lbl.getText())).show(lbl, event.getScreenX(), event.getScreenY());
            });
        }

        //opens popup
        final Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(result);
        Scene dialogScene = new Scene(dialogVbox, 500, 500);
        dialog.setScene(dialogScene);
        dialog.showAndWait();

    }

    private ContextMenu getContextMenu(int documentID){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem showEntities = new MenuItem("Show Entities");
        MenuItem showDocument = new MenuItem("Show Document");
        showEntities.setOnAction(event -> {
            showEntities(documentID);
        });
        showDocument.setOnAction(event -> {
            showDocument(documentID);
        });
        contextMenu.getItems().addAll(showEntities, showDocument);
        return contextMenu;
    }

    private void showEntities(int docID){
        System.out.println("The Entities of DocumentID: "+docID);
    }

    private void showDocument(int docID){
        System.out.println("The Document of DocumentID: "+docID);
    }



}
