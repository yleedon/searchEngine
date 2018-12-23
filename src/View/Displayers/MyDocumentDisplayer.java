package View.Displayers;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import processing.MyDocument;

import java.util.ArrayList;

public class MyDocumentDisplayer extends ListView {
    MyDocument doc;
    ArrayList<Label> lines;

    public MyDocumentDisplayer(MyDocument document){
        if (document != null)
            setDocument(document);
    }

    private void setDocument(MyDocument document) {
        doc = document;
        String sDoc = document.getDoc();
        String[] sLines = sDoc.split("\n");
        init();
        for (String line: sLines){
            lines.add(new Label(line));
        }
        lines.add(new Label("Rank: "+doc.getRank()));
        setPrefHeight(450);
        setPrefWidth(250);
        getItems().addAll(lines);
    }

    private void init(){
        lines = new ArrayList<>();
    }
}
