package Model;

public class MyDocument {

    //<editor-fold desc="Fields">
    String doc, docNo, txt;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructor. by the document string(have to be between the tags <Doc> and </Doc>) dismember the documentID and the Text inside.
     * if there is no tags (<DocNo> - for docID, and <Text> for txt) it makes them null;
     * @param doc
     */
    public MyDocument(String doc) {
        setDoc(doc);
//        this.docNo = create("<DocNo>");
//        this.txt = create("<Text>");

    }
    //</editor-fold>

    //<editor-fold desc="Getters">

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
    //</editor-fold>

    //<editor-fold desc="Fields Creator Functions">

    /**
     * create the field by the tag.
     * @param tag - the requested tag (for example: <Text>)
     * @return - all the string between the tag and its closing tag (for example will return all the text between <Text> and </Text> in the doc field. if tag doesn't exists returns null.
     */
    private String create(String tag){
        String ans;
        try{
            if (!(doc.contains(tag))) {tag = tag.toUpperCase();}
            if (!(doc.contains(tag))) {return null;}
            String[] splittedDoc = doc.split(tag);
            ans = (splittedDoc[1].split(tag.replace("<", "</")))[0];
            ans = cleanEdges(ans);
            return ans;
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
        String ans = s;
        if (ans == null)
            return null;
        if(ans.startsWith("\n")){
            ans = ans.substring(1);
        }
        if (ans.endsWith("\n")){
            ans = ans.substring(0, ans.length()-1);
        }
        return ans;
    }
    //</editor-fold>
}