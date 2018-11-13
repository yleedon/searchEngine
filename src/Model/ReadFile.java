package Model;
//pleaseeeeee
import java.io.*;

public class ReadFile {
    //<editor-fold desc="Fields">
    String path; /////////////////////////////////////////**********//the path to the corpus should be in config!!!!!!
    File docIdxFile; //the file ReadFile writes into. AKA doocumentIdx.txt
    PrintWriter writer; // the object that writes to the file
    Parse parser;
    Indexer indexer;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructor - get the path of the corpus and set the printWriter
     * @param path - the path of the corpus
     */
    public ReadFile(String path) {

        this.path = path;
        parser = new Parse("", true);
        indexer = new Indexer();
        ClassLoader classLoader = getClass().getClassLoader();
        docIdxFile = new File(classLoader.getResource("documentIdx.txt").getFile());

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
            fileReader = new FileReader(new File(path+info[3]));
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
            System.out.println(e.getMessage());
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Read Files">

    /**
     * reads all the files in the corpus and writes to the documentIdx the relevant details
     */
    public void readDirectory(){
        try {
            FileWriter fileWriter = new FileWriter(docIdxFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            writer = new PrintWriter(bufferedWriter);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            writer = null;
        }
        File mainDir = new File(path);
        File[] list = mainDir.listFiles();
        writer.flush();
        for(File directory: list){
            if(directory.getPath().endsWith("StopWords")) {
//                System.out.println("stop");
                readDirectory(directory);
            }

            if(!directory.getPath().endsWith("StopWords"))
            readDirectory(directory);
        }

        indexer.printWaitList();
        indexer.printTermlist();
        System.out.println(parser.getNumberSet());
        System.out.println("document indexing complete");
        writer.close();
    }

    /**
     * read all the files in a specific directory
     * @param directory - the given directory to read files from
     */
    private void readDirectory(File directory){
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
                    System.out.println("********************************" +entry);
//                    System.out.println(new MyDocument(docBuilder.toString()).getTxt());
                    MyDocument document = new MyDocument(docBuilder.toString());

                    parser.setTxt(document.getTxt());
                    try {
                        parser.parse();
//                        System.out.println("DocName: "+ entry);
//                        parser.printIndex();
                        document.setMaxFrequency(parser.maxFreq);
                        document.setTerms(parser.getDocMap());
                        indexer.addDoc(document);

//                        indexer.addDoc(new Doc);

//                        System.out.println(parser.getNumberSet());
//                        System.out.println("amount: " + parser.getNumberSet().size());
                    }
                    catch (Exception e){
                        System.out.println("didn't parse");
                    }
                    entry = new StringBuilder().append(entry).append(",").append(startIdx).append(",").append(endIdx).append(",").append(file.getPath().replace(path.substring(1).replace("/","\\"),"")).append("\n").toString();

                    docBuilder.delete(0,docBuilder.length());
                    writer.append(entry);
                    writer.flush();
                }
            }
        }
        catch (IOException e){
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
    //</editor-fold>
}
