package Model;

public class DicEntry {
    private  int id;
//    private int lastEntryId;
    public int numOfDocs;
    public int totalTermFrequency;
    int lastDocin;

    public DicEntry(int row){
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
        return id+","+numOfDocs+","+totalTermFrequency;
    }

    public int getLastDocin(){return lastDocin;}

    public void setLastDocin(int num){
        lastDocin=num;
    }
}
