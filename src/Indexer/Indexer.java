package Indexer;


import Merge.MergeFile;
import processing.MyDocument;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer {

    private double tempFileSize;
    private int waitlistSize;
    private Map<String, DicEntry> dictianary;
    private int nextLineNum;
    private Map<Integer,String> waitList;
    private AtomicInteger tempFileName;
    private String path;
    private int waitFolderId;
    private int numOflistsInCurrrentFolder;
    private List<Thread> miniThreadList;
    private List<Thread> bigThreadList;


    /**
     * Constructor: createsall needed feilds including the first temp posting directory "w1"
     * @param outPath - the output path were the data is to be saved
     * @param size - the size of the temporary posting lists
     */
    public Indexer(String outPath, double size) {
        miniThreadList = new ArrayList<>();
        bigThreadList = new ArrayList<>();
        tempFileName = new AtomicInteger(0);
        path = outPath;
        waitlistSize=0;
        this.dictianary =  new TreeMap<>();
        nextLineNum = 0;
        waitList = new TreeMap<>();
        tempFileSize = size*1000000;
        waitFolderId = 1;
        createDirectory(path+"\\waitingList\\w1");

    }

    /**
     * adds the doc terms to the dictionary and updates the terms data, also creates posting lists and writes them to the disk
     * @param doc - the doc that is to be added.
     */
    public void addDoc(MyDocument doc){
        if(doc==null)
            return;
        Map<String, Pair<Integer,Integer>> docMap = doc.getTerms();
        for (Object t:docMap.keySet()) {
            if(t.toString().equals(""))
                continue;

            String term = t.toString();
            String originalTerm = term;
            //deal with capital or lowerCase if allready exists in dic
            if(!term.toLowerCase().equals(term.toUpperCase())) { // big and small are different

                if(term.toLowerCase().equals(term)) { // is lower case

                    if (dictianary.containsKey(term.toUpperCase())) {
                        DicEntry oldEntry = dictianary.get(term.toUpperCase());
                        dictianary.remove(term.toUpperCase());
                        dictianary.put(term, oldEntry);
                    }
                }
                else{
                    if(term.toUpperCase().equals(term)) // is upper case
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
            String inTitleValue = "";
            if(doc.isInTitle(originalTerm)==1)
                inTitleValue = "@";
            String entry = ","+ docMap.get(originalTerm).getKey()+","+(int)termPlace + inTitleValue ;
            if(!waitList.containsKey(dictianary.get(term).getId())) {

                ///////////////////////////////////////////////////////
                entry=doc.getDocId()+entry;
                dictianary.get(term).setLastDocin(doc.getDocId());
                ////////////////////////////////////////////////////

                waitlistSize+= (""+dictianary.get(term).getId()).length()+1+entry.length();
                waitList.put(dictianary.get(term).getId(),  dictianary.get(term).getId()+":"+entry);
            }
            else {
                int gap = doc.getDocId();
                if(doc.getDocId()!=dictianary.get(term).getLastDocin())
                    gap = getGap(term,gap);
                else {

                    //yaniv ****************************************
                    int id = doc.getDocId();
                    int termId = dictianary.get(term).getId();
                    String ttt = term;
                    System.out.println("333");
//                    ************************************************

                }
                entry = gap + entry;
                waitlistSize+=entry.length()+1;//(~)
                entry = waitList.get(dictianary.get(term).getId())+"~"+entry;
                waitList.replace(dictianary.get(term).getId(),entry);
                dictianary.get(term).setLastDocin(doc.getDocId());
            }
        }
        ;
        if (waitlistSize > tempFileSize) { // file size 300kb
            waitlistSize=0;

            Map temp = waitList;
            waitList = new TreeMap<>();//yaniv

            if(numOflistsInCurrrentFolder>99){

                int folderToMerge = waitFolderId;
                List<Thread> tempThreadList = miniThreadList;
                Thread bigThread = new Thread(()-> mergeSingleFolder(folderToMerge,tempThreadList));
                bigThreadList.add(bigThread);
                bigThread.start();
                miniThreadList = new ArrayList<>();
                numOflistsInCurrrentFolder=0;
                waitFolderId++;
                createDirectory(path+"/waitingList/w"+waitFolderId);
            }
            numOflistsInCurrrentFolder++;
//            System.out.println("waiting list started writting to disk");
            Thread t = new Thread(()->writeWaitingList(temp,waitFolderId));
            miniThreadList.add(t);
            t.start();

        }
    }

    /**
     * merges the last temp directory with posting lists
     */
    public void mergeLastMiniFolded(){
        mergeSingleFolder(waitFolderId,miniThreadList);
    }

    /**
     * merges a mini posting lists in a given directory
     * @param folderId - the folerId that is to be merged
     * @param tList - the thread list of all the threads that have been writing to the Directory ("W"+foldierId)
     */
    private void mergeSingleFolder(int folderId, List<Thread> tList) {
        try {
//            System.out.println("merging w"+folderId);
            for (Thread t:tList){
                t.join();
            }
            MergeFile mergeFile = new MergeFile(path+"\\waitingList\\w"+folderId,path+"\\waitingList\\"+folderId);
            mergeFile.merge();
//            System.out.println("w"+folderId + "has been merged");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error eror mother fucker");
        }

    }

    /**
     * writes the current waiting list to the disk
     * @param CurrentWaitList - the current waiting list that is to be saved to the disk (not used any more)
     * @param folderId - the directory in which the waiting list is to be saved.
     */
    private void writeWaitingList(Map<Integer, String> CurrentWaitList,int folderId) {
        try {
            String fName = ""+ tempFileName.incrementAndGet();
            String tempPath = path+"\\waitingList\\w"+folderId+"\\"+fName+".txt";
            File tempFile = new File(tempPath);
            FileWriter fileWriter = new FileWriter(tempFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter writer = new PrintWriter(bufferedWriter);
            writer.flush();
            String lines = "";

            for(int line:CurrentWaitList.keySet()){
                writer.flush();
                writer.println(CurrentWaitList.get(line));
            }
            writer.close();
        }
        catch (Exception e){
            System.out.println("error index "+e.getMessage());
        }
//        System.out.println("finished writing waitlist to disk");
    }

    /**
     * writes the last waiting list to the disk - file size may range between 0 to "waitListSize"
     */
    public void writeLastWaitingList(){
        if(!waitList.isEmpty())
        writeWaitingList(waitList,waitFolderId);
    }

    /**
     * saves the dictionary to the disk.
     */
    public void saveDictinary() {
        try {
//            System.out.println("started writing dictionary");
            ClassLoader classLoader = getClass().getClassLoader();
            String tempPath = path+"/dictionary.txt";
            File tempFile = new File(tempPath);
            FileWriter fileWriter = new FileWriter(tempFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter writer = new PrintWriter(bufferedWriter);
            writer.flush();

            for (String term : dictianary.keySet()) {
                writer.flush();
                writer.println(term+":"+dictianary.get(term));
            }
            writer.close();
//            System.out.println("finished writing dictionary");

        } catch (Exception e) {
            System.out.println("error indexer");
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @return - the dictionary
     */
    public Map<String, DicEntry> getDictianary(){
        return dictianary;
    }

    /**
     * calculates and returns the gap between the current doc to the last doc
     * @param term - the term that is in session
     * @param docId - the new docId
     * @return
     */
    private int getGap(String term,int docId){
        int last = dictianary.get(term).getLastDocin();
        return docId-last;
    }

    /**
     * creates a directory
     * @param dir - the directory that is to be created
     */
    private void createDirectory(String dir) {
        File output = new File(dir);
        if (!output.exists()) {
//            System.out.println("creating directory: " + dir);
            boolean result = false;

            try{
                output.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
//                System.out.println("DIR "+dir+" created");
            }
        }
    }

    /**
     * merges the final temp osting lists to one file: "postingList.txt"
     */
    public void mergeFinalePostingList(){

        try {
//            System.out.println("Creating Final PostingList");
            MergeFile mg = new MergeFile(path+"\\waitingList",path+"\\postingList.txt");
            mg.merge();
//            System.out.println("Final PostingList created");
        } catch (Exception e) {

            System.out.println("error thrown from mergeFile - function in indexer \"mergeFinalPostingList\"");
        }
    }

    /**
     * writes a dictionary (term, tf) for the user to view
     */
    public void writePresentedDictionary(){
        try{

            File dicFile = new File (path+"\\dicToShow.txt");
            FileWriter fw = new FileWriter(dicFile);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter writer = new PrintWriter(bw);
            writer.flush();

            writer.println("**************DICTIONARY**************");
            if(path.endsWith("not stemmed"))
                writer.println("             not stemmed");
            else writer.println("               stemmed");
            writer.println("           TERM = FREQUENCY");
            writer.println("**************************************");
            writer.println();


            for(String term:dictianary.keySet()){
                writer.println(term + " = "+ dictianary.get(term).totalTermFrequency);
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error writePresentDic");
        }
    }

}
