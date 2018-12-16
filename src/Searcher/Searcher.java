package Searcher;

import Parser.Parse;
import Ranker.Ranker;
import javafx.scene.control.CheckBox;
import javafx.util.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class Searcher {

    private String quary;
    private Map<String,Pair<Integer,Integer>> quaryMap;
    private  String corpusPath;
    private String outPutPath;
    private boolean usestemmer;
    private boolean useSemantics;
    private Collection<String> cityFilters;
    private Ranker ranker;
    private HashSet<String> filteredDocs;



    public Searcher(String quaryText, String corpPath, boolean stemmer, String outPath, boolean cb_semantics, Collection<String> citysFilter) {
        quary = quaryText;
        corpusPath = corpPath;
        outPutPath = outPath;
        usestemmer=stemmer;
        useSemantics = cb_semantics;
        if(citysFilter==null || citysFilter.size()==0)
            this.cityFilters = null;
        else this.cityFilters = citysFilter;




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
            return;
        }


    }
}
