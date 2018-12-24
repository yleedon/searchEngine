package View.Displayers;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import processing.MyDocument;

import java.util.*;

public class ResultDisplayer extends ListView {
    private Collection<Label> docs;
    private Map<String, Integer> documentMap;

    /**
     * Constructor
     * @param documents - a collection of documents (as a String) to display
     */
    public ResultDisplayer(PriorityQueue<MyDocument> documents){
        setDocuments(documents);
    }

    /**
     * sets the document to display
     * @param documents
     */
    private void setDocuments(PriorityQueue<MyDocument> documents) {
        if (documents == null)
            return;
        init();
        MyDocument current;
        Label lbl;
        int i=1;
        while (!documents.isEmpty()){
            current = documents.poll();
            lbl = new Label(i+".\t"+current.getDocumentName()+"       " + current.getRank());
            docs.add(lbl);
            documentMap.put(lbl.getText(), current.getDocId());
            i++;
        }
//        getChildren().addAll(docs);
        getItems().addAll(docs);
    }

    /**
     * gets the id of the document
     * @param documentName - the real name of the document
     * @return the unique id of the document
     */
    public int getDocumentID(String documentName){
        int ans = documentMap.get(documentName);
        return ans;
    }

    /**
     * initialize all fields
     */
    private void init(){
        docs = new ArrayList<>();
        Label title = new Label("#\tDocument:\tRank:");
        title.setStyle("-fx-text-fill: BLUE;-fx-font-weight:bold;");
        getItems().add(title);
        documentMap = new HashMap<>();
    }

    /**
     * getter for the documents
     * @return a collection of Labels, each one is the name of the document
     */
    public Collection<Label> getDocs() {
        return docs;
    }
}
