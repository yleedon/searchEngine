package processing;

public class CityEntry {
    private String cityName;
    private String state;
    private String coin;
    private String population;
    private String docAndPositions;
    private int lastDocIn;


    /**
     * constructor
     * @param city - the name of the city
     */
    public CityEntry(String city){
        cityName = city;
        docAndPositions = "";
        lastDocIn = 0;
    }

    /**
     * sets the state
     * @param state - the state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * sets the coin
     * @param coin - the coin
     */
    public void setCoin(String coin) {
        this.coin = coin;
    }

    /**
     * sets the population
     * @param population - the population
     */
    public void setPopulation(String population) {
        this.population = population;
    }

    /**
     * appends the new doc positions to the "docAndPositions" String
     * @param newDoc - the positions of the city in the given document
     */
    public void addDoc(String newDoc) {
        docAndPositions += newDoc;
    }

    /**
     * to string entry format
     * @return - a Atring with all the entry data
     */
    public String toString(){
        return cityName+"="+state+","+coin+","+population+"@"+docAndPositions;
    }

    /**
     *  (for gaps)
     * @return last doc in
     */
    public int getLastDocIn() {
        return lastDocIn;
    }

    /**
     * (for gaps)
     * @param lastDocIn - doc id of last doc added
     */
    public void setLastDocIn(int lastDocIn) {
        this.lastDocIn = lastDocIn;
    }

    /**
     * return the citys name
     * @return
     */
    public  String getCityName(){
        return cityName;
    }
}

