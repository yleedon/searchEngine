package Model;


import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Indexer {


    private int waitlistSize;
    private Map<String,Integer> dictianary;
    private int nextLineNum;
    private Map<Integer,String> waitList;
    private int tempFileName;


    public Indexer() {
        tempFileName = 1;
        waitlistSize=0;
        this.dictianary =  new HashMap<>();
        nextLineNum = 0;
        waitList = new TreeMap<>();

    }

    public void addDoc(MyDocument doc){
        if(doc==null)
            return;
        Map docMap = doc.getTerms();
        for (Object t:docMap.keySet()) {
            if(t.toString().equals(""))
                continue;

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
            String entry = "(id:"+doc.getDocId()+",freq:"+ docMap.get(term)  + ",maxFreq" + doc.getMaxFrequency() + ")~";
            if(!waitList.containsKey(dictianary.get(term))) {
                waitlistSize+= (""+dictianary.get(term)).length()+1+entry.length();
                waitList.put(dictianary.get(term),  dictianary.get(term)+":"+entry);
            }
            else {
                waitlistSize+=entry.length();
                entry = waitList.get(dictianary.get(term))+entry;
                waitList.replace(dictianary.get(term),entry);
            }

//            System.out.println("testing  ...  "+ term.toString());

        }
        if (waitlistSize > 300000) { // file size 300kb
            writeWaitingList();
            System.out.println("write waiting list to disk");
        }
    }

    public void writeWaitingList() {
        try {

            String fName = ""+tempFileName;
            ClassLoader classLoader = getClass().getClassLoader();
            String tempPath = classLoader.getResource("").getPath()+"Resources/waitingList/"+fName+".txt";
            File tempFile = new File(tempPath);
            FileWriter fileWriter = new FileWriter(tempFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter writer = new PrintWriter(bufferedWriter);
            writer.flush();

            for(int line:waitList.keySet()){
                writer.flush();
                writer.println(waitList.get(line));
            }
            writer.flush();
            writer.close();




        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        tempFileName++;
        waitlistSize=0;
        waitList=new TreeMap<>();



    }


    public void printWaitList(){
        for (int ent:waitList.keySet()) {
            System.out.println(waitList.get(ent));
        }
    }
    public void printTermlist(){
        for (String ent:dictianary.keySet()) {
            System.out.println("term: ["+ent+"] line: "+ dictianary.get(ent));
        }
    }
    public void printWaitListSize(){
        System.out.println("waitlist size = "+ (double)waitlistSize/1000000+"MB");
    }

}
