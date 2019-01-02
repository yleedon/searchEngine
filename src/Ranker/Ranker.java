package Ranker;

import Indexer.DicEntry;
import javafx.util.Pair;
import processing.MyDocument;
import java.io.*;
import java.util.*;

public class Ranker {

    private Map<String, DicEntry> dictianary;
    private String outPut;
    private Map<String, Pair<Integer, Integer>> quaryMap;
    private TreeSet<String> filteredDocs;
    private boolean filterOn;
    private double avrageTermCount;
    private int numOfDocsInCorpus;
    private PriorityQueue<TermDocData> docDataList;
    private Map<String,Integer> docTermCountMap;
    private Map<Integer,String> postingListMap;


    /**
     * constructor, retrieves all the necessary data for ranking the documents.
     *
     * @param dataPath         - the path where all the disc data is at
     * @param quaryMap         - the terms in the quarry with the frequency
     * @param filteredDocs     - the filtered docs by city
     * @param averageTermCount - the total average term count in the dataBase
     * @param dictianary
     * @throws Exception - from private functions (I/O)
     */
    public Ranker(String dataPath, Map<String, Pair<Integer, Integer>> quaryMap, TreeSet<String> filteredDocs, double averageTermCount, int numOfDocsInCorpus, Map<String, DicEntry> dictianary) throws Exception {

        this.dictianary = dictianary;
        this.outPut = dataPath;
        this.quaryMap = quaryMap;
        this.filteredDocs = filteredDocs;
        this.avrageTermCount = averageTermCount;
        this.numOfDocsInCorpus = numOfDocsInCorpus;
        this.docDataList = new PriorityQueue<>();

        if (filteredDocs == null || filteredDocs.size() == 0)
            filterOn = false;
        else filterOn = true;
        loadDocsTermCount();
        getReleventDocs();
    }

    /**
     * loads the "docTermCountMap" from "docIdx"
     * @throws Exception - I/O
     */
    private void loadDocsTermCount() throws Exception {
        try {
            docTermCountMap = new HashMap<>();
            File docIdx = new File(outPut + "docIdx.txt");
            FileReader fr = new FileReader(docIdx);
            BufferedReader bf = new BufferedReader(fr);
            String line;
            int docNum = 1;
            while (bf.ready()){
                line = bf.readLine();
                if(line.equals(""))
                    continue;
                docTermCountMap.put(docNum+"",Integer.valueOf(line.split(",")[5]));
                docNum++;
            }
            bf.close();

        } catch (Exception e) {
            throw new Exception("docIdx.txt not found\nRanker_loadDocTermCOunt");
        }
    }

    /**
     * goes over each term in the query, gets its posting list line and creates adds the data to the "docDataList"
     *
     * @throws Exception
     */
    private void getReleventDocs() throws Exception {
        deCapitalizedMap();

        createPostinglistmap();

        for (String term : quaryMap.keySet()) {
            if (dictianary.containsKey(term)) {
                String posting = postingListMap.get(dictianary.get(term).getId());
                String[] docs = posting.split(":")[1].split("~");
                createTermDocData(docs, term);
            }
        }
        if (docDataList.isEmpty())
            throw new Exception("No results found for your query");
    }

    /**
     * creates the postingList Map by retrieving specific data from postingList.txt
     * @throws Exception
     */
    private void createPostinglistmap() throws Exception {
        TreeSet<Integer> postinglistSet = new TreeSet<>();
        for (String term : quaryMap.keySet()) {
            if (dictianary.containsKey(term)) {
                postinglistSet.add(dictianary.get(term).getId());
            }
        }
        PriorityQueue<Integer> postingLines = new PriorityQueue<>();
        for (int termId:postinglistSet){
            postingLines.add(termId);
        }

        loadPostingList(postingLines);
    }

    /**
     * adds all the relative lines from the posting list to the postingList MAP
     * @param postingLines - the relative lines (sorted from small to big)
     * @throws Exception - I/O Exceptions
     */
    private void loadPostingList(PriorityQueue<Integer> postingLines) throws Exception {
        postingListMap = new HashMap<>();
        int lineNumber;
        int currentLine = 0;
        String line;

        File postingList = new File(outPut + "postingList.txt");
        try {
            FileReader fr = new FileReader(postingList);
            BufferedReader bf = new BufferedReader(fr);
            while (!postingLines.isEmpty()) {
                lineNumber = postingLines.poll();
                while (currentLine!=lineNumber){
                    bf.readLine();
                    currentLine++;
                }
                line = bf.readLine();
                postingListMap.put(lineNumber,line);
                currentLine++;
            }
        } catch (Exception e) {
            throw new Exception("posting list not found -- getPosting");
        }
    }

    /**
     * changes the querry case sensitivity in accordance to the dictionary.
     * @throws Exception  - not supposed to happen
     */
    private void deCapitalizedMap() throws Exception {
        try {
            Map<String, Pair<Integer, Integer>> tempMap = new TreeMap<>();

            for (String term : quaryMap.keySet()) {

                if (!dictianary.containsKey(term)) {
                    Pair<Integer, Integer> currentPair = quaryMap.get(term);
                    term = capitalSensitivetyFix(term);
                    tempMap.put(term, currentPair);
                }
                else tempMap.put(term,quaryMap.get(term));
            }
            quaryMap = tempMap;
        } catch (Exception e){
            throw new Exception("deCapitalizedMap error (Ranker)");
        }
    }

    /**
     * this function makes the query not case sensitive (for upper or lower case terms)
     * checks if the term exists in a capital state, if so return that state
     *
     * @param term - the term that is to become not case sensitive
     * @return - the term in a capital state the exists in the dittionary, or the term as is if does not exist in dictionary
     */
    private String capitalSensitivetyFix(String term) {
        if (dictianary.containsKey(term.toUpperCase()))
            return term.toUpperCase();
        if (dictianary.containsKey(term.toLowerCase()))
            return term.toLowerCase();
        return term;
    }

    /**
     * givven a posting list line, this function splits the line by docs (and fixes the gaps) and retrieves the information
     * this function takes into account the "city Filter" (will process only cities selected if selected)
     *
     * @param docs - the list of doxs+info from the posting list
     * @param term - the term that the docs belong to
     * @throws Exception - "addTermDocs()" exceptions
     */
    private void createTermDocData(String[] docs, String term) throws Exception {
        int curretnDoc = 0;
        int gap;
        for (String docInfo : docs) {
            String[] docInfoSplit = docInfo.split(",");
            gap = Integer.valueOf(docInfoSplit[0]);
            curretnDoc += gap;

            docInfoSplit[0] = curretnDoc + "";

            if (filterOn && !filteredDocs.contains(docInfoSplit[0]))
                continue;

            addTermDocData(docInfoSplit, term);
        }
    }

    /**
     * given  the information from the posting list, creates a "TermDocData" object and adds it to the priority queue.
     *
     * @param docInfo - the information of a certain document in the posting list (without gaps)
     * @param term    - the term that the document belongs to
     * @throws Exception - thrown from "getDocTotalTermAmount()"
     */
    private void addTermDocData(String[] docInfo, String term) throws Exception {
        boolean isInTitle = false;
        if (docInfo[2].contains("@")) {
            docInfo[2] = docInfo[2].replace("@", "");
            isInTitle = true;
        }
        int docTotalTermAmount = docTermCountMap.get(docInfo[0]);

        TermDocData termDocData = new TermDocData(Integer.valueOf(docInfo[0]), isInTitle, Integer.valueOf(docInfo[2]), Integer.valueOf(docInfo[1]), term, docTotalTermAmount);
        docDataList.add(termDocData);
    }

    /**
     * gets the top documents by rank
     * @param N - the number of document to return
     * @return a priority queue by rank of the top N documents
     */
    public PriorityQueue<MyDocument> getTopNDocs(int N) {
        PriorityQueue<MyDocument> ans = new PriorityQueue<>(Comparator.reverseOrder());
        PriorityQueue<MyDocument> minHeap = new PriorityQueue<>();
        int qSize = 0;
        TermDocData currentDoc;
        MyDocument myDoc;
        ArrayList<TermDocData> docTerms;
        while (!docDataList.isEmpty()) {
            docTerms = new ArrayList<TermDocData>();
            currentDoc = docDataList.poll();
            docTerms.add(currentDoc);
            while (!docDataList.isEmpty() && currentDoc.getDocId() == docDataList.peek().getDocId()) {
                docTerms.add(docDataList.poll());
            }
            myDoc = new MyDocument(currentDoc.getDocId(), BM25(docTerms));
            if (qSize < N) {
                ans.add(myDoc);
                minHeap.add(myDoc);
                qSize++;
            } else if (minHeap.peek().getRank() < myDoc.getRank()) {
                ans.remove(minHeap.poll());
                ans.add(myDoc);
                minHeap.add(myDoc);
            }
        }
        return ans;
    }

    /**
     * calculating the BM25 formula
     * @param docTerms - the terms of the doc and query
     * @return the rank of the document
     */
    private double BM25(ArrayList<TermDocData> docTerms) {
        double rank = 0;
        int cwq, cwd, dLen, df;
        double b = 0.25, k = 1.6;
        for (TermDocData tdd : docTerms) {
            cwq = quaryMap.get(tdd.getTerm()).getKey();
            cwd = tdd.getFrequency();
            dLen = tdd.getDocTotalAmountofTerms();
            df = dictianary.get(tdd.getTerm()).numOfDocs;
            rank += ((cwq * (k + 1) * cwd) / (cwd + k * (1 - b + b * (dLen / avrageTermCount)))) * Math.log((numOfDocsInCorpus + 1) / df);
//            System.out.println(tdd.getDocId() +"  "+ tdd.getTerm());
            rank += tdd.getRelativePlace()/9; // += (0-1]
            if(tdd.isInTitle())
                rank = rank*1.4;
            rank = Math.floor(rank*10000)/10000;
        }

        return rank;

    }

}
