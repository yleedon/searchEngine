package Indexer;

/**
 * this Class represents an Entry for the Dictionary which includes the termID, docIdf, and TF.
 */
public class DicEntry implements Comparable{
    private  int id;
    public int numOfDocs;
    public int totalTermFrequency;
    public int lastDocin;

    /**
     * Constructer:
     * @param row - the termId
     */
    public DicEntry(int row){
        id = row;
    }

    /**
     * adds One to the amount of docs that the term appears in.
     */
    public void incrementNumOfDocs(){
        numOfDocs++;
    }

    /**
     * adds the current document TF to the total term TF
     * @param adition - the Tf of the newly added doc.
     */
    public void addTotalFrequency(int adition){
        totalTermFrequency+=adition;
    }

    /**
     * return the termId
     * @return
     */
    public int getId(){
        return id;
    }

    @Override
    public String toString() {
        return id+","+numOfDocs+","+totalTermFrequency;
    }

    /**
     *
     * @return - the last docId that was added to the termsPosting list.
     */
    public int getLastDocin(){return lastDocin;}

    /**
     * sets the last docId that was added to the termsPosting list.
     * @param num - the last docId that was added to the termsPosting list.
     */
    public void setLastDocin(int num){
        lastDocin=num;
    }

    /**
     * compares DicEntries by the Tf
     * @param o - a DicEntry to compare with
     * @return - 1 if other is greater than this, -1 if other is smaller than this, 0 if they are equal
     */
    @Override
    public int compareTo(Object o) {
        if(!(o instanceof DicEntry))
            return 0;
        DicEntry other = (DicEntry)o;
        if (other.totalTermFrequency > this.totalTermFrequency)
            return 1;
        if (other.totalTermFrequency < this.totalTermFrequency)
            return -1;
        return 0;
    }
}
