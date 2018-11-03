package Model;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Parse {
    private String txt;
    private Map<String,String> monthMap;
    private char[] delimeters = {'.',',','?','!','"','\'',':',';','(',')','{','}','[',']','/','\\','<','>','\n'};
    private Map<String, Pair<Integer,String>> moneyMap;
    private Map<String,Pair<Integer,String>> numberMap;
    private String ans;
    private Set<String> stopWords;
    private String[] tokens;
    private Map<String,Integer> indexMap;
    int maxFreq;


    /**
     * Constructer - recieves a string to work on
     * @param text - the string to work on
     */
    public Parse(String text) {
        this.txt = text;
        maxFreq = 0;
        monthMap = new HashMap<>();
        moneyMap = new HashMap<>();
        indexMap = new HashMap<>();
        numberMap = new HashMap<>();
        stopWords = new HashSet<>();
        initializeMaps();
        ans ="";
    }

    /**
     * initializes the Hash Maps
     */
    private void initializeMaps() {
        monthMap.put("January","01");monthMap.put("February","02");monthMap.put("March","03");monthMap.put("April","04");monthMap.put("May","05");
        monthMap.put("June","06");monthMap.put("July","07");monthMap.put("August","08");monthMap.put("September","09");monthMap.put("October","10");
        monthMap.put("November","11");monthMap.put("December","12");monthMap.put("JANUARY","01");monthMap.put("FEBRUARY","02");monthMap.put("MARCH","03");
        monthMap.put("APRIL","04");monthMap.put("MAY","05");monthMap.put("JUNE","06");monthMap.put("JULY","07");monthMap.put("AUGUST","08");
        monthMap.put("SEPTEMBER","09");monthMap.put("OCTOBER","10");monthMap.put("NOVEMBER","11");monthMap.put("DECEMBER","12");
        monthMap.put("Jan","01");monthMap.put("Feb","02");monthMap.put("Mar","03");monthMap.put("Apr","04");monthMap.put("May","05");
        monthMap.put("Jun","06");monthMap.put("Jul","07");monthMap.put("Aug","08");monthMap.put("Sep","09");monthMap.put("Oct","10");
        monthMap.put("Nov","11");monthMap.put("Dec","12");

        moneyMap.put("million",new Pair<>(1,"M"));
        moneyMap.put("billion",new Pair<>(1000,"M"));
        moneyMap.put("trillion",new Pair<>(1000000,"M"));

        numberMap.put("Trillion", new Pair(1000,"B"));
        numberMap.put("Million", new Pair(1,"M"));
        numberMap.put("Billion",new Pair(1,"B"));
        numberMap.put("Thousand",new Pair(1,"K"));
        initializeStopWords();
    }

    /**
     * initializes stop words from a file
     */
    private void initializeStopWords() {

        try{
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("StopWords").getFile());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null)
                stopWords.add(st);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("stop words were not used!!");
        }
    }

    /**
     * this will take the textt and change the word formats.
     * @throws Exception
     */
    public void parse() throws Exception{
        ans = "";
        if(txt==null)
            throw new Exception("error: text was empty");
        tokens = txt.split(" ");
        if (tokens==null || tokens.length==0)
            throw new Exception("error: split didnt work");

        String word = "";
        for (int tNum = 0;tNum < tokens.length;tNum++ ) {
            if(tokens[tNum]==null)
                continue;
            try {
                word = tokenToTerm(tokens[tNum], tNum);
            }
            catch (Exception e){
                System.out.println("something went terribly wrong - this should never happen ):");
                System.out.println(e.getMessage());
            }

            addTerm(word);
        }

    }

    /**
     * return the string of the parser (for tests)
     * @return the parsed text.
     */
    public String toString(){
        return ans;
    }

    //<editor-fold desc="Private Functions">

    //<editor-fold desc="Classifying Types Related">
    /**
     * checks if the string contains a number
     * @param word - string
     * @return - true if a number is in the word
     */
    private boolean containsNumber(String word) {
        char c = '0';
        for(int i = 0; i< word.length();i++) {
            c = '0';
            for (int j = 0; j < 10; j++) {
                if (word.charAt(i) == c){
                    return true;
                }
                c++;
            }
        }
        return false;
    }

    /**
     * this deletes the delimiters
     * @param word - the word to free from delimiters
     * @return  a word without delimeters (can return "")
     */
    private String deleteDelimeter(String word) {
        char t;
        for(int i = 0; i < delimeters.length;i++) {
            t=delimeters[i];
            while (word.length()> 0 && word.charAt(word.length() - 1) == delimeters[i]) {
                word = word.substring(0, word.length() - 1);
                i=0;
            }
            while (word.length()> 0 &&word.charAt(0) == delimeters[i]) {
                word = word.substring(1);
                i=0;
            }
        }
        return word;
    }
    //</editor-fold>

    //<editor-fold desc="NUMBER Parsing Related">
    /**
     * this function evaluates the type of number and fixes to a tamplet form.
     * (numbers and dates)
     * @param word - a word that contains a number
     * @param tNum - the index of the word
     * @return - the term
     */
    private String numberEvaluation(String word, int tNum) {
        String originalWord = word;
        word = word.replaceAll(",","");
        boolean startsWithDoller = false;

        if(word.length() > 0 && word.charAt(0) == '$') {
            startsWithDoller = true;
            word = word.substring(1);
        }
        try {
            double numValue = Double.parseDouble(word);

            if (startsWithDoller)
                return DealWithDollerSign(tNum,numValue,originalWord);
        }
        catch(Exception e){

            if(tNum+1 < tokens.length && tokens[tNum+1]!=null && tokens[tNum+1].equals("Dollars"))  // takes care of 123bn and 123m
                return checknumberAndSize(word,tNum,originalWord);

            return originalWord ; // the number contains a char that is not a number - rules do not apply - f
        }
        ////////////////////////it is a number!!//////////////////////////
        word = checkAfterNumber(word,tNum);
//        if((tNum+1 < tokens.length && tokens[tNum+1]!=null) || tNum+1 >=tokens.length) {// has not been changed
        return dealWithSimpleNumber(word,tNum);
//        }
//        return word;
    }

    /**
     * this function check if the number format is "123m Dollars" or "123bn Dollars" and changes the format
     * @param word - the number
     * @param tNum - the token index
     * @param originalWord - the word before delimiters were deleted
     * @return the final term
     */
    private String checknumberAndSize(String word, int tNum, String originalWord) {
        String type="";
        int multyplyer = 0;
        if (word.charAt(word.length() - 1) == 'm') {
            multyplyer = 1;
            word = word.substring(0, word.length() - 1);
        }
        if (word.length() - 2 > 0 && word.charAt(word.length() - 1) == 'n' && word.charAt(word.length() - 2) == 'b') {
            multyplyer = 1000;
            word = word.substring(0, word.length() - 2);
        }
        try {
            double numValue = Double.parseDouble(word);
            tokens[tNum + 1] = null;
            if (word.contains("."))
                return numValue * multyplyer + " M Dollars";
            else return (int) numValue * multyplyer + " M Dollars";
        } catch (Exception e2) {
            return originalWord;
        }
    }

    /**
     * recievs a token that begins with '$' and is a number
     * @param tNum - token index
     * @param numValue - the number
     * @param originalWord - the token befor deleted delimiters.
     * @return - a doller term
     */
    private String DealWithDollerSign(int tNum, double numValue, String originalWord) {
        String secondWord;
        if (tNum+1 < tokens.length && tokens[tNum+1]!=null) {
            secondWord = deleteDelimeter(tokens[tNum + 1]);
            if (moneyMap.containsKey(secondWord)) {
                tokens[tNum+1] = null;
                int number = (int)numValue*moneyMap.get(secondWord).getKey();
                return number + " M Dollars";
            }
        }
        if (numValue < 1000000)
            return originalWord.substring(1) + " Dollars";

        else {
            int num = (int) numValue / 1000000;
            return Integer.toString(num) + " M" + " Dollars";
        }
    }

    /**
     * deals with a simple number and formats acording to its size
     * @param word - the number
     * @return - the formated number
     */
    private String dealWithSimpleNumber(String word,int tNum) {
        try {
            double number = Double.valueOf(word);

            if(number >= 1000 && number < 1000000){
//                number = (int)number;
                number=number/1000;
                if(number % 1 == 0)
                    return (int)number + "K";
//
                return number + "K";
            }

            if (number>=1000000 && number < 1000000000){

                double fraction;
                if(number%1 != 0){
                    fraction = number % 1000000;
                }

                number=number/1000000;
                if(number % 1 == 0)
                    return (int)number + "M";
                return number + "M";
            }
            if (number >= 1000000000){
                if ((number/1000000000) % 1 == 0)
                    return (int)(number/1000000000) + "B";
                return number/1000000000 + "B";
            }
        }
        catch(Exception e){
            return word; // not a number
        }
        return word;
    }

    /**
     * checks if a double is a Natural Number
     * @param v - the double
     * @return true if the number i a Natural Number
     */
    private boolean isInteger(double v) {

        if(v/(int)v==1)
            return true;
        return false;
    }

    /**
     * num
     * @param num
     * @param tNum
     * @return
     */
    private String checkAfterNumber(String num, int tNum) {
        if (tNum+1 >= tokens.length || tokens[tNum+1] == null)
            return num;
        String secondWord = deleteDelimeter(tokens[tNum+1]);

        //checks for Dates
        if(monthMap.containsKey(secondWord) ) // second word is month
            return createDateTerm(num,tNum,secondWord);

        //checks for percent/percentae
        if(secondWord.equals("percent") || secondWord.equals("percentage")){
            tokens[tNum+1] = null;
            return num + "%";
        }

        //checks for numberSizeword
        if (numberMap.containsKey(secondWord)){
            double number = Double.valueOf(num);
            tokens[tNum+1] = null;
            if(!isInteger(number))
                return number*numberMap.get(secondWord).getKey() + "" +numberMap.get(secondWord).getValue();
            else return (int)number*numberMap.get(secondWord).getKey() + "" +numberMap.get(secondWord).getValue();
        }

        //checks for "Dollars"
        if(secondWord.equals("Dollars")){
            tokens[tNum+1] = null;
            try{
                if(Integer.valueOf(num)>=1000000)
                    return Integer.valueOf(num)/1000000 + " M Dollars";
                return num +" Dollars";
            }
            catch (Exception e){
                if(Double.valueOf(num)>=1000000)
                    return Double.valueOf(num)/1000000 + " M Dollars";
                return num +" Dollars";
            }
        }

        // checks for a numberSize word
        if(moneyMap.containsKey(secondWord)){
            return dealWithSizeAfterNumber(num,tNum);
        }

        if(secondWord.contains("/") && containsNumber(secondWord))
            return dealWithFractionAfterNumber(num,tNum,secondWord);

        return num;
    }

    /**
     * checks if the second token is a fraction
     * @param num - the first number
     * @param tNum - the second tokens index
     * @param secondToken - the suspected fraction
     * @return the term
     */
    private String dealWithFractionAfterNumber(String num, int tNum, String secondToken) {
        String[] fraction = secondToken.split("/");
        if(fraction.length != 2)
            return num;
        // sheck if integers
        try{
            Integer.valueOf(fraction[0]);
            Integer.valueOf(fraction[1]);
        }
        catch (Exception e){
            /// not a leagal fraction
            return num;
        }
        if (tNum + 2 < tokens.length && tokens[tNum+2].equals("Dollars") ){
            num = num+ " " + secondToken + " Dollars";
            tokens[tNum +1] = null; tokens[tNum + 2] = null;
            return num;
        }
        tokens[tNum+1] = null;
        return num+" "+secondToken;
    }

    /**
     * transforms "123 million U.S. dollars" to "123 M Dollars"
     * @param num
     * @param tNum
     * @return
     */
    private String dealWithSizeAfterNumber(String num, int tNum) {
        String secondWord = deleteDelimeter(tokens[tNum+1]);
        if(tNum+2 < tokens.length && tokens[tNum+2].equals("U.S."))
            if(tNum+3 < tokens.length && deleteDelimeter(tokens[tNum+3]).equals("dollars")){
                tokens[tNum + 1] = null; tokens[tNum+2] = null; tokens[tNum + 3] = null;
                return Integer.valueOf(num)*moneyMap.get(secondWord).getKey() + " M Dollars";
            }
        return num;
    }

    /**
     * turns two tokens: "04" and "May" int0 "05-04
     * @param day - the day
     * @param tNum - the index od the day
     * @param secondWord - the month
     * @return - a Date term or the number if not a date term
     */
    private String createDateTerm(String day, int tNum, String secondWord) {
        try {
            if (Integer.valueOf(day) <= 31 && Integer.valueOf(day) >= 1 && day.length()<3) {
                if (day.length()==1)
                    day = "0"+day;
                tokens[tNum+1]=null;
                return monthMap.get(secondWord) + "-" + day;
            }
        }
        catch (Exception e){
            System.out.println("number date term exception ");
            System.out.println(e.getMessage());
            return day;
        }
        return day;
    }
    //</editor-fold>

    //<editor-fold desc="Parser Actions">
    /**
     * adds the term to the table
     * @param word
     */
    private void addTerm(String word) {
        if(word!=null && !word.equals("")) {
            word = tradeUppercase(word);

            if(indexMap.containsKey(word)) {
                indexMap.replace(word, indexMap.get(word) + 1);
                if (maxFreq<indexMap.get(word)+1)
                    maxFreq = indexMap.get(word);
            }
            else {
                indexMap.put(word, 1);
                if (maxFreq<1)
                    maxFreq = 1;
            }
            ans = ans + "{" + word + "} ";
        }
    }

    /**
     * if a upperCase term arrives - checks if a lower exists.. if so changes it to lower case
     * if a lowerCase term arrives, checks if an Upper case exists, if so deletes the upper case
     * from the index map and ads its counter to the lowerCase index.
     * @param word a term
     * @return - the term
     */
    private String tradeUppercase(String word) {
        if(word.charAt(0) >= 'A' && word.charAt(0) <= 'Z'){
            if(indexMap.containsKey(word.toLowerCase())){
                return word.toLowerCase();
            }
        }

        if(word.charAt(0) >= 'a' && word.charAt(0) <= 'z'){
            if(indexMap.containsKey(word.toUpperCase())){
                String upperCase = word.toUpperCase();
                int bigLetterCount = indexMap.get(upperCase);
                indexMap.remove(upperCase);
                if(indexMap.containsKey(word)){
                    indexMap.replace(word,indexMap.get(word)+bigLetterCount);
                }
                else indexMap.put(word,bigLetterCount);////////////////////////////////////////problem
            }
        }
        return word;
    }

    /**
     * this takes a word and does the first word processing:
     * deletes delimiters
     * numbers
     * Capitalizes
     * stop words
     * stemming
     * @param word -the word to process
     * @param tNum - this index of the current word
     * @return - the processed word
     */
    private String tokenToTerm(String word, int tNum) {
        String originalWord = word;
        word = deleteDelimeter(word); // deletes delimiters



        if(containsNumber(word)) { // deals with numbers in String
            word = numberEvaluation(word, tNum);
            if(!word.equals(originalWord))
                return word;
        }

        if(monthMap.containsKey(word)) { //checks for dates
            String date = checkWordForDate(word, tNum);
            if (!date.equals(word))
                return date;
        }

        if(checkmultiTerm(word,tNum))
            return "";

        if (stopWords.contains(word.toLowerCase())) {
            if(!word.toUpperCase().equals(word))
                return "";
        }



        word = Capitelize(word); // capitalizes all letters if start with upperCase

        //term-term-term
        // remove stop word
        /// stemming(boolian)?
        return word;
    }

    /**
     * checks if is term has AA-BB-CC.. or "Between Number and Number" and changes the format
     * @param word - the token
     * @param tNum - token index
     * @return - true if "Between Number and Number"
     */
    private boolean checkmultiTerm(String word, int tNum) {
        boolean negetiveFirst = false;
        boolean negetiveSecond = false;
        if(word.charAt(0) == '-'){
            word=word.substring(1);
            negetiveFirst = true;
        }
        if(word.contains("-")) {

            if(word.indexOf('-') + 1 < word.length())
                if (word.charAt(word.indexOf('-') + 1) == '-') {
                    negetiveSecond = true;
                    char test = word.charAt(word.indexOf("-"));
                    word = word.substring(0,word.indexOf('-') + 1) +word.substring(word.indexOf('-') + 2);
                }

            String[] tempTokens = word.split("-");
            if(tempTokens.length >= 2) {
                if (negetiveFirst) {
                    tempTokens[0] = "-"+ tempTokens[0];
                }
                if (negetiveSecond)
                    tempTokens[1] = "-" + tempTokens[1];
            }
            tokens[tNum]=null;
            for(int i = 0; i < tempTokens.length;i++){
                if(tempTokens[i] != null && !tempTokens[i].equals(""))
                    addTerm(tokenToTerm(tempTokens[i],tNum-1)); // (-1 so that it thinks the next token is num
            }
            return false;
        }

        try {
            if (word.equals("Between") && tokens[tNum+2].equals("and")) {
                String num1 = tokens[tNum+1];
                String num2 = tokens[tNum+3];

                Double.valueOf(deleteDelimeter(tokens[tNum+1]).replaceAll(",",""));
                Double.valueOf(deleteDelimeter(tokens[tNum+3]).replaceAll(",","")); //are numbers
                tokens[tNum] = null;  tokens[tNum+1] = null;  tokens[tNum+2] = null;  tokens[tNum+3] = null;
                num1 = tokenToTerm(num1,tNum-1);
                num2 = tokenToTerm(num2,tNum-1);
                addTerm(num1);
                addTerm(num2);
                addTerm(num1+"-"+num2);
                return true;
            }
        }
        catch (Exception e){
            return false;
        }
        return false;

    }


    /**
     * gets a month wotd and check if a day or year is the next token
     * and creates a date term.
     * @param word - the month
     * @param tNum - the token index of the motnth
     * @return Date term of the word if not a DateTerm
     */
    private String checkWordForDate(String word, int tNum) {
        if(tNum + 1 < tokens.length && tokens[tNum + 1] != null){
            String sNum = deleteDelimeter(tokens[tNum+1]);
            try{
                int number = Integer.valueOf(sNum);
                if(number <= 31 && number >= 1 && sNum.length()<3 ){
                    if(sNum.length() == 1)
                        sNum = "0" + sNum;
                    tokens[tNum+1]=null;
                    return monthMap.get(word) + "-" + sNum;
                }
                if (number >= 0 && sNum.length()>2){
                    tokens[tNum+1]=null;
                    return number +"-"+monthMap.get(word);
                }
            }
            catch (Exception e){
                return word;
            }
        }
        return word;

    }
    //</editor-fold>



    /**
     * this checks if the word Starts with a capitol letter and if so makes the whole
     * word upper case
     * @param word - the word to capitalize
     * @return - the word
     */
    private String Capitelize(String word) {
        if(word==null || word.equals(""))
            return word;
        if(word.charAt(0) >=65 && word.charAt(0) <=90)
            return word.toUpperCase();
        return word;
    }
    //</editor-fold>

    //<editor-fold desc="test functions">
    /**
     * sets the text to work on
     * @param text - the String to parse
     */
    public void setTxt(String text){
        txt = text;
    }

    public void printIndex(){
        System.out.println("IndexTable:");
        for (String term:indexMap.keySet()) {
            System.out.println("{"+term + " , "+ indexMap.get(term)+"}");
        }
        System.out.println("Max frequency: " + maxFreq);
        System.out.println("end.\n\n\n");
    }
    //</editor-fold>
}
