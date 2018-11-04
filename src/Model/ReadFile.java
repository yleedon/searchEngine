package Model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ReadFile {
    String path;
    PrintWriter writer;

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

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

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
}
