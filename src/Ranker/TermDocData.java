package Ranker;

import java.io.File;

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




    public int getDocId() {
        return docId;
    }

    public boolean isInTitle() {
        return isInTitle;
    }

    public int getRelativePlace() {
        return relativePlace;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getTerm() {
        return term;
    }



    @Override
    public int compareTo(Object o) {
        if(o==null || !(o instanceof TermDocData))
            return 1;
        TermDocData other = (TermDocData)o;
        return docId - other.getDocId();
    }
}
