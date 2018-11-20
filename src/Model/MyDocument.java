package Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MyDocument {

    //<editor-fold desc="Fields">
    String doc, docNo, txt, city, date, title;
    int maxFrequency;
    Map<String, Integer> terms;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructor. by the document string(have to be between the tags <Doc> and </Doc>) dismember the documentID and the Text inside.
     * if there is no tags (<DocNo> - for docID, and <Text> for txt) it makes them null;
     * @param doc
     */
    public MyDocument(String doc) {
        setDoc(doc);
        maxFrequency = -1;
//        this.docNo = create("<DocNo>");
//        this.txt = create("<Text>");

    }
    //</editor-fold>

    //<editor-fold desc="Getters">

    /**
     * Getter to the city that the document came from.
     * @return The city where they post this document, if there is no city, returns null
     */
    public String getCity(){
        if(city==null) {
            city = create("<F P=104>");
            if (city.contains(" ")){
                city = city.split(" ")[0].toUpperCase();
            }
        }
        return city;
    }

    /**
     * The date that the document made at.
     * @return the date when they post this document, if there is no date, returns null
     */
    public String getDate() {
        if(date==null) {
            date = create("<Date>");
            if (date==null)
                date = create("<Date1>");
        }
        return date;
    }

    /**
     * Getter to the txt field
     * @return - the text of the document. If there is no tag <Text> the getter will return null;
     */
    public String getTxt() {
        if (txt==null)
            txt = create("<Text>");
        return txt;
    }

    /**
     * Getter to the maximum frequency of all the terms in the document
     * @return the max frequency of the term, if hasn't been set yet return -1
     */
    public int getMaxFrequency() {
        return maxFrequency;
    }

    /**
     * Getter for all the terms and their frequency in this document
     * @return The term HashMap of the doc
     */
    public Map<String, Integer> getTerms() {
        return terms;
    }

    /**
     * Getter to the whole doc
     * @return - the whole doc String. if doesn't start with the tag <Doc> and ends with the tag </Doc> returns null
     */
    public String getDoc() {
        return doc;
    }

    /**
     * Getter to the docId field
     * @return - the docId of the document(between the tags <Doc> and </Doc>). If there is no tag <DocNo> the getter will return null;
     */
    public String getDocId() {
        if (this.docNo==null)
            this.docNo = create("<DocNo>");
        return docNo;
    }

    /**
     * Getter to the title of the document
     * @return - the title of the document( between the tags <TI> and </TI>0. if there is no taf <TI> returns null;
     */
    public String getTitle(){
        if(this.title==null)
            this.title = create(("<TI"));
        return title;
    }
    //</editor-fold>

    //<editor-fold desc="Setters">

    /**
     * set the document String for the object. if doesn't starts with <Doc> and ends with </Doc> sets it as null.
     * @param doc - the String of all the document
     */
    public void setDoc(String doc) {
        doc = cleanEdges(doc);
        if(doc != null && ((doc.startsWith("<Doc>") && doc.endsWith("</Doc>")) || (doc.startsWith("<DOC>") && doc.endsWith("</DOC>")))) {
            this.doc = doc;
//            this.docNo = create("<DocNo>");
//            this.txt = create("<Text>");
        }
        else
            this.doc = null;
    }

    /**
     * Setter for the terms HashMap of the document(the terms and their frequency
     * @param terms - The HashMap of the terms and their frequency.
     */
    public void setTerms(Map<String, Integer> terms) {
        this.terms = terms;
    }

    /**
     * Setter for the max frequency term
     * @param maxFrequency - the maximum frequency of a term
     */
    public void setMaxFrequency(int maxFrequency) {
        this.maxFrequency = maxFrequency;
    }

    //</editor-fold>

    //<editor-fold desc="Fields Creator Functions">

    /**
     * create the field by the tag.
     * @param tag - the requested tag (for example: <Text>)
     * @return - all the string between the tag and its closing tag (for example will return all the text between <Text> and </Text> in the doc field. if tag doesn't exists returns null.
     */
    private String create(String tag){
        try{
            if (!(doc.contains(tag))) {tag = tag.toUpperCase();}
            if (!(doc.contains(tag))) {return null;}
            String[] splittedDoc = doc.split(tag);
            return cleanEdges((splittedDoc[1].split(tag.replace("<", "</")))[0]);
        }
        catch (Exception e){
            return null;
        }
    }

    /**
     * Clean the char '\n' from the string
     * @param s - the string to clean
     * @return - the same given string without '\n' in the beginning and in the end.
     */
    private String cleanEdges(String s) {
        if (s == null)
            return null;
        while(s.startsWith("\n") || s.startsWith(" ")){
            s = s.substring(1);
        }
        if (s.endsWith("\n") || s.endsWith(" ")){
            s = s.substring(0, s.length()-1);
        }
        return s;
    }
    //</editor-fold>
}