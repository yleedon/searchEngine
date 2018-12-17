package View.Displayers;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import processing.MyDocument;

import java.util.*;

public class ResultDisplayer extends VBox {
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
        while (!documents.isEmpty()){
            current = documents.poll();
            docs.add(new Label(current.getDocumentName()));
            documentMap.put(current.getDocumentName(), current.getDocId());
        }
        getChildren().addAll(docs);
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
