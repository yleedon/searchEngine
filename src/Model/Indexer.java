package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Timer;

public class Indexer {
    private Parse parser;
    private ReadFile rf;

    public Indexer(ReadFile readFile) {
        parser = new Parse("", true);
//        ClassLoader classLoader = getClass().getClassLoader();
        this.rf = readFile;

    }
    public void parse(){
        try {
//            ClassLoader classLoader = getClass().getClassLoader();
            FileReader fileReader = new FileReader(new File("C:\\Users\\Yaniv\\Desktop\\searchproject\\searchEngine\\out\\production\\SearchEngine\\documentIdx.txt"));
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();

            long startTime = System.nanoTime();

            int numberOfDocs = 0;
            while (reader.ready()){

                line = line.split(",")[0];

                parser.setTxt( rf.getDocument(line).getTxt());
                System.out.println("********************************doc name is: " + line+ "*************************************************");
                parser.parse();
                parser.printIndex();
                numberOfDocs++;
                line = reader.readLine();
            }
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);
            System.out.println("process finished successfully\ntotal time: "+ duration/1000000 + "ms\nnum of docs: "+ numberOfDocs);
        }
        catch(Exception e){
            System.out.println("something went wrong!!!");
            return;
        }
    }
}
