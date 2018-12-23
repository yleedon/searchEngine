package Parser;

public class UpperCaseEntity implements Comparable{

    private int frequency;
    private String term;

    /**
     * contruter
     * @param term - the term
     * @param frequency - amount of times the term is in the text
     */
    public UpperCaseEntity(String term, int frequency) {
        this.frequency = frequency;
        this.term = term;
    }

    /**
     * getter
     * @return returns the frequency
     */
    public int getFrequency() {
        return frequency;
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

        return other.getFrequency() - frequency;
    }

    @Override
    public String toString() {
        return "Entity: "+ term+", rank: "+ 0.9*frequency; //decide rank...
    }
}


