package Parser;

public class UpperCaseEntity implements Comparable{

    private double rank;
    private String term;

    /**
     * contruter
     * @param term - the term
     * @param rank - amount of times the term is in the text divided by doc max frequency
     */
    public UpperCaseEntity(String term, double rank) {
        this.rank = rank;
        this.term = term;
    }

    /**
     * getter
     * @return returns the rank
     */
    public double getRank() {
        return rank;
    }

    /**
     * getter
     * @return -  returns the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * compares between the term frequencies
     * @param o - an instanceof UpperCaseEntity
     * @return - positive if other is bigger, negetive if this is bigger 0 if equal
     */
    @Override
    public int compareTo(Object o) {
        if(o==null || !(o instanceof UpperCaseEntity))
            return -1; /// yaniv ??????
        UpperCaseEntity other = (UpperCaseEntity) o;
        double compare = other.getRank() - rank;
        if (compare > 0)
            return 1;
        if(compare < 0)
            return -1;
        return 0;
    }

    @Override
    public String toString() {
        return "Entity: "+ term+",     Rank: "+ rank;
    }
}


