package Merge;

import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MergeFile {
    private File merged, waitingList;
    private File[] files;


    /**
     * Constructor for MergeFile
     * @param input - the directory that contains only the files to merge
     * @param output - the path (name included) to the file that will be written
     * @throws Exception
     */
    public MergeFile(String input, String output) throws Exception{
        if (input == null || output == null)
            throw new Exception("error: path is null");
        waitingList = new File(input);//yaniv
        if (!waitingList.isDirectory())
            throw new Exception("error: parameter does not have a directory named waitingList");
        files = waitingList.listFiles();
        merged = new File(output);
    }

    /**
     * merging all the directory to 1 file
     */
    public void merge() {
        try {

            //<editor-fold desc="init readers">
            FileReader[] fileReaders = new FileReader[files.length];
            BufferedReader[] readers = new BufferedReader[fileReaders.length];
            for (int i = 0; i < fileReaders.length; i++) {
                fileReaders[i] = new FileReader(files[i]);
            }
            for (int i = 0; i < fileReaders.length; i++) {
                readers[i] = new BufferedReader(fileReaders[i]);
            }
            //</editor-fold>

            //<editor-fold desc="init writer">
            FileWriter fileWriter = new FileWriter(merged);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            //</editor-fold>

            //reading lines
            String[] lines = new String[readers.length];
            for (int i=0; i<lines.length; i++){
                lines[i] = readers[i].readLine();
            }

            //init q for the lines
            PriorityQueue<String> qLines = new PriorityQueue<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int t1 = Integer.parseInt(o1.split(":")[0]);
                    int t2 = Integer.parseInt(o2.split(":")[0]);
                    return t1-t2;
                }
            });

            //adding lines to the q
            for(String line: lines){
                if(line!=null)//yaniv
                    qLines.add(line);
            }

            //init q for the unition of the same term
            PriorityQueue<String> qTerm = new PriorityQueue<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int d1 = Integer.parseInt((o1.split(":")[1]).split(",")[0]);
                    int d2 = Integer.parseInt((o2.split(":")[1]).split(",")[0]);
                    return d1-d2;
                }
            });

            int idxOfLine;
            String currentLine;

            while (!qLines.isEmpty()){
                //get term to work on
                do {
                    currentLine = qLines.poll();
                    idxOfLine = idxOfLine(lines, currentLine);
                    lines[idxOfLine] = readers[idxOfLine].readLine();
                    try{
                        qLines.add(lines[idxOfLine]);
                    }
                    catch (NullPointerException e){
                    }
                    qTerm.add(currentLine);
                } while( !qLines.isEmpty() && qLines.peek().split(":")[0].equals(qTerm.peek().split(":")[0]));
                writer.write(lineUnition(qTerm));
                qTerm.clear();
            }

            //close readers
            for (int i=0; i<readers.length; i++){
                readers[i].close();
                fileReaders[i].close();
            }

            //close writer
            writer.close();
            fileWriter.close();

            //delete files
            if(!deleteDir(waitingList)){
                System.out.println("error: did not delete waitinList");
            }
        }
        catch (IOException e){
            System.out.println("error: MergeFile.merge()");
        }
    }

    /**
     * get the index of the line in the lines
     * @param lines - array of lines
     * @param line - the requested line
     * @return the index of the requested line, if doesn't exist return -1
     */
    private int idxOfLine(String[] lines, String line){
        int ans = 0;
        try {
            while (lines[ans]==null || !(lines[ans].equals(line))) {
                ans++;
            }
            return ans;
        }
        catch (Exception e) {
            return  -1;
        }
    }

    /**
     * unite the lines to one line
     * @param lines - the lines organized by the order of the docs
     * @return - the whole line after uniting
     */
    private String lineUnition(PriorityQueue<String> lines){
        StringBuilder ans = new StringBuilder();
        int currentIdx, lastIdx;
        String line = lines.poll();
        ans.append(line);
        String[] brokenLine;
        lastIdx = getIdxAfterLine(line.split(":")[1]);
        while (!lines.isEmpty()){
            line = lines.poll().split(":")[1];
            currentIdx = Integer.parseInt((line.split(",")[0]));
            brokenLine = line.split(",");
            brokenLine[0] = ""+(currentIdx-lastIdx);
            ans.append("~");
            for(int i=0; i<brokenLine.length-1; i++){
                ans.append(brokenLine[i]).append(",");
            }
            ans.append(brokenLine[brokenLine.length-1]);
            lastIdx = getIdxAfterLine(line);
        }
        return ans.append("\n").toString();
    }

    /**
     * given the current index, calculates the index of the doc after the given line
     * @param line - the current line
     * @return the index after this line
     */
    private int getIdxAfterLine(String line) {
        //get docs array
        String[] docs = line.split("~");
        //get docIdx array
        String[] docIdxs = new String[docs.length];
        for(int i=0; i<docIdxs.length; i++){
            docIdxs[i] = docs[i].split(",")[0];
        }

        //the answer index
        int ans = 0;

        //calculate current idx
        for (String idx: docIdxs){
            ans += Integer.parseInt(idx);
        }
        return ans;
    }

    /**
     * this function given a directory will delete it recursively
     * @return true if deleted
     */
    private boolean deleteDir(File dir) {
        if(dir.isDirectory()) {
            String[] children = dir.list();
            boolean success;
            for (int i = 0; i < children.length; i++) {
                success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }
        return dir.delete();
    }

}
