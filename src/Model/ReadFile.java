package Model;

import javafx.util.Pair;

import java.io.*;
import java.util.Map;

public class ReadFile {
    //<editor-fold desc="Fields">

    int docNumber;
    String path;
    File docIdxFile; //the file ReadFile writes into. AKA doocumentIdx.txt
    PrintWriter writer; // the object that writes to the file
    Parse parser;
    Indexer indexer;
    int numOdfiles;

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     *  Constructor - get the path of the corpus and set the printWriter
     * @param corpusPath - path of the corpus
     * @param outputPath - path of the outPut
     * @param stemmer - us stemming or not
     */
    public ReadFile(String corpusPath,String outputPath, boolean stemmer) {

        this.path = corpusPath;
        docNumber=0;
        numOdfiles = 0;
        parser = new Parse(corpusPath,"", stemmer);

        setOutputDestination(outputPath,stemmer);


    }

    private void setOutputDestination(String outputPath, boolean stemmer) {
        String masterDir = "\\dataBase";
        createDirectory(outputPath+masterDir);

        String stemType = "\\stemmed";
        if(!stemmer)
            stemType = "\\not stemmed";

        createDirectory(outputPath+masterDir+stemType);
        docIdxFile = new File(outputPath + masterDir + stemType+"\\docIdx.txt");
        createDirectory(outputPath+masterDir+stemType+"\\waitingList");

        indexer = new Indexer(outputPath + masterDir + stemType,0.5);

    }

    private void createDirectory(String dir) {
        File output = new File(dir);
        if (!output.exists()) {
            System.out.println("creating directory: " + dir);
            boolean result = false;

            try{
                output.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
                System.out.println("DIR "+dir+" created");
            }
        }

    }
    //</editor-fold>

    //<editor-fold desc="Setters">

    /**
     * Setter to the path of the corpus
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }
    //</editor-fold>

    //<editor-fold desc="Getters">

    /**
     * Getter to the path of the corpus
     * @return - the path of the corpus
     */
    public String getPath() {
        return path;
    }

    /**
     * get a document from corpus by name(docID)
     * @param docName - the unique name(docID) of the requested document
     * @return - the requested document.
     */
    public MyDocument getDocument(String docName){
        try {
            FileReader fileReader = new FileReader(docIdxFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            while (line != null && !line.startsWith(docName+",")){
                line = reader.readLine();
            }
            if (line == null) {return null;}
            String[] info = line.split(",");
            reader.close();
            fileReader.close();
            fileReader = new FileReader(new File(path+"\\"+info[3]));
            reader = new BufferedReader(fileReader);
            String doc = "";
            for (int i = 1; i<Integer.valueOf(info[1]); i++){
                reader.readLine();
            }
            for(int i = Integer.valueOf(info[1]); i<= Integer.valueOf(info[2]); i++){
                doc += reader.readLine() + "\n";
            }
            reader.close();
            fileReader.close();
            return new MyDocument(doc);
        }
        catch (Exception e){
            System.out.println("error read file");
            System.out.println(e.getMessage());
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Read Files">

    /**
     * reads all the files in the corpus and writes to the documentIdx the relevant details
     */
    public Map<String,DicEntry>  readDirectory(){
        try {
            FileWriter fileWriter = new FileWriter(docIdxFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            writer = new PrintWriter(bufferedWriter);
        }
        catch (IOException e){
            System.out.println("error read file");
            System.out.println(e.getMessage());
            writer = null;
        }
        File mainDir = new File(path);
        File[] list = mainDir.listFiles();
        writer.flush();

        double  n = 1;
        double size = list.length;
        for(File directory: list){


            if(!directory.getPath().endsWith("StopWords"))
                readDirectory(directory);
            System.out.println((int)Math.floor((n/size)*100)+"%");
            n++;
        }

        indexer.writeLastWaitingList();
        indexer.saveDictinary();
//        indexer.printTermlist();
//        indexer.printWaitList();
//        indexer.printWaitListSize();
        System.out.println("\n***************************************************************************");
        System.out.println("total files processed: "+ numOdfiles);
        System.out.println("total documents parsed: "+ (docNumber));


        System.out.println("amount of numbers: "+ parser.getNumberSet().size());
//        System.out.println(parser.getNumberSet());
//        indexer.test();
        System.out.println("document indexing complete");
        writer.close();
        return indexer.getDictianary();
    }

    /**
     * read all the files in a specific directory
     * @param directory - the given directory to read files from
     */
    private void readDirectory(File directory){
        numOdfiles++;
        File[] list = directory.listFiles();
        if(list!=null)
            for (File file: list) {
                dismember2Docs(file);
//                System.out.println("finished working on file: "+ file.getName());
            }

    }

    /**
     * dismember a file to documents. for each document it writes the relevant details to the documentIdx file.
     * @param file
     */
    private void dismember2Docs(File file) {
        StringBuilder docBuilder = new StringBuilder();
        String line,entry = "";
        int startIdx=0, endIdx=0, currentLine = 0;
        try{
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            while(reader.ready()){
                line = reader.readLine();
                currentLine++;
                if (line.toLowerCase().contains("<doc>")){
                    docBuilder.append(line+"\n");
                    line = reader.readLine();
                    startIdx = currentLine++;
                    while(reader.ready() && !line.toLowerCase().contains("</doc>")){
                        if(line.toLowerCase().contains("<docno>")){
                            String tag = getTag(line);
                            entry = (line.split(tag)[1]).split(tag.replace("<", "</"))[0];
                            entry = cleanEdges(entry);
                        }
                        docBuilder.append(line+"\n");
                        line = reader.readLine();
                        currentLine++;
                    }
                    docBuilder.append(line+"\n");
                    endIdx = currentLine;
//                    System.out.println("working on doc: "+entry);
//                    System.out.println(new MyDocument(docBuilder.toString()).getTxt());
                    MyDocument document = new MyDocument(docBuilder.toString());
                    document.setDocId(++docNumber);

                    parser.setTxt(document.getTxt());
                    int maxFreq = -1;
                    try {
                        parser.parse();

                        maxFreq = parser.maxFreq;
                        document.setTerms(parser.getDocMap());
                        document.setTextTokenCount(parser.getTokenSize());

                        parser.setTxt(document.getTitle());
                        parser.parse();
                        document.setTitleSet(parser.getDocMap());

                        indexer.addDoc(document);

                    }
                    catch (Exception e){
                        System.out.println("error (readFile) didn't parse");
                        System.out.println(e.getMessage());

                    }
                    //entry: doxId,startLine,endLine,path,termsCount,MaxFrequency,City(if needed)\n
                    int termsCount = document.getTerms()!=null?document.getTerms().size():0;
                    entry = new StringBuilder().append(docNumber).append(",").append(startIdx).append(",").append(endIdx).append(",").append(file.getPath().replace(path+"\\","")).append(",").append(termsCount).append(",").append(maxFreq).append(document.getCity()).append("\n").toString();

                    docBuilder.delete(0,docBuilder.length());
                    writer.append(entry);
                    writer.flush();
                }
            }
//            reader.close();
//            fileReader.close();
//            reader.close();//yaniv
//            writer.close();//yaniv
        }
        catch (IOException e){
//            writer.close();//yaniv
            System.out.println("error read file");
            System.out.println(e.getMessage());
        }

    }

    /**
     * Cleans the spaces in the beginning and the end of the string
     * @param s - String to clean
     * @return - the same String s without spaces in the beginning or the end
     */
    private String cleanEdges(String s) {
        String ans = s;
        if (ans == null)
            return null;
        while(ans.startsWith(" ")){
            ans = ans.substring(1);
        }
        while (ans.endsWith(" ")){
            ans = ans.substring(0, ans.length()-1);
        }
        return ans;
    }

    /**
     * gets the inside of the tag if exists
     * @param line
     * @return the tag for example: <Text>
     */
    private String getTag(String line) {
        String tag = line.split("<")[1];
        tag = tag.split(">")[0];
        return "<" + tag + ">";
    }

    public void reset() {
        path = null; /////////////////////////////////////////**********//the path to the corpus should be in config!!!!!!
        docIdxFile = null;; //the file ReadFile writes into. AKA doocumentIdx.txt
        writer.close(); // the object that writes to the file
        parser = null;;
        indexer.reset();
        numOdfiles = 0;;
    }

    public int numOfDocsProcessed(){return  docNumber;}
    //</editor-fold>

}
