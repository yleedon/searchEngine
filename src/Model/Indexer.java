package Model;


import java.util.HashMap;
import java.util.Map;

public class Indexer {

    private Map<String,Integer> dictianary;
    private int nextLineNum;
    private Map<Integer,String> waitList;


    public Indexer() {
        this.dictianary =  new HashMap<>();
        nextLineNum = 0;
        waitList = new HashMap<>();
    }

    public void addDoc(MyDocument doc){
        if(doc==null)
            return;
        Map docMap = doc.getTerms();
        for (Object t:docMap.keySet()) {
            String term = t.toString();

            if(!term.toLowerCase().equals(term.toUpperCase())) { // big and small are different

                if(term.toLowerCase().equals(term)) { // is lower case
                    if (dictianary.containsKey(term.toUpperCase())) {
                        ///// a bigger allready exists
                        int line = dictianary.get(term.toUpperCase());
                        dictianary.remove(term.toUpperCase());
                        dictianary.put(term, line);
                    }
                }
                else{
                    // is upper case
                    if (dictianary.containsKey(term.toLowerCase())){
                        term = term.toLowerCase();
                    }
                }
            }




            if(!dictianary.containsKey(term)){
                dictianary.put(term,nextLineNum);
                nextLineNum++;
            }
            //(docid,number Of times term appears,max frequancy)
            String entry =  "["+term+"]:("+doc.getDocId()+","+ docMap.get(term)  + "," + doc.getMaxFrequency() + ")~";
            if(!waitList.containsKey(dictianary.get(term))) {
                waitList.put(dictianary.get(term), entry);
            }
            else {
                entry = waitList.get(dictianary.get(term))+entry;
                waitList.replace(dictianary.get(term),entry);

            }

//            System.out.println("testing  ...  "+ term.toString());

        }
    }


    public void printWaitList(){
        for (int ent:waitList.keySet()) {
            System.out.println("line: "+ ent + " "  +waitList.get(ent));
        }
    }
    public void printTermlist(){
        for (String ent:dictianary.keySet()) {
            System.out.println("term: {"+ent+"] line: "+ dictianary.get(ent));
        }
    }

}
