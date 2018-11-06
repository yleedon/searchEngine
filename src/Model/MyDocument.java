package Model;

public class MyDocument {
    String doc, docNo, txt;

    //<editor-fold desc="Constructor">
    public MyDocument(String doc) {
        setDoc(doc);
        this.docNo = create("<DocNo>");
        this.txt = create("<Text>");
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    public String getTxt() {
        return txt;
    }

    public String getDoc() {
        return doc;
    }

    public String getDocId() {
        return docNo;
    }
    //</editor-fold>


    public void setDoc(String doc) {
        doc = cleanEndges(doc);
        if(doc != null && doc.startsWith("<Doc>") && doc.endsWith("</Doc>")) {
            this.doc = doc;
            this.docNo = create("<DocNo>");
            this.txt = create("<Text>");
        }
        else
            this.doc = null;
    }

    //<editor-fold desc="Private Functions">
    private String create(String tag){
        String ans;
        try{
            String[] splittedDoc = doc.split(tag);
            ans = (splittedDoc[1].split(tag.replace("<", "</")))[0];
            ans = cleanEndges(ans);
            return ans;
        }
        catch (Exception e){
            return null;
        }
    }

    private String cleanEndges(String s) {
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