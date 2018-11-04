package Model;

import java.io.*;

public class ReadFile {
    String path;
    File docIdxFile;
    PrintWriter writer;
//    PrintWriter writer = new PrintWriter(new File((getClass().getClassLoader()).getResource("documentIdx").getFile()));


    //<editor-fold desc="Constructor">

    /**
     * Constructor - get the path of the corpus and set the printWriter
     * @param path - the path of the corpus
     */
    public ReadFile(String path) {

            this.path = path;
            docIdxFile = new File("C:\\Dan\\UNI\\Jarta.Projects\\searchEngine\\src\\Resources\\documentIdx.txt");
            ClassLoader classLoader = getClass().getClassLoader();
            try {
                FileWriter fileWriter = new FileWriter(docIdxFile, true);
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
    public void readDirectory(){
        File mainDir = new File(path);
        File[] list = mainDir.listFiles();
        writer.flush();
        for(File directory: list){
            readDirectory(directory);
        }
        writer.close();
    }

    private void readDirectory(File directory){
        File[] list = directory.listFiles();
        for (File file: list) {
            dismember2Docs(file);
        }
    }

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
