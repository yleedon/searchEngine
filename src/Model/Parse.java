package Model;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class Parse {
    private String txt;
    private Map<String,String> monthMap;
    private char[] delimeters = {'.',',','?','!','"','\'',':',';','(',')','{','}','[',']','/','\\','<','>','\n'};
    private String [] moneyWords = {"million","billian","trillan"};
    private Map<String, Pair<Integer,String>> moneyMap;
    private String ans;
    private String[] tokens;

    /**
     * Constructer - recieves a string to work on
     * @param text - the string to work on
     */
    public Parse(String text) {
        this.txt = text;
        monthMap = new HashMap<>();
        moneyMap = new HashMap<>();
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

        moneyMap.put("million",new Pair<>(1,"M"));
        moneyMap.put("billion",new Pair<>(1000,"M"));
        moneyMap.put("trillion",new Pair<>(1000000,"M"));


    }

    /**
     * this will take the textt and change the word formats.
     * @throws Exception
     */
    public void parse() throws Exception{
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

            if(word!=null && !word.equals(""))
                ans = ans+" {"+word+"}";
        }
    }


    public String toString(){
        return ans;
    }

    /**
     * this takes a word and does the first word processing:
     * deletes delimiters
     * numbers
     * Capitalizes
     * stop words
     * stemming
     * @param word -the word to process
     * @param worNum - this index of the current word
     * @return - the processed word
     */
    private String tokenToTerm(String word, int worNum) {
        word = deleteDelimeter(word);
        if(containsNumber(word))
            return numberEvaluation(word,worNum);
        word = Capitelize(word);
        // remove stop word
        /// stemming(boolian)?
        return word;
    }

    /**
     * this function evaluates the type of number and fixes to a tamplet form.
     * (numbers and dates)
     * @param word - a word that contains a number
     * @param tNum - the index of the word
     * @return - the term
     */
    private String numberEvaluation(String word, int tNum) {
        String originalWord = word;
        boolean hasDollar = false;
        word = word.replaceAll(",","");

        if(word.length() > 0 && word.charAt(0) == '$') {
            word = word.substring(1);
            hasDollar = true;
        }
        // checks if it a number also returns the original word
        try {

            double numValue = Double.parseDouble(word);
            ///////////////// passed!!!!///////////

            if (hasDollar) {
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
        }
        catch(Exception e){
            //git test
            return originalWord ; // the number contains a char that is not a number - rules do not apply - f
            /// to do? 100bn, 100m
        }

        ////////////////////////it is a number!!//////////////////////////

        word = checkAfterNumber(word,tNum);
        if((tNum+1 < tokens.length && tokens[tNum+1]!=null) || tNum+1 >=tokens.length)// has not been changed
            System.out.println(" not changed");
        return word;
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
     * @return
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
     * @param num - the day
     * @param tNum - the index od the day
     * @param secondWord - the month
     * @return - a Date term or the number if not a date term
     */
    private String createDateTerm(String num, int tNum, String secondWord) {
        try {
            if (Integer.valueOf(num) <= 31 && Integer.valueOf(num) >= 1 && num.length()<3) {
                if (num.length()==1)
                    num = "0"+num;
                tokens[tNum+1]=null;
                return monthMap.get(secondWord) + "-" + num;
            }
        }
        catch (Exception e){
            System.out.println("number date term exception ");
            System.out.println(e.getMessage());
        }
        finally {
            return num;
        }

    }

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

}
