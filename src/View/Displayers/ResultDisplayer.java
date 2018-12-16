package View.Displayers;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.Collection;

public class ResultDisplayer extends GridPane {
    private Collection<Label> docs;

    /**
     * Constructor
     * @param documents - a collection of documents (as a String) to display
     */
    public ResultDisplayer(Collection<String> documents){
        setDocuments(documents);
    }

    /**
     * sets the document to display
     * @param documents
     */
    private void setDocuments(Collection<String> documents) {
    }

    /**
     * initialize all fields
     */
    private void init(){
    }

    /**
     * getter for the documents
     * @return a collection of Labels, each one is the name of the document
     */
    public Collection<Label> getDocs() {
        return docs;
    }
}
