package Searcher;

import Parser.Parse;
import javafx.util.Pair;

import java.util.Map;

public class Searcher {

    private String quary;
    private Map<String,Pair<Integer,Integer>> quaryMap;
    private  String corpusPath;
    private String outPutPath;
    private boolean usestemmer;



    public Searcher(String quaryText, String corpPath, boolean stemmer, String outPath) {
        quary = quaryText;
        corpusPath = corpPath;
        outPutPath = outPath;
        usestemmer=stemmer;


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
