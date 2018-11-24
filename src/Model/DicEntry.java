package Model;

public class DicEntry {
    private  int id;
//    private int lastEntryId;
    private int numOfDocs;
    private int totalTermFrequency;

    DicEntry(int row){
        id = row;

    }

    public void incrementNumOfDocs(){
        numOfDocs++;
    }
    public void addTotalFrequency(int adition){
        totalTermFrequency+=adition;
    }
    public int getId(){
        return id;
    }

    @Override
    public String toString() {
        return "[id: "+id+", "+numOfDocs+", "+totalTermFrequency+"]";
    }
}
