package processing;

import Indexer.*;
import Parser.Parse;
import javafx.scene.control.Alert;

import java.io.*;
import java.util.*;

public class ReadFile {
    //<editor-fold desc="Fields">

    private Alert processingAlert;
    private int docNumber;
    private String path;
    private File docIdxFile; //the file ReadFile writes into. AKA doocumentIdx.txt
    private PrintWriter writer; // the object that writes to the file
    private Parse parser;
    private Indexer indexer;
    private int numOdfiles;
    private Map<String,CityEntry> cityDick;
    private String outPath;
    private boolean stem;
    private List<Thread> apiThreadList;
    private Parse cityParser;

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     *  Constructor - get the path of the corpus and set the printWriter
     * @param corpusPath - path of the corpus
     * @param outputPath - path of the outPut
     * @param stemmer - us stemming or not
     */
    public ReadFile(String corpusPath,String outputPath, boolean stemmer, Alert processingAlert) {

        this.processingAlert = processingAlert;
        cityParser = new Parse(corpusPath,"", stemmer);
//        mutex = new Mutex();
        apiThreadList = new ArrayList<>();
        stem = stemmer;
        outPath = outputPath;
        cityDick = new TreeMap<>();
        this.path = corpusPath;
        docNumber=0;
        numOdfiles = 0;
        parser = new Parse(corpusPath,"", stemmer);

        setOutputDestination(outputPath,stemmer);
    }

    /**
     * sets the output destination in accordance to "stemmed" or "not stemmed" and also initializes the indexer
     * @param outputPath - the dataBase destination
     * @param stemmer - true if stemmed is to be used
     */
    private void setOutputDestination(String outputPath, boolean stemmer) {
        String masterDir = "\\dataBase";
        createDirectory(outputPath+masterDir);

        String stemType = "\\stemmed";
        if(!stemmer)
            stemType = "\\not stemmed";

        createDirectory(outputPath+masterDir+stemType);
        docIdxFile = new File(outputPath + masterDir + stemType+"\\docIdx.txt");
        createDirectory(outputPath+masterDir+stemType+"\\waitingList");

        indexer = new Indexer(outputPath + masterDir + stemType,1);
    }

    /**
     * creates a directory
     * @param dir - the path of the new directory
     */
    private void createDirectory(String dir) {
        File output = new File(dir);
        if (!output.exists()) {
//            System.out.println("creating directory: " + dir);
            boolean result = false;

            try{
                output.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
//                System.out.println("DIR "+dir+" created");
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
     * reads all the files in the corpus calls the parser and indexer on every doc,  and writes to the documentIdx and cityIdx the relevant details
     */
    public Map<String, DicEntry>  readDirectory(){
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
        int progress = 0;
        double last = 0;
        double size = list.length+1;
        for(File directory: list){
            if(directory.isDirectory())
                readDirectory(directory);
            progress = (int)Math.floor((n/size)*100);
            if(progress!=last) {
                processingAlert.setTitle(progress + "%");
                last=progress;

            }
//            processingAlert.setContentText(progress+"%");
//            processingAlert.setTitle((int)(Math.floor(n/size)*100)+"%");
            n++;
//            System.out.println((int)Math.floor((n/size)*100)+"%");
//            n++;
        }

        processingAlert.setTitle("99%");

        Thread t1 = new Thread(()->indexer.saveDictinary());
        t1.start();
        Thread t2 = new Thread(()->saveCityIndex());
        t2.start();
        Thread t3 = new Thread(()-> indexer.writePresentedDictionary());
        t3.start();

        indexer.writeLastWaitingList();
        indexer.mergeLastMiniFolded();
        indexer.mergeFinalePostingList();
//        System.out.println("\n***************************************************************************");
//        System.out.println("total files processed: "+ numOdfiles);
//        System.out.println("total documents parsed: "+ (docNumber));
//        indexer.creatReportData();
//        System.out.println("amount of numbers: "+ parser.getNumberSet().size());
//        System.out.println("document indexing complete");

        try {
            for (Thread t:apiThreadList){
                t.join();
            }

            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("error mergeFinalePostingList thread exception");
        }

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

                    parser.setTxt(document.getTxt(),document.getCity());
                    int maxFreq = -1;
                    try {
                        parser.parse();

                        maxFreq = parser.maxFreq;
                        document.setCityData(parser.getCityPositions());
                        if(!document.getCity().equals("")){
                            addDocToCity(document);
                        }

                        document.setTerms(parser.getDocMap());
                        document.setTextTokenCount(parser.getTokenSize());

                        parser.setTxt(document.getTitle(),document.getCity());
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
                    String city = ","+document.getCity();
                    if(city.length()==1)
                        city = "";
                    entry = new StringBuilder().append(docNumber).append(",").append(startIdx).append(",").append(endIdx).append(",").append(file.getPath().replace(path+"\\","")).append(",").append(termsCount).append(",").append(maxFreq).append(city).append("\n").toString();

                    docBuilder.delete(0,docBuilder.length());
                    writer.append(entry);
                    writer.flush();
                }
            }
        }
        catch (IOException e){
            System.out.println("error read file");
            System.out.println(e.getMessage());
        }

    }

    private void addDocToCity(MyDocument document) {
        try {
            String city = document.getCity();
            CityEntry entry;
            if (!cityDick.containsKey(city)) {
//                mutex.lock();
                entry = new CityEntry(city);
                cityDick.put(city, entry);
                if (!city.equals("")) {
                    //api
                    CityEntry finalEntry = entry;
                    Thread t = new Thread(() -> getApi(finalEntry));
                    t.start();
                    apiThreadList.add(t);

                }}
            else
                entry = cityDick.get(city);

            int gap = document.getDocId() - entry.getLastDocIn();
            entry.addDoc(document.getCityData(gap));
            entry.setLastDocIn(document.getDocId());
        }
        catch (Exception e){
            System.out.println();
        }


    }

    /**
     * gets the data from the API DATABASE and sets the entry accordingly
     * @param entry
     */
    private void getApi(CityEntry entry) {
        String dan = entry.getCityName();
        CityInfo cityInfo = new CityInfo(entry.getCityName());
        entry.setState(cityInfo.getCountry());
        entry.setCoin(cityInfo.getCurrency());
        cityParser.setTxt(cityInfo.getPopulation(),"");
        try {
            cityParser.parse();
//            System.out.println(cityInfo.getPopulation() + " -->  "+ cityParser.toString() );
            entry.setPopulation(cityParser.toString());
        }
        catch (Exception e){
            System.out.println("error read file cityApi");;
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

    /**
     * returns the number of docs processed
     * @return
     */
    public int numOfDocsProcessed(){return  docNumber;}

    /**
     * saves the city index to the disk
     */
    private void saveCityIndex(){
        try {
            String isStemmed = "\\stemmed";
            if(!stem)
                isStemmed = "\\not stemmed";

            File cityIndex = new File(outPath +"\\dataBase"+isStemmed+ "\\cityIndex.txt");
            FileWriter fw = new FileWriter(cityIndex);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.flush();
            for (String city : cityDick.keySet()) {
                bw.write(cityDick.get(city)+"\n");
            }
            bw.close();
        }
        catch (Exception e){
            System.out.println("error readFile save cityIndex ");
        }
    }
    //</editor-fold>

}
