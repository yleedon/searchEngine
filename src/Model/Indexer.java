package Model;


import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Indexer {

    private double tempFileSize;
    private int waitlistSize;
    private Map<String,DicEntry> dictianary;// term:[id,numOfDocs,totalFrequancy]
    private int nextLineNum;
    private Map<Integer,String> waitList;
    private int tempFileName;
    private String path;


    public Indexer(String outPath, double size) {
        path = outPath;
        tempFileName = 1;
        waitlistSize=0;
        this.dictianary =  new TreeMap<>();
        nextLineNum = 0;
        waitList = new TreeMap<>();
        tempFileSize = size*1000000;

    }

    public void addDoc(MyDocument doc){
        if(doc==null)
            return;
        Map<String, Pair<Integer,Integer>> docMap = doc.getTerms();
        for (Object t:docMap.keySet()) {
            if(t.toString().equals(""))
                continue;

            String term = t.toString();
            String originalTerm = term;
            if(!term.toLowerCase().equals(term.toUpperCase())) { // big and small are different

                if(term.toLowerCase().equals(term)) { // is lower case

                    if (dictianary.containsKey(term.toUpperCase())) {
                        ///// a bigger allready exists
                        DicEntry oldEntry = dictianary.get(term.toUpperCase());
//                        int line = dictianary.get(term.toUpperCase());
                        dictianary.remove(term.toUpperCase());
                        dictianary.put(term, oldEntry);
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
                DicEntry dicEntry = new DicEntry(nextLineNum);
                dicEntry.incrementNumOfDocs();
                dicEntry.addTotalFrequency(docMap.get(originalTerm).getKey());
                dictianary.put(term,dicEntry);
                nextLineNum++;
            }
            else {
                dictianary.get(term).incrementNumOfDocs();
                dictianary.get(term).addTotalFrequency(docMap.get(originalTerm).getKey());
            }
            //(docid,number Of times term appears,relative first appearence)

            double termPlace = (1-(double)docMap.get(originalTerm).getValue()/doc.getTextTokenCount());
            termPlace = Math.floor(termPlace * 10);
            String entry = doc.getDocId()+","+ docMap.get(originalTerm).getKey()+","+(int)termPlace+ "," + doc.isInTitle(originalTerm);
            if(!waitList.containsKey(dictianary.get(term).getId())) {
                waitlistSize+= (""+dictianary.get(term).getId()).length()+1+entry.length();
                waitList.put(dictianary.get(term).getId(),  dictianary.get(term).getId()+":"+entry);
            }
            else {
                waitlistSize+=entry.length();
                entry = waitList.get(dictianary.get(term).getId())+"~"+entry;
                waitList.replace(dictianary.get(term).getId(),entry);
            }
        }
        ;
        if (waitlistSize > tempFileSize) { // file size 300kb
            writeWaitingList();
            System.out.println("waiting list was written to disk");
        }
    }

    public void writeWaitingList() {
        try {
            String fName = ""+tempFileName;
            String tempPath = path+"/waitingList/"+fName+".txt";
            File tempFile = new File(tempPath);
            FileWriter fileWriter = new FileWriter(tempFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter writer = new PrintWriter(bufferedWriter);
            writer.flush();
            String lines = "";

            for(int line:waitList.keySet()){
                writer.flush();
                writer.println(waitList.get(line));
            }
            writer.close();

        }
        catch (Exception e){
            System.out.println("error index "+e.getMessage());
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


    public void saveDictinary() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String tempPath = path+"/dictionary.txt";
            File tempFile = new File(tempPath);
            FileWriter fileWriter = new FileWriter(tempFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter writer = new PrintWriter(bufferedWriter);
            writer.flush();

            for (String term : dictianary.keySet()) {
                writer.flush();
                writer.println(term+","+dictianary.get(term));
            }
            writer.close();

        } catch (Exception e) {
            System.out.println("error indexer");
            System.out.println(e.getMessage());
        }
    }

    public void reset() {
        dictianary.clear();
        dictianary=null;

        waitList.clear();
        waitList = null;

    }

    public Map<String,DicEntry> getDictianary(){
        return dictianary;
    }
}
