package Model;

public class Dodument {
    String doc, txt;

    public Dodument(String doc) {
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
        }
        catch (Exception e){
            txt = null;
        }
    }
}
