package Ranker;

import Indexer.DicEntry;
import javafx.util.Pair;
import processing.MyDocument;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Ranker implements IRanker {

    private TreeMap<String, DicEntry> dictianary;
    private String outPut;
    private Map<String, Pair<Integer, Integer>> quaryMap;
    private TreeSet<String> filteredDocs;
    private boolean filterOn;

    public Ranker(String dataPath, Map<String, Pair<Integer, Integer>> quaryMap, TreeSet<String> filteredDocs) throws Exception {
        this.outPut = dataPath;
        this.quaryMap = quaryMap;
        this.filteredDocs = filteredDocs;
        if(filteredDocs == null || filteredDocs.size() ==0 )
            filterOn = false;
        else filterOn = true;

        loadDictionary();
        getReleventDocs();
    }

    private void getReleventDocs() throws Exception {
        for(String term:quaryMap.keySet()){
            if(dictianary.containsKey(term)){
                String posting = getPosting(dictianary.get(term).getId());
                String[] docs = posting.split(":")[1].split("~");

                createTermDocData(docs,term);


            }
        }
    }

    private void createTermDocData(String[] docs,String term) throws Exception {
        ArrayList<MyDocument> ans = new ArrayList<>();
        int curretnDoc = 0;
        int gap ;
        for(String docInfo:docs){
            String[] docInfoSplit = docInfo.split(",");
            gap = Integer.valueOf(docInfoSplit[0]);
            curretnDoc+=gap;

            docInfoSplit[0] = curretnDoc+"";

            if(filterOn && !filteredDocs.contains(docInfoSplit[0]) )
                continue;

            addTermDocData(docInfoSplit,term);

        }

    }
    //check posting list for correctness!!!

    private void addTermDocData(String[] docInfo, String term) throws Exception {
        boolean isInTitle = false;
        if(docInfo[2].contains("@")){
            docInfo[2]=docInfo[2].replace("@","");
            isInTitle=true;
        }
        int docTotalTermAmount = getDocTotalTermAmount(Integer.valueOf(docInfo[0]));

        TermDocData termDocData = new TermDocData(Integer.valueOf(docInfo[0]),isInTitle,Integer.valueOf(docInfo[2]),Integer.valueOf(docInfo[1]),term,docTotalTermAmount);
        ///////////////////////////////////// yaniv

        System.out.println(termDocData);
        ///////////////////////////////////////
    }

    private int getDocTotalTermAmount(int doc) throws Exception {
        try {
            File docIdx = new File(outPut + "docIdx.txt");
            FileReader fr = new FileReader(docIdx);
            BufferedReader bf = new BufferedReader(fr);
            String line;
            for (int i = 1; i < doc; i++) {
                bf.readLine();
            }
            line = bf.readLine();
            bf.close();
            String totalTermsAmount = line.split(",")[5];
            return Integer.valueOf(totalTermsAmount);

        }
        catch (Exception e){
            throw new Exception("docIdx.txt not found\nRanker_GetTotalTermAmount (docNumber: "+doc+" )");
        }


    }

    private String getPosting(int id) throws Exception {
        File postingList = new File(outPut+"postingList.txt");
        try{
            FileReader fr = new FileReader(postingList);
            BufferedReader bf = new BufferedReader(fr);
            for(int i = 0; i < id;i++){
                bf.readLine();
            }
            return bf.readLine();
        }
        catch (Exception e){
            throw new Exception("posting list not found -- getPosting");
        }
    }


    public void loadDictionary() throws Exception {

        File file = new File(outPut+"dictionary.txt");
        if (!file.exists()) {
           throw new Exception("dictionary not found:\n"+outPut+"dictionary.txt");
        }

        dictianary = new TreeMap<>();
        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            String[] line;
            String[] data;
            while ((st = br.readLine()) != null) {
                if (st.equals("")) {
                    continue;
                }

                line = st.split(":");
                data = line[1].split(",");
                DicEntry entry = new DicEntry(Integer.valueOf(data[0]));
                entry.numOfDocs = Integer.valueOf(data[1]);
                entry.totalTermFrequency = Integer.valueOf(data[2]);
                dictianary.put(line[0], entry);
            }


            br.close();

        } catch (Exception e) {
           throw new Exception("load dictionary error");
        }
    }
}
