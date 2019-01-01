package Ranker.Semantic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class LSIExecutor {
    private String delimiters = "{[()]}\"\\:";

    /**
     * get a synonym to a given word
     *
     * @param word
     * @return
     */
    public String getSynonyms(String word) {
        String s = word.replaceAll(" ", "+");
        return getWords(getJSON("http://api.datamuse.com/words?rel_syn=" + s), 2500, " ");
    }

    /**
     * spell checking the given word
     *
     * @param word - the given word to spell check
     * @return a spell corrected sentence
     */
    public String spellCheck(String word) {
        String s = word.replaceAll(" ", "+");
        String soundsSimilar = getWords(soundsSimilar(s), 1200, ",");
        String speltSimilar = getWords(speltSimilar(s), 1200, ",");
        StringBuilder ans = new StringBuilder();
        for (String str : speltSimilar.split(",")) {
            if (soundsSimilar.contains(str) && !str.equals(word)) {
                ans.append(str).append(" ");
            }
        }
        String retVal = ans.toString().length()>1 ? ans.toString() : speltSimilar.split(" ")[0];
        return retVal.length() == 0 ? word : retVal;
    }

    /**
     * Find words which are spelt the same as the specified word/phrase.
     *
     * @param word A word or phrase.
     * @return A list of words/phrases which are spelt similar.
     */
    private String speltSimilar(String word) {
        String s = word.replaceAll(" ", "+");
        return getJSON("http://api.datamuse.com/words?sp=" + s);
    }

    /**
     * Find words which sound the same as the specified word/phrase when spoken.
     *
     * @param word A word or phrase.
     * @return A list of words/phrases which sound similiar when spoken.
     */
    private String soundsSimilar(String word) {
        String s = word.replaceAll(" ", "+");
        return getJSON("http://api.datamuse.com/words?sl=" + s);
    }

    /**
     * dividing the entries to words, split by given delimiter
     *
     * @param synonyms  - the entries
     * @param delimiter - the given delimiter
     * @return the words in the entries split by the delimiter
     */
    private String getWords(String synonyms, int scoreLimit, String delimiter) {
        synonyms = synonyms.substring(1, synonyms.length()-1);
        synonyms = synonyms.replace("},{", "}-{");
        String[] wordsData = synonyms.split("-");
        StringBuilder ans = new StringBuilder();
        boolean foundMatch = false;
        for (String word : wordsData) {
            if (getScore(word) > scoreLimit) {
                word = word.split("\",\"")[0];
                word = word.split("\":\"")[1];
                ans.append(word).append(delimiter);
                foundMatch = true;
                break;
            } else
                break;
        }
        return foundMatch ? ans.toString().substring(0, ans.toString().length() - 1) : "";
    }

    /**
     * getter for the score for the word
     *
     * @param wordsData - the entry
     * @return - the score
     */
    private int getScore(String wordsData) {
        if (wordsData.length() < 2)
            return 0;
        String score = wordsData.split(",")[1].split(":")[1];
        while (delimiters.contains("" + score.charAt(score.length() - 1))) {
            score = score.substring(0, score.length() - 1);
        }
        return Integer.parseInt(score);
    }

    /**
     * Query a URL for their source code.
     *
     * @param url The page's URL.
     * @return The source code.
     */
    private String getJSON(String url) {
        URL datamuse;
        URLConnection dc;
        StringBuilder s = null;
        try {
            datamuse = new URL(url);
            dc = datamuse.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(dc.getInputStream(), "UTF-8"));
            String inputLine;
            s = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                s.append(inputLine);
            in.close();
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return s != null ? s.toString() : null;
    }
}
