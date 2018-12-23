package Searcher;

import Parser.Parse;
import Parser.UpperCaseEntity;
import Ranker.Ranker;
import javafx.util.Pair;
import processing.MyDocument;
import processing.ReadFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Searcher {

    private String quary;
    private Map<String,Pair<Integer,Integer>> quaryMap;
    private  String corpusPath;
    private String outPutPath;
    private boolean usestemmer;
    private boolean useSemantics;
    private HashSet<String> cityFilters;
    private Ranker ranker;
    private String dataPath;
    private TreeSet<String> filteredDocs;
    private double averageTermCount;
    private int docAmount;

    /**
     * constructor
     * @param quaryText - the query
     * @param corpPath - the path where the stp word are
     * @param stemmer - indicator if the stemmer is in use
     * @param outPath - the path where all the disc data is at
     * @param semantics - indicator if the semantics are in use
     * @param citysFilter - a list of cities that the are to be filtered
     * @throws Exception - inner functions exceptions
     */
    public Searcher(String quaryText, String corpPath, boolean stemmer, String outPath, boolean semantics, HashSet<String> citysFilter) throws Exception {
        System.out.println(corpPath);
        System.out.println(outPath);
        quary = quaryText;
        corpusPath = corpPath;
        outPutPath = outPath;
        usestemmer=stemmer;
        useSemantics = semantics;
        cityFilters = citysFilter;

        filteredDocs = new TreeSet<>();

        if(stemmer)
            dataPath = outPutPath+"\\dataBase\\stemmed\\";
        else dataPath = outPutPath+"\\dataBase\\not stemmed\\";

        getFilteredDocs();

        Parse parser = new Parse(corpPath,quary,stemmer);
        try {
            parser.parse();
            quaryMap = parser.getDocMap();
            for (String term:quaryMap.keySet()
                 ) {
                System.out.println(term);// yaniv
            }

        }catch (Exception e){
            System.out.println("error searcher constructor 1");
            throw new Exception("parser failure");
        }

        getRankData();
        ranker = new Ranker(dataPath,quaryMap,filteredDocs,averageTermCount,docAmount);

//        if (!semantics)
//            ranker = new Ranker


    }

    /**
     * this function trigers the Ranker to retrieve the ranked documents
     * for each document returned, the document will be loaded from the disk
     * @return - the top ranked document of the query
     */
    public PriorityQueue<MyDocument> getSearchResault(){
        ReadFile readFile = new ReadFile(outPutPath.substring(0,outPutPath.length()-1),corpusPath);
        PriorityQueue<MyDocument> reaults = ranker.getTopNDocs(50);
        for (MyDocument doc:reaults) {
            doc.setDoc(readFile.getDocument(doc.getDocId()+"").getDoc());
        }
        return reaults;
    }

    /**
     * translates the filtered cities to the document ID that hold the cities
     * @throws Exception - cityIndex.txt path not found
     */
    private void getFilteredDocs() throws Exception {
        if (cityFilters == null || cityFilters.isEmpty())
            return;

        String path = dataPath+"cityIndex.txt";
        try{
            File cityIndex = new File(path);
            FileReader fr = new FileReader(cityIndex);
            BufferedReader bf = new BufferedReader(fr);

            String currentCity;
            while (bf.ready()){
                currentCity = bf.readLine();
                if(cityFilters.contains(currentCity.split("=")[0]))
                    addCityDocs(currentCity.split("@")[1]);
            }

        }
        catch (Exception e){
            throw new Exception("path not found:\n"+ path);

        }
    }

    /**
     * adds the document to the filteredDocs list
     * @param s - the city data line (cityIdx)
     * @throws Exception
     */
    private void addCityDocs(String s) throws Exception {
        try {
            String[] allData = s.split("~");
            int currentDoc = 0;
            for (String doc : allData) {
                currentDoc += Integer.valueOf(doc.split("\\*")[0]);
                filteredDocs.add(currentDoc + "");
            }
        }
        catch (Exception e){
            throw new Exception("cityIdx bad Format error!!");
        }
    }

    /**
     * retrieves from the disc the AverageTermCount in the dataBase
     * @return - the AverageTermCount
     * @throws Exception - averageTermCount.txt path not found
     */
    private void getRankData() throws Exception {
        String path = dataPath + "rankerInfo.txt";
        try {
            File f = new File(path);
            FileReader fr = new FileReader(f);
            BufferedReader bf = new BufferedReader(fr);
            String line = bf.readLine();
            averageTermCount =  Double.valueOf(line);
            line = bf.readLine();
            docAmount = Integer.valueOf(line);

            //////////////////////////////////////////////////////////////
            System.out.println("averageTermCount = "+averageTermCount);
            System.out.println("totalDocsInCorpus = "+ docAmount);
            ////////////////////////////////////////////////////////////// yaniv
            bf.close();

        }
        catch (Exception e){
            throw new Exception("path not found:\n"+path);
        }
    }

    /**
     * given a document, this function will return the 5 top ranked enteties in the document
     * @param document - the document
     * @return - the 5 top ranked entities in the document
     * @throws Exception - parser exceptions and no entities exception
     */
    public ArrayList<UpperCaseEntity> getFiveEnteties(MyDocument document)throws Exception{
        ArrayList<UpperCaseEntity> ans;
        try {
            Parse parser = new Parse(corpusPath, "", usestemmer);
            parser.setTxt(document.getTxt(), "");
            parser.parse();
            ans = parser.getFiveTopEnteties();

        }
        catch (Exception e){
            throw new Exception("parser error (searcher: getFiveEntities)");
        }
        if(ans.size() == 0)
            throw new Exception("no entities exist in this document");
        return ans;
    }
}
