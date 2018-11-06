package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Indexer {
    private Parse parser;
    private ReadFile rf;

    public Indexer(ReadFile readFile) {
        parser = new Parse("", true);
        ClassLoader classLoader = getClass().getClassLoader();
      this.rf = readFile;

    }
    public void parse(){
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            FileReader fileReader = new FileReader(new File("C:\\Users\\Yaniv\\Desktop\\searchproject\\searchEngine\\out\\production\\SearchEngine\\documentIdx.txt"));
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            while (reader.ready()){

                line = line.split(",")[0];
                rf.getDocument(line).getDoc();
                parser.setTxt( rf.getDocument(line).getDoc());
                parser.parse();
                parser.printIndex();
                line = reader.readLine();
            }
        }
        catch(Exception e){
            System.out.println("something went wrong!!!");
            return;
        }
    }
}
