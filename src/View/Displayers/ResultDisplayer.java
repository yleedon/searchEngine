package View.Displayers;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import processing.MyDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

public class ResultDisplayer extends VBox {
    private Collection<Label> docs;

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
        while (!documents.isEmpty()){
            docs.add(new Label(documents.poll().getDocumentName()));
        }
        getChildren().addAll(docs);
    }

    /**
     * initialize all fields
     */
    private void init(){
        docs = new ArrayList<>();
    }

    /**
     * getter for the documents
     * @return a collection of Labels, each one is the name of the document
     */
    public Collection<Label> getDocs() {
        return docs;
    }
}
