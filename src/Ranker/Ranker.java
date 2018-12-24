package Ranker;

import Indexer.DicEntry;
import javafx.util.Pair;
import processing.MyDocument;

import java.io.*;
import java.util.*;

public class Ranker implements IRanker {

    private TreeMap<String, DicEntry> dictianary;
    private String outPut;
    private Map<String, Pair<Integer, Integer>> quaryMap;
    private TreeSet<String> filteredDocs;
    private boolean filterOn;
    private double avrageTermCount;
    private int numOfDocsInCorpus;
    private PriorityQueue<TermDocData> docDataList;

    /**
     * constructor, retrieves all the necessary data for ranking the documents.
     *
     * @param dataPath         - the path where all the disc data is at
     * @param quaryMap         - the terms in the quarry with the frequency
     * @param filteredDocs     - the filtered docs by city
     * @param averageTermCount - the total average term count in the dataBase
     * @throws Exception - from private functions (I/O)
     */
    public Ranker(String dataPath, Map<String, Pair<Integer, Integer>> quaryMap, TreeSet<String> filteredDocs, double averageTermCount, int numOfDocsInCorpus) throws Exception {
        this.outPut = dataPath;
        this.quaryMap = quaryMap;
        this.filteredDocs = filteredDocs;
        this.avrageTermCount = averageTermCount;
        this.numOfDocsInCorpus = numOfDocsInCorpus;
        this.docDataList = new PriorityQueue<>();

        if (filteredDocs == null || filteredDocs.size() == 0)
            filterOn = false;
        else filterOn = true;

        loadDictionary();
        getReleventDocs();
    }

    /**
     * goes over each term in the query, gets its posting list line and creates adds the data to the "docDataList"
     *
     * @throws Exception
     */
    private void getReleventDocs() throws Exception {
        deCapitalizedMap();
        for (String term : quaryMap.keySet()) {
            if (dictianary.containsKey(term)) {
                String posting = getPosting(dictianary.get(term).getId());
                String[] docs = posting.split(":")[1].split("~");

                createTermDocData(docs, term);
            }
        }
        if (docDataList.isEmpty())
            throw new Exception("No results found for your query");
    }

    private void deCapitalizedMap() {
        for (String term : quaryMap.keySet()) {
            if (!dictianary.containsKey(term)) {
                Pair<Integer, Integer> currentPair = quaryMap.get(term);
                quaryMap.remove(term);
                term = capitalSensitivetyFix(term);
                quaryMap.put(term, currentPair);
            }
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
        int docTotalTermAmount = getDocTotalTermAmount(Integer.valueOf(docInfo[0]));

        TermDocData termDocData = new TermDocData(Integer.valueOf(docInfo[0]), isInTitle, Integer.valueOf(docInfo[2]), Integer.valueOf(docInfo[1]), term, docTotalTermAmount);
        docDataList.add(termDocData);
    }

    /**
     * retrieves the totalTermCount in the givven document
     *
     * @param doc - the documrnt ID
     * @return - the total amount of terms in the document
     * @throws Exception - doxIdx,txt psth not found
     */
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

        } catch (Exception e) {
            throw new Exception("docIdx.txt not found\nRanker_GetTotalTermAmount (docNumber: " + doc + " )");
        }
    }

    /**
     * retrieves the terms posting list
     *
     * @param id - term ID
     * @return - the posting line of the term
     * @throws Exception - posting list path not found
     */
    private String getPosting(int id) throws Exception {
        File postingList = new File(outPut + "postingList.txt");
        try {
            FileReader fr = new FileReader(postingList);
            BufferedReader bf = new BufferedReader(fr);
            for (int i = 0; i < id; i++) {
                bf.readLine();
            }
            return bf.readLine();
        } catch (Exception e) {
            throw new Exception("posting list not found -- getPosting");
        }
    }

    /**
     * loads the dictionary from the disc
     *
     * @throws Exception - dictionary path not found
     */
    public void loadDictionary() throws Exception {

        File file = new File(outPut + "dictionary.txt");
        if (!file.exists()) {
            throw new Exception("dictionary not found:\n" + outPut + "dictionary.txt");
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

    private double BM25(ArrayList<TermDocData> docTerms) {
        double rank = 0;
        int cwq, cwd, dLen, df;
        double b = 0.75, k = 1.5;
        for (TermDocData tdd : docTerms) {
            cwq = quaryMap.get(tdd.getTerm()).getKey();
            cwd = tdd.getFrequency();
            dLen = tdd.getDocTotalAmountofTerms();
            df = dictianary.get(tdd.getTerm()).numOfDocs;
            rank += ((cwq * (k + 1) * cwd) / (cwd + k * (1 - b + b * (dLen / avrageTermCount)))) * Math.log((numOfDocsInCorpus + 1) / df);
        }
        return rank;
    }
}
