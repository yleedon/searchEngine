package Searcher;

import Parser.Parse;
import Ranker.Ranker;
import javafx.scene.control.CheckBox;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

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



    public Searcher(String quaryText, String corpPath, boolean stemmer, String outPath, boolean cb_semantics, HashSet<String> citysFilter) throws Exception {
        quary = quaryText;
        corpusPath = corpPath;
        outPutPath = outPath;
        usestemmer=stemmer;
        useSemantics = cb_semantics;
        cityFilters = citysFilter;

        filteredDocs = new TreeSet<>();

        if(stemmer)
            dataPath = outPutPath+"\\dataBase\\stemmed\\";
        else dataPath = outPutPath+"\\dataBase\\not stemmed\\";

        getFilteredDocs();
        for (String doc : filteredDocs)
            System.out.println("doc num:" + doc);







        Parse parser = new Parse(corpPath,quary,stemmer);
        try {
            parser.parse();
            quaryMap = parser.getDocMap();
            for (String term:quaryMap.keySet()
                 ) {
                System.out.println(term);
            }

        }catch (Exception e){
            System.out.println("error searcher constructor 1");
            throw new Exception("parser failure");
        }


    }

    private HashSet<String> getFilteredDocs() throws Exception {
        if (cityFilters == null || cityFilters.isEmpty())
            return null;

        HashSet<String> ans = new HashSet<>();
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

        return null;
    }

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
}
