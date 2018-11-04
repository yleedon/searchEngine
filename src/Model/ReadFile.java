package Model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ReadFile {
    String path;
    PrintWriter writer;


    //<editor-fold desc="Constructor">
    public ReadFile(String path) {
        this.path = path;
        ClassLoader classLoader = getClass().getClassLoader();
        File docIdxFile = new File(classLoader.getResource("documentIdx").getFile());
        try {
            PrintWriter writer = new PrintWriter(docIdxFile);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            writer = null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Setters">
    public void setPath(String path) {
        this.path = path;
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    public String getPath() {
        return path;
    }
    //</editor-fold>

    //<editor-fold desc="Read Files">
    public void readDirectory(){
        File mainDir = new File(path);
        for(File directory: mainDir.listFiles()){
            readDirectory(directory);
        }
    }

    private void readDirectory(File directory){
        for (File file: directory.listFiles()) {
            dismember2Docs(file);
        }
    }

    private void dismember2Docs(File file) {
        try{
            Scanner scanner = new Scanner(file);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void dismember2Docs(){
        File file = new File(path);
        Scanner scanner = null;

        String line;
        int idx = 1;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()){
                line = scanner.nextLine();
                if(line.contains("<Doc>")) {
//                    line = line.replace("<Doc>", "");
//                    if (line.length() >0) {writer.println(line);}
                    line = scanner.nextLine();
                    while (!line.contains("</Doc>")){
                        if (line.contains("<DocNo>")){
                            line = line.replace("<DocNo>", "");
                            line = line.replace("</DocNo>", "");
                        }
                        writer.println(line);
                        line = scanner.nextLine();
                    }
                    line = line.replace("</Doc>", "");
                    if (line.length() >0) {writer.println(line);}
                    writer.close();
                    idx++;
                }
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }

    }
    //</editor-fold>
}
