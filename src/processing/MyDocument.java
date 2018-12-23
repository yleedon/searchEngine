package processing;

import javafx.util.Pair;

import java.util.Map;
import java.util.Set;

public class MyDocument implements Comparable{

    //<editor-fold desc="Fields">
    private String delimiters = " $%'\",?!\\/:;()[]{}\n#&|*�+=^@_~-";
    private String doc, txt, city, date, title, name;
    private int textTokenCount;
    private Map<String, Pair<Integer, Integer>> terms;
    private Set<String> titleSet;
    private int docNo;
    private String cityData;
    private double rank;
    //</editor-fold>

    /**
     * Constructor. by the document string(have to be between the tags <Doc> and </Doc>) dismember the documentID and the Text inside.
     * if there is no tags (<DocNo> - for docID, and <Text> for txt) it makes them null;
     *
     * @param doc
     */
    public MyDocument(String doc) {
        setDoc(doc);
        rank = 0;

//        this.docNo = create("<DocNo>");
//        this.txt = create("<Text>");

    }

    /**
     * Constructor for id and rank
     * @param docID - the unique id of this document
     * @param rank - the rank of this document
     */
    public MyDocument(int docID, double rank){
        docNo = docID;
        this.rank = rank;
    }

    //<editor-fold desc="Getters">

    /**
     * Getter to the city that the document came from.
     *
     * @return The city where they post this document, if there is no city, returns null
     */
    public String getCity() {
        if (city == null) {
            city = create("<F P=104>");
            if (city == null || city.length() == 0) {
                city = "";
                return city;
            }
            city = cleanNumerialEdges(city);
            if (city.contains(" ")) {
                city = city.split(" ")[0];
            }
            city = cleanNumerialEdges(city.toUpperCase());
            city = replaceDelimiters(city);
        }
        return city;
    }

    /**
     * The date that the document made at.
     *
     * @return the date when they post this document, if there is no date, returns null
     */
    public String getDate() {
        if (date == null) {
            date = create("<Date>");
            if (date == null)
                date = create("<Date1>");
        }
        return date;
    }

    /**
     * getter for the doc's name
     * @return the name of the document (between the tag <DOCNO>). if not found returns null.
     */
    public String getDocumentName(){
        if (name == null)
            name = create("<DocNo>");
        return name;
    }

    /**
     * Getter to the txt field
     *
     * @return - the text of the document. If there is no tag <Text> the getter will return null;
     */
    public String getTxt() {
        if (txt == null)
            txt = create("<Text>");
        return txt;
    }

    /**
     * Getter for all the terms and their frequency in this document
     *
     * @return The term HashMap of the doc
     */
    public Map<String, Pair<Integer, Integer>> getTerms() {
        return terms;
    }

    /**
     * Getter to the whole doc
     *
     * @return - the whole doc String. if doesn't start with the tag <Doc> and ends with the tag </Doc> returns null
     */
    public String getDoc() {
        return doc;
    }

    /**
     * Getter to the docId field
     *
     * @return - the docId of the document(between the tags <Doc> and </Doc>). If there is no tag <DocNo> the getter will return null;
     */
    public int getDocId() {
        return docNo;
    }

    /**
     * Getter to the title of the document
     *
     * @return - the title of the document( between the tags <TI> and </TI>0. if there is no taf <TI> returns null;
     */
    public String getTitle() {
        if (this.title == null)
            this.title = create(("<TI>"));
        return (title != null ? title : "");
    }

    /**
     *
     * @return - the number of tokens in the document
     */
    public int getTextTokenCount() {
        return textTokenCount;
    }

    /**
     * getter for the document's rank
     * @return the rank of the document (as double). if hasn't been set, returns 0
     */
    public double getRank(){
        return rank;
    }

    //</editor-fold>

    //<editor-fold desc="Setters">

    /**
     * set the document String for the object. if doesn't starts with <Doc> and ends with </Doc> sets it as null.
     *
     * @param doc - the String of all the document
     */
    public void setDoc(String doc) {
        doc = cleanEdges(doc);
        if (doc != null && ((doc.startsWith("<Doc>") && doc.endsWith("</Doc>")) || (doc.startsWith("<DOC>") && doc.endsWith("</DOC>")))) {
            this.doc = doc;
        } else
            this.doc = null;
    }

    /**
     * Setter for the terms HashMap of the document(the terms and their frequency
     *
     * @param terms - The HashMap of the terms and their frequency.
     */
    public void setTerms(Map<String, Pair<Integer, Integer>> terms) {
        this.terms = terms;
    }

    public void setTitleSet(Map<String, Pair<Integer, Integer>> titleMap) {
        titleSet = titleMap.keySet();
    }

    /**
     * srts the id of the document
     * @param docNumber - the doc ID
     */
    public void setDocId(int docNumber) {
        docNo = docNumber;
    }

    /**
     * sets the total number of tokens in the document
     * @param count -  the total number of tokens in the document
     */
    public void setTextTokenCount(int count) {
        textTokenCount = count;
    }

    public void setRank(double rank){
        this.rank = rank;
    }

    //</editor-fold>

    //<editor-fold desc="Fields Creator Functions">

    /**
     * create the field by the tag.
     *
     * @param tag - the requested tag (for example: <Text>)
     * @return - all the string between the tag and its closing tag (for example will return all the text between <Text> and </Text> in the doc field. if tag doesn't exists returns null.
     */
    private String create(String tag) {
        try {
            if (!(doc.contains(tag))) {
                tag = tag.toUpperCase();
            }
            if (!(doc.contains(tag))) {
                return null;
            }
            String closeTag = (tag.split(" ")[0]).replace("<", "</");
            String[] splittedDoc = doc.split(tag);
            String ans = (splittedDoc[1].split(closeTag))[0];
            return cleanEdges(ans);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clean the char '\n' from the string
     *
     * @param s - the string to clean
     * @return - the same given string without '\n' in the beginning and in the end.
     */
    private String cleanEdges(String s) {
        if (s == null)
            return null;
//        while(s.startsWith("\n") || s.startsWith(" ") || s.startsWith("(")){
//            s = s.substring(1);
//        }
        while (s.length() > 0 && delimiters.contains("" + s.charAt(0))) {
            s = s.substring(1);
        }
        while (s.length() > 0 && delimiters.contains("" + s.charAt(s.length() - 1))) {
            s = s.substring(0, s.length() - 1);
        }
//        while(s.endsWith("\n") || s.endsWith(" ") || s.endsWith(")")){
//            s = s.substring(0, s.length()-1);
//        }
        return s;
    }

    /**
     * cleans numbers and selimiters from the edges
     * @param s - the string to be cleaned
     * @return the cleaned string
     */
    private String cleanNumerialEdges(String s) {
        String delimetersWithNumbers = delimiters + "1234567890";
        if (s == null)
            return null;
        while (s.length() > 0 && delimetersWithNumbers.contains("" + s.charAt(0))) {
            s = s.substring((1));
        }
        while (s.length() > 0 && delimetersWithNumbers.contains("" + s.charAt(s.length() - 1))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * replacing = , @ * ~ to empty string
     *
     * @param s - the string to change
     * @return same string without these signs
     */
    private String replaceDelimiters(String s) {
        return s.replace("=", "").replace(",", "").replace("@", "").replace("*", "").replace("~", "").replace("'","");
    }

    //</editor-fold>

    /**
     * check if a term is in the title
     *
     * @param term - the term to check
     * @return 1 if the term is in the title, else returns 0.
     */
    public int isInTitle(String term) {
        if (titleSet == null) return 0;
        return titleSet.contains(term) ? 1 : 0;
    }

    /**
     * sets the positions where the city is found in the text
     * @param cityPositions - the positions where the city is found in the text
     */
    public void setCityData(String cityPositions) {
        cityData = "*" + cityPositions + "~";
    }

    /**
     *
     * @param gap - the gap betwwen this docID and the last entered docID
     * @return -  the positions where the city is found in the text
     */
    public String getCityData(int gap) {
        return gap + cityData;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof MyDocument){
            double ans = rank-((MyDocument)o).rank;
            if (ans > 0) return 1;
            if (ans < 0) return -1;
        }
        return 0;
    }

    /**
     * callculates the total number ofTerm+appearances in the curent document
     * @return - the total number of term appearances.
     */
    public int getTotalDocTermFrequanct(){
        int ans=0;
        for(String term:terms.keySet()){
            if(term.equals(""))
                continue;
            ans += terms.get(term).getKey();
        }
        return ans;

    }

    @Override
    public String toString() {
        String ans = "My Document: id= " + docNo+", rank= "+rank;
        return ans;
    }
}