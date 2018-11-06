package Model;
//pleaseeeeee
import java.io.*;

public class ReadFile {
    //<editor-fold desc="Fields">
    String path; //the path to the corpus
    File docIdxFile; //the file ReadFile writes into. AKA doocumentIdx.txt
    PrintWriter writer; // the object that writes to the file
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructor - get the path of the corpus and set the printWriter
     * @param path - the path of the corpus
     */
    public ReadFile(String path) {

        this.path = path;
        ClassLoader classLoader = getClass().getClassLoader();
        docIdxFile = new File(classLoader.getResource("documentIdx.txt").getFile());
        try {
            FileWriter fileWriter = new FileWriter(docIdxFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            writer = new PrintWriter(bufferedWriter);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            writer = null;
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
            fileReader = new FileReader(new File(info[3]));
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
        File mainDir = new File(path);
        File[] list = mainDir.listFiles();
        writer.flush();
        for(File directory: list){
            readDirectory(directory);
        }
        writer.close();
    }

    /**
     * read all the files in a specific directory
     * @param directory - the given directory to read files from
     */
    private void readDirectory(File directory){
        File[] list = directory.listFiles();
        for (File file: list) {
            dismember2Docs(file);
        }
    }

    /**
     * dismember a file to documents. for each document it writes the relevant details to the documentIdx file.
     * @param file
     */
    private void dismember2Docs(File file) {
        String line,entry = "";
        int startIdx=0, endIdx=0, currentLine = 0;
        try{
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
//            Scanner scanner = new Scanner(file);
            while(reader.ready()){
                line = reader.readLine();
                currentLine++;
                if (line.contains("<Doc>")){
                    line = reader.readLine();
                    startIdx = currentLine++;
//                    currentLine++;
                    while(!line.contains("</Doc>")){
                        if(line.contains("<DocNo>"))
                            entry = (line.split("<DocNo>")[1]).split("</DocNo>")[0];
                        line = reader.readLine();
                        currentLine++;
                    }
                    endIdx = currentLine;
                    entry = entry + "," + startIdx + "," + endIdx + "," +file.getPath() + "\n";
//                    writer.println(entry);
                    writer.append(entry);
                    writer.flush();
                }
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    //</editor-fold>
}
