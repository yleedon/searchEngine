package View;


import Indexer.DicEntry;
import Parser.UpperCaseEntity;
import Ranker.Semantic.LSIExecutor;
import Searcher.Searcher;
import View.Displayers.CitiesFilterDisplayer;
import View.Displayers.MyDocumentDisplayer;
import View.Displayers.ResultDisplayer;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
    private Searcher searcher;
    private PriorityQueue<MyDocument> queryResult;
    public TextField fld_fileQueryPath;
    public TextField fld_fileQueryOutput;
    private boolean ctrlPressed=false;
    private String dicLoaededInfo;
    private boolean bCancelQuery = false;

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
     * File browser for the query file
     */
    public void fileBrowser(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("SELECT THE QUERY FILE");
        File selectedFile = chooser.showOpenDialog(new Stage());   //showDialog(new Stage());
        if(selectedFile == null){
            return;
        }
        fld_fileQueryPath.setText(selectedFile.getPath());
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
        if (button == 3) {// outputBrowse
            chooser.setTitle("SELECT QUERY OUTPUT DIRECTORY");
        }


        File selectedDirectory = chooser.showDialog(new Stage());
        if (selectedDirectory == null)
            return;
        if (button == 1)
            fld_corpusPath.setText(selectedDirectory.getPath());
        if (button == 2)
            fld_outputPath.setText(selectedDirectory.getPath());
        if (button == 3)
            fld_fileQueryOutput.setText(selectedDirectory.getPath());

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
     * @param showInfo
     */
    public void loadDictionary(boolean showInfo) {

        String stemm = "stemmed";
        if (!btn_stemmingBox.isSelected())
            stemm = "not stemmed";
        String dicPath = fld_outputPath.getText() + "/dataBase/" + stemm + "/dictionary.txt";
        File file = new File(dicPath);
        if (!file.exists()) {
            Alert alert = createAlert();
            alert.setHeaderText("dictionary not loaded");
            alert.setContentText("\"" + stemm + "\" dictionary not found");
            alert.show();
            dictianary = null;
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
            dicLoaededInfo = fld_outputPath.getText()+btn_stemmingBox.isSelected();
            if(showInfo) {
                a.setHeaderText("dictionary loaded successful");
                a.setContentText("total terms loaded: " + dictianary.size());
                a.show();
            }
            System.out.println("dictionary loaded");
        } catch (Exception e) {
            a.setContentText("dictianary was not loaded");
            a.show();
            dictianary = null;
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
            System.out.println(f.getPath());
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


    /**
     * Constructor - loads the config (creates a config if does not exist)
     */
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
                pw.println("queryFilePath=");
                pw.println("queryOutPath=");
                pw.close();
                System.out.println("default config file created");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * loads the config
     */
    public void getConfig() {
        try {
            FileReader configFile = new FileReader("config");
            Properties properties = new Properties();
            properties.load(configFile);

            fld_corpusPath.setText(properties.getProperty("corpus"));
            fld_outputPath.setText(properties.getProperty("outPut"));
            fld_fileQueryPath.setText(properties.getProperty("queryFilePath"));
            fld_searchQuary.setPromptText("enter query");
            fld_fileQueryOutput.setText(properties.getProperty("queryOutPath"));
//            cb_semantics.setSelected(true);
            boolean stemmer = false;
            if (properties.getProperty("stemmer").equals("true"))
                stemmer = true;
            btn_stemmingBox.setSelected(stemmer);
        } catch (Exception e) {
            System.out.println("wtf get conig error");
        }
    }

    /**
     * sets the config with the current field data
     */
    public void setConfig() {
        try {
            PrintWriter pw = new PrintWriter("config");
//                FileWriter fw = new FileWriter("config");

            pw.println("corpus=" + fld_corpusPath.getText().replace("\\", "\\\\"));
            pw.println("outPut=" + fld_outputPath.getText().replace("\\", "\\\\"));
            pw.println("stemmer=" + btn_stemmingBox.isSelected());
            pw.println("queryFilePath=" + fld_fileQueryPath.getText().replace("\\", "\\\\"));
            pw.println("queryOutPath="+ fld_fileQueryOutput.getText().replace("\\", "\\\\"));
            pw.close();
            System.out.println("config file updated");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
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
        dialog.setTitle("Filter by cities");
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

    /**
     * display the results of the query
     * @param documents - priority queue of the document, order by rank
     */
    private void showResults(PriorityQueue<MyDocument> documents){
        //sets the result diplayer
        PriorityQueue<MyDocument> pq_docs = new PriorityQueue<>(Comparator.reverseOrder());
        for(MyDocument md:documents){
            pq_docs.add(md);
        }
        ResultDisplayer result = new ResultDisplayer(pq_docs);
        result.setOnKeyPressed(event -> {
            resultKeyPressed(event, result);
        });

        //set the context menu for each label
        for(Label lbl: result.getDocs()){
            lbl.setOnContextMenuRequested(event -> {
                getContextMenu(result.getDocumentID(lbl.getText())).show(lbl, event.getScreenX(), event.getScreenY());
            });
            lbl.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY)){
                    if (event.getClickCount() == 2){
                        showDocument(result.getDocumentID(lbl.getText()));
                    }
                }
            });
        }

        //sets Buttons
        Button btn_showDoc = new Button("Show Document");
        Button btn_showEntitis = new Button("Show Entities");
        Button btn_cancel = new Button("Cancel");

        //opens popup
        final Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(result);

        //sets the button bar
        btn_showDoc.setOnAction(event -> {
            showDocument(result.getDocumentID(((Label)result.getSelectionModel().getSelectedItem()).getText()));
        });
        btn_showEntitis.setOnAction(event -> {
            showEntities(result.getDocumentID(((Label)result.getSelectionModel().getSelectedItem()).getText()));
        });
        btn_cancel.setOnAction(event -> {
            dialog.close();
        });
        ButtonBar bb = new ButtonBar();
        bb.getButtons().addAll(btn_showDoc, btn_showEntitis, btn_cancel);
        dialogVbox.getChildren().add(bb);

        Scene dialogScene = new Scene(dialogVbox, 500, 500);
        dialog.setScene(dialogScene);
        dialog.setTitle("Search Results");
        dialog.showAndWait();

    }

    /**
     * for controlling with keyboard on the resultDisplayer
     * @param event - the key event
     * @param result - the result displayer
     */
    private void resultKeyPressed(KeyEvent event, ResultDisplayer result) {
        MultipleSelectionModel<Label> selection = result.getSelectionModel();
        if (selection.getSelectedIndices().size() == 0 || selection.getSelectedIndices().contains(0)) return;
        if (event.getCode().getName().equals("Enter")){
            showDocument(result.getDocumentID(((Label)result.getSelectionModel().getSelectedItem()).getText()));
        }
    }

    /**
     * getter for specific context menu for the doc
     * @param documentID - the specific document
     * @return the right context menu for the document
     */
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

    /**
     * display the entities of the document
     * @param docID - the id of the requested document
     */
    private void showEntities(int docID){
        MyDocument doc = findDocInResults(docID);
        if (doc == null)
            return;
        try {
            ArrayList<UpperCaseEntity> entities = searcher.getFiveEnteties(doc);
            ListView entitiesView = new ListView();
            for (UpperCaseEntity entity: entities){
                entitiesView.getItems().add(new Label(entity.toString()));
            }

            //popup
            final Stage dialog = new Stage();
            dialog.initModality(Modality.NONE);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(entitiesView);

            //sets the button bar
            Button btn_cancel = new Button("Cancel");
            btn_cancel.setOnAction(event -> {
                dialog.close();
            });
            dialogVbox.getChildren().add(btn_cancel);


            Scene dialogScene = new Scene(dialogVbox, 300, 300);
            dialog.setScene(dialogScene);
            dialog.setTitle("Five top Entities");
            dialog.showAndWait();
        }
        catch (Exception e){
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * displays the requested document
     * @param docID - the id of the requested document
     */
    private void showDocument(int docID){
        MyDocument document = findDocInResults(docID);
        MyDocumentDisplayer docDisplayer = new MyDocumentDisplayer(document);

        //opens popup
        final Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(docDisplayer);

        //sets the button bar
        Button btn_showEntities = new Button("Show Entities");
        Button btn_cancel = new Button("Cancel");
        btn_showEntities.setOnAction(event -> {
            showEntities(docID);
        });
        btn_cancel.setOnAction(event -> {
            dialog.close();
        });
        ButtonBar bb = new ButtonBar();
        bb.getButtons().addAll(btn_showEntities, btn_cancel);
        dialogVbox.getChildren().add(bb);


        Scene dialogScene = new Scene(dialogVbox, 500, 500);
        dialog.setScene(dialogScene);
        dialog.setTitle("Document Preview");
        dialog.showAndWait();
    }

    /**
     * getter for the Document itself
     * @param docID - the id of the requested doc
     * @return the requested document, if not found returns null
     */
    private MyDocument findDocInResults(int docID) {
        Iterator<MyDocument> iterator = queryResult.iterator();
        MyDocument ans = null;
        while (iterator.hasNext()){
            ans = iterator.next();
            if(ans.getDocId() == docID)
                return ans;
        }
        return ans;
    }

    /**
     * for keyboard shortcuts
     * @param event - the key event
     */
    public void onKeyPressed(KeyEvent event){
        if (event.getCode().getName().equals("Enter")){
            searchPressed();
        }
        else if (event.getCode().getName().equals("Ctrl")){
            ctrlPressed = true;
        }
        else if (ctrlPressed && event.getCode().getName().equals("L")){
            loadDictionary(true);
        }
        else if (ctrlPressed && event.getCode().getName().equals("Q")){
            fileSearchPressed();
        }
    }

    /**
     * for keyboard shortcut
     * @param event - the key event
     */
    public void onKeyReleased(KeyEvent event){
        if (event.getCode().getName().equals("Ctrl")){
            ctrlPressed = false;
        }
    }

    /**
     * focus on the query txt field
     */
    public void setFocus() {
        fld_searchQuary.requestFocus();
    }

    /**
     * load config by double click on the main screen
     * @param event - mouse event
     */
    public void getConfigMainWindow(MouseEvent event){
        if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
            getConfig();
        }
    }////////////////////???????????

    /**
     * for the tests
     */
    public void buttonTestPressed() {
        LSIExecutor exe = new LSIExecutor();
        System.out.println(exe.getSynonyms("observe"));
        System.out.println(exe.spellCheck("obzerve"));
    }

    /**
     * loading the dictionary
     */
    public void loadDicPressed(){
        loadDictionary(true);
    }

    /**
     * searching file query
     */
    public void fileSearchPressed(){
        if(dictianary == null || (dicLoaededInfo != null && !dicLoaededInfo.equals(fld_outputPath.getText()+btn_stemmingBox.isSelected()))){
            loadDictionary(false);
            if (dictianary==null)
                return;
        }

        File queryFile = new File(fld_fileQueryPath.getText());
        if(!queryFile.exists()) {
            Alert alert = createAlert();
            alert.setContentText("query file not found");
            alert.showAndWait();
            return;
        }
        try {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.INFORMATION);
            for (Node node : alert.getDialogPane().getChildren()) {
                if (node instanceof ButtonBar) {
                    node.setVisible(false);
                }
            }
            alert.setContentText("this may take a few minutes.\nA notification message will appear on completion.\n\n (progress can be seen in the console)");
            alert.setHeaderText("File query in progress...");
            alert.setTitle("FILE QUERY SEARCH");
            alert.show();
            searcher = new Searcher(fld_searchQuary.getText(), fld_corpusPath.getText(), btn_stemmingBox.isSelected(), fld_outputPath.getText(), cb_semantics.isSelected(), selectedCitiesFilter,dictianary);
            long start = System.nanoTime();
            searcher.getFileQuerySearchReaults(queryFile,fld_fileQueryOutput.getText());
            alert.close();
            System.out.println("total file query time: "+ (System.nanoTime() - start)/1000000000 + "Sec");

        }
        catch (Exception e){
            Alert alert = createAlert();
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setHeaderText("File query complete");
        alert.setContentText("outPut file name:\nresults.txt");
        alert.showAndWait();

    }

    /**
     * sends the qury to the searcher and shows the results
     */
    public void searchPressed() {
        if(dictianary == null || (dicLoaededInfo != null && !dicLoaededInfo.equals(fld_outputPath.getText()+btn_stemmingBox.isSelected()))){
            loadDictionary(false);
            if (dictianary==null)
                return;
        }
        try {
            if(!(new File(fld_corpusPath.getText()).exists()))
                throw new Exception("path does not exist\n"+fld_corpusPath.getText());

            searcher = new Searcher(fld_searchQuary.getText(), fld_corpusPath.getText(), btn_stemmingBox.isSelected(), fld_outputPath.getText(), cb_semantics.isSelected(), selectedCitiesFilter,dictianary);
            String spellChecked = searcher.runSpellcheck(fld_searchQuary.getText());

            if(!spellChecked.equals(fld_searchQuary.getText())){
                  if (!verifySpell(spellChecked)) {
                    return;
                }
            }

            queryResult = searcher.getSearchResault();
            showResults(queryResult);

        } catch (Exception e) {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }

    /**
     * Verifying the spell check
     * @param spellCheckedQuery - the query after spell checking
     * @return - true if the user chose to continue, false if the user canceled the query
     */
    private boolean verifySpell(String spellCheckedQuery){
        Label[] lines = {new Label("   Did you mean: "+spellCheckedQuery + "?"),
                new Label("   YES - change query."),
                new Label("   NO - use original Query."),
                new Label("   CANCEL - cancels the search.")
        };
        final Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().addAll(lines);

        ButtonBar bb = new ButtonBar();
        Button btn_cancel = new Button("Cancel");
        Button btn_Yes = new Button("Yes");
        Button btn_No = new Button("No");

        btn_cancel.setOnAction(event -> {
            bCancelQuery = true;
            dialog.close();
        });
        btn_Yes.setOnAction(event -> {
            fld_searchQuary.setText(spellCheckedQuery);
            searcher.setQuary(spellCheckedQuery);
            dialog.close();
        });
        btn_No.setOnAction(event -> {
            dialog.close();
        });

        bb.getButtons().addAll(btn_Yes, btn_No, btn_cancel);
        dialogVbox.getChildren().add(bb);

        Scene dialogScene = new Scene(dialogVbox, 550, 230);
        dialog.setScene(dialogScene);
        dialog.setTitle("We found A SpellCheck mistake:");
        dialog.showAndWait();

        if (bCancelQuery){
            bCancelQuery = false;
            return false;
        }
        return true;
    }
}
