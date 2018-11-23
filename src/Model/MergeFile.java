package Model;

import java.io.*;

public class MergeFile {
    File f1, f2, merged;

    public MergeFile(File f1, File f2) {
        this.f1 = f1;
        this.f2 = f2;
        merged = (f1==null)?null: new File(getClass().getClassLoader().getResource(f1.getName()).getFile());
    }

    public String testMerge(){
        //init
        f1 = new File("C:/Users/Dan/Desktop/tstmrg/1.txt");
        f2 = new File("C:/Users/Dan/Desktop/tstmrg/2.txt");
        merged = new File("C:/Users/Dan/Desktop/tstmrg/3.txt");
        //

        //measuring merge's time
        long start = System.nanoTime();
        merge();
        long end = System.nanoTime();
        //

        String ans = "" + ((end-start)/1000000);
        try {
            FileReader r = new FileReader(merged);
            BufferedReader br = new BufferedReader(r);

            int i=0;
            String s;
            while (br.ready())
            {
                s = br.readLine();
                if(s.startsWith(""+i))
                    i++;
                else {
                    System.out.println("problem");
                    return "Not increasing";
                }
            }
            return ans + " & increasing";
        }
        catch (IOException e)
        {
            return "Shiiiiittttt";
        }
    }

    /**
     * merge 2 files into 1 file(keeps the 1st's name)
     */
    public void merge() {
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            //init reader and writer and StringBuilder
            FileReader fr1 = new FileReader(f1);
            FileReader fr2 = new FileReader(f2);
            fw = new FileWriter(merged, false);
            BufferedReader r1 = new BufferedReader(fr1);
            BufferedReader r2 = new BufferedReader(fr2);
            writer = new BufferedWriter(fw);
            StringBuilder builder = new StringBuilder();
            //
            String line1 = r1.readLine();
            String line2 = r2.readLine();
            while(r1.ready() && r2.ready()){
                if(Integer.parseInt(line1.split(":")[0]) < Integer.parseInt(line2.split(":")[0])) //if line1 < line2 put line1 in the final file
                {
                    builder.append(line1).append("\n");
                    writer.append(builder.toString());
                    line1 = r1.readLine();
                    builder.delete(0, builder.length());
                }
                else if(Integer.parseInt(line1.split(":")[0]) > Integer.parseInt(line2.split(":")[0])) //if line1 > line2 put line2 in the final file
                {
                    builder.append(line2).append("\n");
                    writer.append(builder.toString());
                    line2 = r2.readLine();
                    builder.delete(0, builder.length());
                }
                else //if line1 = line2 put line1&line2 without the beginning in the final file
                {
                    builder.append(line1).append(line2.substring(line2.indexOf(":"))).append("\n");
                    writer.append(builder.toString());
                    line1 = r1.readLine();
                    line2 = r2.readLine();
                    builder.delete(0, builder.length());
                }
            }
            //put the last lines by order
            boolean r1ready = r1.ready();
            builder.append(r1ready?line2:line1).append("\n");
            writer.append(builder.toString());
            builder.delete(0, builder.length());
            builder.append(r1ready?line1:line2).append("\n");
            writer.append(builder.toString());
            builder.delete(0, builder.length());
            //
            while (r1ready) //write the rest of f1
            {
                builder.append(r1.readLine()).append("\n");
                writer.append(builder.toString());
                builder.delete(0, builder.length());
            }
            r1.close();
            fr1.close();
            while (r2.ready()) //write the rest of f2
            {
                builder.append(r2.readLine()).append("\n");
                writer.append(builder.toString());
                builder.delete(0, builder.length());
            }
            r2.close();
            fr2.close();
        }
        catch (IOException e) {
            System.out.println("error: MergeFile.merge()");
        }
        finally {//close writer
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.out.println("error: MergeFile.merge() - writer.closer()");
                }
            }
            if(fw != null){
                try {
                    fw.close();
                }
                catch (IOException e){
                    System.out.println("error: MergeFile.merge() - fw.closer()");
                }
            }
        }
    }
}
