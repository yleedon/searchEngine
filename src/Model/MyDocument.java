package Model;

public class MyDocument {
    String doc, txt;

    public MyDocument(String doc) {
        this.doc = doc;
        createTxt();
    }

    public String getTxt() {
        return txt;
    }

    private void createTxt(){
        try {
            String[] splittedDoc = doc.split("<Text>");
            txt = (splittedDoc[1].split("</Text>"))[0];
            txt = txt.substring(1, txt.length()-1);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            txt = null;
        }
    }
}
