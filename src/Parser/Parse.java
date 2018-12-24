package Parser;

import javafx.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Parse {

    int termPosition;
    private Set<String> numberSet;
    private String txt;
    private boolean useStemming;
    private Map<String,String> monthMap;
    private char[] delimeters = {'-','$','%','`','.',',','?','!','"','\'',':',';','(',')','{','}','[',']','/','\\','<','>','\n','#','&','|','*','�','+','=','^','@','_'};
    private Map<String, Pair<Integer,String>> moneyMap;
    private Map<String,Pair<Integer,String>> numberMap;
    private String ans;
    private Set<String> stopWords;
    private String[] tokens;
    private Map<String,Pair<Integer,Integer>> indexMap; //pair: frequency,first appearance
    public int maxFreq;
    private Stemmer stemmer;
    String cityName;
    String cityPositionList;


    /**
     * Constructer - recieves a string to work on
     * @param text - the string to work on
     */
    public Parse(String stopWordPath, String text,boolean stemmerStatus) {
        this.txt = text.replace("\n", " ");
        this.useStemming = stemmerStatus;
        initializeMaps(stopWordPath);
        numberSet = new HashSet<>();
        stemmer = new Stemmer();

    }

    /**
     * this will take the textt and change the word formats.
     * @throws Exception
     */
    public void parse() throws Exception{
        indexMap = new HashMap<>();
        ans ="";
        maxFreq=0;
        if(txt==null) {
            return;
        }
        tokens = txt.split(" ");
        if (tokens==null || tokens.length==0)
            return;
        String word = "";
        for (int tNum = 0;tNum < tokens.length;tNum++ ) {
            if(tokens[tNum]==null)
                continue;
            try {
                word = tokenToTerm(tokens[tNum], tNum);
            }
            catch (Exception e){
                System.out.println("error something went terribly wrong - this should never happen ) (PARSE: parse():");
                System.out.println(e.getMessage());
            }
            termPosition = tNum;
            addTerm(word);
        }
    }

    //<editor-fold desc="Private Functions">

    /**
     * initializes the Hash Maps
     */
    private void initializeMaps(String path) {
        monthMap = new HashMap<>();
        moneyMap = new HashMap<>();
        numberMap = new HashMap<>();
        stopWords = new HashSet<>();
        monthMap.put("January","01");monthMap.put("February","02");monthMap.put("March","03");monthMap.put("April","04");monthMap.put("May","05");
        monthMap.put("June","06");monthMap.put("July","07");monthMap.put("August","08");monthMap.put("September","09");monthMap.put("October","10");
        monthMap.put("November","11");monthMap.put("December","12");monthMap.put("JANUARY","01");monthMap.put("FEBRUARY","02");monthMap.put("MARCH","03");
        monthMap.put("APRIL","04");monthMap.put("MAY","05");monthMap.put("JUNE","06");monthMap.put("JULY","07");monthMap.put("AUGUST","08");
        monthMap.put("SEPTEMBER","09");monthMap.put("OCTOBER","10");monthMap.put("NOVEMBER","11");monthMap.put("DECEMBER","12");
        monthMap.put("Jan","01");monthMap.put("Feb","02");monthMap.put("Mar","03");monthMap.put("Apr","04");monthMap.put("May","05");
        monthMap.put("Jun","06");monthMap.put("Jul","07");monthMap.put("Aug","08");monthMap.put("Sep","09");monthMap.put("Oct","10");
        monthMap.put("Nov","11");monthMap.put("Dec","12");

        moneyMap.put("million",new Pair<>(1,"M"));
        moneyMap.put("Million",new Pair<>(1,"M"));
        moneyMap.put("MILLION",new Pair<>(1,"M"));

        moneyMap.put("billion",new Pair<>(1000,"M"));
        moneyMap.put("BILLION",new Pair<>(1000,"M"));
        moneyMap.put("Billion",new Pair<>(1000,"M"));

        moneyMap.put("trillion",new Pair<>(1000000,"M"));
        moneyMap.put("TRILLION",new Pair<>(1000000,"M"));
        moneyMap.put("Trillion",new Pair<>(1000000,"M"));

        numberMap.put("Trillion", new Pair(1000,"B"));
        numberMap.put("trillion", new Pair(1000,"B"));
        numberMap.put("TRILLION", new Pair(1000,"B"));

        numberMap.put("million", new Pair(1,"M"));
        numberMap.put("Million", new Pair(1,"M"));
        numberMap.put("MILLION", new Pair(1,"M"));

        numberMap.put("Billion",new Pair(1,"B"));
        numberMap.put("BILLION",new Pair(1,"B"));
        numberMap.put("billion",new Pair(1,"B"));

        numberMap.put("THOUSAND",new Pair(1,"K"));
        numberMap.put("thousand",new Pair(1,"K"));
        numberMap.put("Thousand",new Pair(1,"K"));

        initializeStopWords(path);
    }

    /**
     * initializes stop words from a file
     */
    private void initializeStopWords(String path) {

        try{

            File file = new File(path+"\\stop_words.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null)
                stopWords.add(st);

            br.close();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("stop words were not used!!");
        }
    }

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
        for(int i = 0; i < delimeters.length;i++) {
            while (word.length()> 0 && word.charAt(word.length() - 1) == delimeters[i]) {
                if(i==2){
                    if(word.length() > 1 && (word.charAt(word.length()-2) >= '0' && word.charAt(word.length()-2) <= '9'))
                        break;
                }
                word = word.substring(0, word.length() - 1);
                i=0;
            }
            while (word.length()> 0 &&word.charAt(0) == delimeters[i]) {

                if(i==0 || i==1){//'-']\
                    if(word.length()>1 && (word.charAt(1) <= '9' && word.charAt(1) >= '0'))// iis minus - need to keep
                        break;
                }
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
        boolean ispercent= word.endsWith("%");

        if(word.length() > 0 && word.charAt(0) == '$') {
            startsWithDoller = true;
            word = word.substring(1);
        }
        try {
            if(word.contains("/")){
                word=dealWithFraction(word);
                if (word.endsWith("%"))
                    return word;

            }
            boolean isNormalledNumber = false;
            String end ="";
            boolean dontTuch= false;
            if(tNum+1 < tokens.length && tokens[tNum+1]!=null && tokens[tNum+1].toLowerCase().equals("dollars"))
                dontTuch=true;
            if(!dontTuch)
                if(word.toLowerCase().endsWith("m") || word.toLowerCase().endsWith("k") || word.toLowerCase().endsWith("b")){
                    isNormalledNumber=true;
                    end = ""+word.charAt(word.length()-1);
                    word = word.substring(0,word.length()-1);
                }

            if(ispercent)
                word = (word.substring(0,word.length()-1));
            double numValue = Double.parseDouble(word);
            numValue = Math.floor(numValue * 100) / 100;
            if(ispercent){
                if(isInteger(numValue)) {
                    return (int)numValue+ "%";
                }
                else return numValue+"%";

            }

            if (isNormalledNumber){
                if (isInteger(numValue))
                    word=(int)numValue+end;
                else  word=numValue+end;
                numberSet.add(word.toUpperCase());
                return word.toUpperCase();
            }
            if (startsWithDoller)
                return DealWithDollerSign(tNum,numValue,originalWord);
        }
        catch(Exception e){

            if(tNum+1 < tokens.length && tokens[tNum+1]!=null && tokens[tNum+1].toLowerCase().equals("dollars"))  // takes care of 123bn and 123m
                return checknumberAndSize(word,tNum,originalWord);

            return originalWord ; // the number contains a char that is not a number - rules do not apply - f
        }
        ////////////////////////it is a number!!//////////////////////////
        word = checkAfterNumber(word,tNum);

        return dealWithSimpleNumber(word,tNum);

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
            numValue =  Math.floor(numValue * 100) / 100;
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
                numValue = numValue * moneyMap.get(secondWord).getKey();
                if(isInteger(numValue)) {
                    return (int) numValue + " M Dollars";
                }
                else return numValue + " M Dollars";
            }
        }
        if (Math.abs(numValue) < 1000000 )
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
            double number = Double.parseDouble(word);
            number = number*100;
            number = Math.floor(number);
            number = number/100;

            if((Math.abs(number) >= 1000 && Math.abs(number) < 1000000)){
//                number = (int)number;
                number=Math.floor((number/1000)*100)/100;
                if(number % 1 == 0) {
                    numberSet.add((int) number + "K");
                    return (int) number + "K";
                }

                numberSet.add(number + "K");
                return number + "K";
            }

            if ((Math.abs(number) >= 1000000 && Math.abs(number) < 1000000000) ){

                number=(int)number;
                number=number/1000000;
                number =  Math.floor(number * 100) / 100;
                if(number % 1 == 0) {
                    numberSet.add((int)number + "M");
                    return (int) number + "M";
                }
                numberSet.add(number + "M");
                return number + "M";
            }
            if (Math.abs(number) >= 1000000000){
                number = number/1000000000;
                number =  Math.floor(number * 100) / 100;

                if ((number) % 1 == 0) {
                    numberSet.add((int) (number) + "B");
                    return (int) (number) + "B";
                }
                numberSet.add(number + "B");
                return number + "B";
            }
            word = number+"";
            if(isInteger(number)){
                word = (int)number+"";
            }
            numberSet.add(word);
            return word;
        }
        catch(Exception e) {
            return word; // not a number
        }
    }

    /**
     * checks if a double is a Natural Number
     * @param v - the double
     * @return true if the number i a Natural Number
     */
    private boolean isInteger(double v) {

        try {
            if (v / (int) v == 1)
                return true;
            return false;
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * checks after a number the next tokens and deals with it
     * @param num - the token
     * @param tNum - the token number
     * @return - term
     */
    private String checkAfterNumber(String num, int tNum) {
        if (tNum+1 >= tokens.length || tokens[tNum+1] == null)
            return num;
        String secondWord = deleteDelimeter(tokens[tNum+1]);

        //checks for Dates
        if(monthMap.containsKey(secondWord) ) // second word is month
            return createDateTerm(num,tNum,secondWord);

        //checks for percent/percentae
        if(secondWord.toLowerCase().equals("percent") || secondWord.toLowerCase().equals("percentage")){
            tokens[tNum+1] = null;
            return num + "%";
        }

        //checks for "Dollars"
        if(secondWord.toLowerCase().equals("dollars")){
            tokens[tNum+1] = null;

            try{
                double n =Double.valueOf(num);
                n = Math.floor(n * 100) / 100;
                if(Math.abs(n)>=1000000)
                    return Integer.valueOf(num)/1000000 + " M Dollars";
                if(isInteger(n))
                    return (int)n +" Dollars";
                else return n +" Dollars";
            }
            catch (Exception e){
                double n =Double.valueOf(num);
                n = Math.floor(n * 100) / 100;
                if(Math.abs(n) >=1000000)
                    return n/1000000 + " M Dollars";
                return num +" Dollars";
            }
        }

        // checks for a numberSize word
        if(numberMap.containsKey(secondWord)){
            return dealWithSizeAfterNumber(num,tNum);
        }

        if(secondWord.contains("/") && containsNumber(secondWord))
            return dealWithFractionAfterNumber(num,tNum,secondWord);


        //checks for bn/m Dollars
        try {
            if(tokens[tNum+2].toLowerCase().equals("dollars"))
                if(secondWord.equals("bn") ||  secondWord.equals("m")) {
                    tokens[tNum + 1] = null;
                    tokens[tNum + 2] = null;
                    if (secondWord.equals("m"))
                        return num + " M Dollars";
                    double d = Double.valueOf(num)*1000;

                    return (int)d + " M Dollars";
                }

        }
        catch (Exception e){
            return num;
        }

        return num;
    }

    /**
     * checks if is a number fraction
     * changes the fraction to decimal point with 2 numbers after the zero
     * @param word - the token
     * @return - the fraction or token
     */
    private String dealWithFraction(String word) {
        String[] fraction = word.split("/");
        if (fraction.length != 2)
            return word;

        // sheck if integers
        boolean endWithPercent = false;
        try {
            if (fraction[1].charAt(fraction[1].length() - 1) == '%') {
                endWithPercent = true;
                fraction[1] = fraction[1].substring(0, fraction[1].length() - 1);
            }

            Integer.valueOf(fraction[0]);
            Integer.valueOf(fraction[1]);

        } catch (Exception e) {
            return word; }

        double tempDouble = (Double.valueOf(fraction[0])/Double.valueOf(fraction[1]));
        tempDouble = Math.floor(tempDouble * 100) / 100;
        if(endWithPercent)
            return tempDouble+"%";
        else return ""+tempDouble;
    }

    /**
     * checks if the second token is a fraction
     * @param num - the first number
     * @param tNum - the second tokens index
     * @param secondToken - the suspected fraction
     * @return the term
     */
    private String dealWithFractionAfterNumber(String num, int tNum, String secondToken) {
        if(num.contains("."))
            return num;

        String[] fraction = secondToken.split("/");
        if (fraction.length != 2)
            return num;

        // sheck if integers
        boolean endWithPercent = false;
        try {
            if (fraction[1].charAt(fraction[1].length() - 1) == '%') {
                endWithPercent = true;
                fraction[1] = fraction[1].substring(0,fraction[1].length()-1);
            }
            Integer.valueOf(fraction[0]);
            Integer.valueOf(fraction[1]);

            double tempDouble;
            tokens[tNum + 1] = null;
            if(Integer.valueOf(num) < 0)
                tempDouble = Integer.valueOf(num)-(Double.valueOf(fraction[0])/Double.valueOf(fraction[1]));
            else  tempDouble = Integer.valueOf(num)+(Double.valueOf(fraction[0])/Double.valueOf(fraction[1]));

            tempDouble = Math.floor(tempDouble * 100) / 100;
            if (endWithPercent) {
                return tempDouble+"%";
            }
            return checkAfterNumber(""+tempDouble,tNum+1);

        } catch (Exception e) {
            /// not a leagal fraction
            return num;
        }
    }

    /**
     * transforms "123 million U.S. dollars" to "123 M Dollars"
     * @param num
     * @param tNum
     * @return
     */
    private String dealWithSizeAfterNumber(String num, int tNum) {
        String secondWord = deleteDelimeter(tokens[tNum+1]);
        if (moneyMap.containsKey(secondWord)) {
            if (tNum + 2 < tokens.length && tokens[tNum + 2].equals("U.S."))
                if (tNum + 3 < tokens.length && deleteDelimeter(tokens[tNum + 3]).toLowerCase().equals("dollars")) {
                    tokens[tNum + 1] = null;
                    tokens[tNum + 2] = null;
                    tokens[tNum + 3] = null;
                    if(isInteger(Double.valueOf(num)))
                        return Integer.valueOf(num) * moneyMap.get(secondWord).getKey() + " M Dollars";
                    else {
                        double n = Double.valueOf(num);
                        n = Math.floor(n * 100) / 100;
                        return n * moneyMap.get(secondWord).getKey() + " M Dollars";
                    }
                }
        }
        if (numberMap.containsKey(secondWord)){
            double number = Double.valueOf(num);
            number  = Math.floor(number * 100) / 100;
            tokens[tNum+1] = null;
            if(!isInteger(number)) {
                number = number * numberMap.get(secondWord).getKey();
                if(isInteger(number)){
                    String numberTerm =  (int)number + "" + numberMap.get(secondWord).getValue();
                    numberSet.add(numberTerm);
                    return (int)number + "" + numberMap.get(secondWord).getValue();
                }
                else {
                    String numberTerm = number + "" + numberMap.get(secondWord).getValue();
                    numberSet.add(numberTerm);
                    return number + "" + numberMap.get(secondWord).getValue();
                }
            }
            else {
                String numberTerm = (int) number * numberMap.get(secondWord).getKey() + "" + numberMap.get(secondWord).getValue();
                numberSet.add(numberTerm);
                return numberTerm;
            }
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
            return day;
        }
        return day;
    }
    //</editor-fold>

    //<editor-fold desc="Parser Actions">
    /**
     * adds the term to the table
     * @param word - the trm that is to be added
     */
    private void addTerm(String word) {
        if(word!=null && !word.equals("") && !word.equals("-") && !stopWords.contains(word.toLowerCase())) {
            word = word.replace(":","").replace(",","").replace("~",""); // makes sure no term contains : or ,
            word = deleteDelimeter(word);
            checkCityPosition(word);

            if(word == null || word.equals("") || stopWords.contains(word.toLowerCase()))
                return;
            if(!word.contains("-"))
                word = Capitalize(word);
            word = tradeUppercase(word);

            if(indexMap.containsKey(word)) {
                indexMap.replace(word, new Pair<>(indexMap.get(word).getKey() + 1,indexMap.get(word).getValue()));
                if (maxFreq<indexMap.get(word).getKey())
                    maxFreq = indexMap.get(word).getKey();
            }
            else {
                indexMap.put(word, new Pair<>(1,termPosition));
                if (maxFreq<1)
                    maxFreq = 1;
            }
            ans = ans  + word ; // yaniv

        }
    }

    /**
     * cecks if the term is a the city term and ads its position to th data
     * @param term - the term that may be a city
     */
    private void checkCityPosition(String term){
        if(!term.equals("") && term.toUpperCase().equals(cityName)){
            if(cityPositionList.equals(""))
                cityPositionList = termPosition+"";
            else cityPositionList += ","+termPosition;
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
        if(word.toUpperCase().equals(word)) {
            if(indexMap.containsKey(word.toLowerCase())){
                return word.toLowerCase();
            }
        }

        if(word.toLowerCase().equals(word)){
            if(indexMap.containsKey(word.toUpperCase())){
                String upperCase = word.toUpperCase();
                Pair<Integer,Integer> bigLetterCount = indexMap.get(upperCase);
                indexMap.remove(upperCase);
                if(indexMap.containsKey(word)){
                    indexMap.replace(word,new Pair<>(indexMap.get(word).getKey()+ (Integer)bigLetterCount.getKey(),(Integer)bigLetterCount.getValue()));//indexMap.get(word)+bigLetterCount);
                }

                else {
                    indexMap.put(word, bigLetterCount);////////////////////////////////////////problem
                }
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
        String originalWord =deleteDelimeter(word);

        word = deleteDelimeter(word); // deletes delimiters
        if(word.equals(""))
            return"";

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

        if(checkmultiTerm(word,tNum)) // check multi term
            return "";

        if (stopWords.contains(word.toLowerCase())) { //remove wanted stop word
            return "";
        }
        checkIfWeb(word); // check if website

        word = Capitalize(word); // capitalizes all letters if starts with upperCase

        if(useStemming) { // stemming
            if(word.toLowerCase().equals(word))
                word = stem(word);
            else word = stem(word.toLowerCase()).toUpperCase();
        }

        return word;
    }

    /**
     * this function recieves a token and stems it
     * @param word - the token
     * @return - the token after stemming
     */

    /**
     * stemmes a token
     * @param word - the token
     * @return the stemmed token
     */
    private String stem(String word) {
        if (!word.contains("-")) {
            stemmer.resetStemer();
            for (int i = 0; i < word.length(); i++)
                stemmer.add(word.charAt(i));
            stemmer.stem();
            return stemmer.toString();
        }
        return word;
    }

    /**
     * checks if the token is a webSite, if so creates two terms:
     * 1. the website
     * 2. the name of the webSite ([www.google.com],[google])
     * @param word - the token
     */
    private void checkIfWeb(String word) {
        try {
            if (word.startsWith("www.") || word.startsWith("WWW.") || word.startsWith("https://") || word.startsWith("http://")) {
                if (word.startsWith("https://"))
                    word = word.substring(8);
                else if(word.startsWith("http://"))
                    word = word.substring(7);
                else word = word.substring(4);
                if (word.contains("."))
                    word=word.substring(0,word.indexOf('.'));
                addTerm(word);
            }
        }catch (Exception e){
            return;
        }
    }

    /**
     * checks if is term has AA-BB-CC.. or "Between Number and Number" and changes the format
     * first it dicovers if the tokens are negetive numbers
     * @param word - the token
     * @param tNum - token index
     * @return - true if finished and original word needs to be deleted (not continye the parsing)
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
            String[] orginalTokens = word.split("-");
            if(tempTokens.length >= 2) {
                if (negetiveFirst) {
                    tempTokens[0] = "-"+ tempTokens[0];
                }
                if (negetiveSecond)
                    tempTokens[1] = "-" + tempTokens[1];
            }
            tokens[tNum]=null;
            String temp = "";
            String totalTerm = "";

            for(int i = 0; i < tempTokens.length;i++){

                if(tempTokens[i] != null && !tempTokens[i].equals("")) {
                    if(i ==tempTokens.length -1 )
                        temp = tokenToTerm(tempTokens[i],tNum);
                    else temp = tokenToTerm(tempTokens[i], tNum - 1);


                }
                if(temp.equals("")) // stemming (the-the => "" "" the-the
                    temp = orginalTokens[i];
                if(i!=0)
                    totalTerm = totalTerm + "-" + temp;
                else totalTerm = totalTerm + temp;

                if(i == tempTokens.length -1 )
                    addTerm(temp); // (-1 so that it thinks the next token is num
                else addTerm(tokenToTerm(tempTokens[i],tNum-1)); // (-1 so that it thinks the next token is num
            }

            addTerm(totalTerm.toLowerCase());
            return true;
        }

        try {
            if ((tokens[tNum+2].toLowerCase().equals("and") || tokens[tNum+3].toLowerCase().equals("and")) &&(word.toLowerCase().equals("between"))) {
                String num1 = tokens[tNum+1];
                String num2;
                if(tokens[tNum+2].toLowerCase().equals("and"))
                    num2 = tokens[tNum+3];
                else  num2 = tokens[tNum+4];

                num1 = numberEvaluation(num1,tNum+1);
                if(!containsNumber(num1))
                    return false;
                boolean isLong = false;
                if(tokens[tNum+3].toLowerCase().equals("and")) {
                    num2 = numberEvaluation(num2, tNum + 4);
                    isLong=true;
                }
                else num2 = numberEvaluation(num2,tNum+3);

                tokens[tNum] = null;  tokens[tNum+1] = null;  tokens[tNum+2] = null;  tokens[tNum+3] = null;
                if (isLong)
                    tokens[tNum+4] = null;
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
                if (number >= 32){
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
    private String Capitalize(String word) {
        if(word==null || word.equals(""))
            return word;
        if(word.charAt(0) >= 65 && word.charAt(0) <=90)
            return word.toUpperCase();
        return word;
    }
    //</editor-fold>

    //<editor-fold desc="test functions">
    /**
     * sets the text to work on
     * @param text - the String to parse
     */
    public void setTxt(String text, String city){
        txt  = text;
        ans="";
        if(text!=null) {
            txt = text.replace("\n"," ");
        }
        cityName = city;
        cityPositionList = "";
    }

    /**
     * avtivates and de activates stemming
     * @param use - true if stemming wnated, else false
     */
    public void setStemming(boolean use){
        useStemming = use;
    }

    //</editor-fold>

    /**
     * returns the indexMap (term --> (tr,token Number)
     * @return
     */
    public Map<String,Pair<Integer,Integer>> getDocMap(){
        return indexMap;
    }

    /**
     * @return - the token list length
     */
    public int getTokenSize(){
        return tokens.length;
    }

    /**
     * return the string of the parser (for tests)
     * @return the parsed text.
     */
    public String toString(){
        return ans;
    }

    /**
     *  getter,
     * @return - returns the city position list
     */
    public String getCityPositions() {
        return cityPositionList;
    }

    /**
     * finds the top most frequent upper case terms in the text
     * @return - the 5 most frequent upper case terms;
     */
    public  ArrayList<UpperCaseEntity> getFiveTopEnteties(){
        ArrayList<UpperCaseEntity> ans = new ArrayList<>();
        PriorityQueue<UpperCaseEntity> entetiesList = new PriorityQueue<>();
        for(String term:indexMap.keySet()){
            if(term.toUpperCase().equals(term) && !term.toLowerCase().equals(term)){
                entetiesList.add(new UpperCaseEntity(term,(double)indexMap.get(term).getKey()/maxFreq));
            }
        }
        for (int i = 0; i < 5 ; i ++){
            if (entetiesList.isEmpty())
                return ans;
            ans.add(entetiesList.poll());
        }
        return ans;
    }
}
