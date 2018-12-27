package Ranker;


public class TermDocData implements Comparable {

    private int docId;
    private boolean isInTitle;
    private int relativePlace;
    private int frequency;
    private String term;
    private  int docTotalAmountofTerms;

    public TermDocData(int docId, boolean isInTitle, int relativePlace, int frequency, String term, int docTotalTermAmount) {
        this.docId = docId;
        this.isInTitle = isInTitle;
        this.relativePlace = relativePlace;
        this.frequency = frequency;
        this.term = term;
        this.docTotalAmountofTerms = docTotalTermAmount;

    }

    /**
     *
     * @return - doc Id
     */
    public int getDocId() {
        return docId;
    }

    /**
     *
     * @return - true if in title
     */
    public boolean isInTitle() {
        return isInTitle;
    }

    /**
     * return the relative place of the first apearance of the term (0-10] (10 the first wordof the document test)
     * @return
     */
    public int getRelativePlace() {
        return relativePlace;
    }

    /**
     * return the term frequency
     * @return
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * returns the term
     * @return
     */
    public String getTerm() {
        return term;
    }

    /**
     *
     * @return - the total amount of terms that the document contains
     */
    public int getDocTotalAmountofTerms(){return docTotalAmountofTerms;}

    @Override
    /**
     * campare by doc Id
     */
    public int compareTo(Object o) {
        if(o==null || !(o instanceof TermDocData))
            return 1;
        TermDocData other = (TermDocData)o;
        return docId - other.getDocId();
    }

    /**
     *
     * @return - return the class information
     */
    public String toString(){
        return "[Term:"+term+",Doc:"+docId+",RelativeLocation:"+relativePlace+",intTitle:"+isInTitle+",totalTermsAmount:"+docTotalAmountofTerms+"]";
    }
}
