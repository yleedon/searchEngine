package Parser;

public class UpperCaseEntity implements Comparable{

    private int frequency;
    private String term;

    public UpperCaseEntity(String term, int frequency) {
        this.frequency = frequency;
        this.term = term;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public int compareTo(Object o) {
        if(o==null || !(o instanceof UpperCaseEntity))
            return -1; /// yaniv ??????
        UpperCaseEntity other = (UpperCaseEntity) o;

        return frequency - other.getFrequency();
    }
}


