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
            long start = System.nanoTime();
            rf.readDirectory();
            long end = System.nanoTime();
            double time = end-start;
            System.out.println(time/1000000000);
        }
        catch(Exception e){
            System.out.println("something went wrong!!!");
        }
    }
}
