package Model;

public class CityEntry {
    private String cityName;
    private String state;
    private String coin;
    private String population;
    private String docAndPositions;
    private int lastDocIn;


    public CityEntry(String city){
        cityName = city;
        docAndPositions = "";
        lastDocIn = 0;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public void addDoc(String newDoc) {
        docAndPositions += newDoc;
    }



    public String toString(){
        return cityName+"="+state+","+coin+","+population+"@"+docAndPositions;
    }

    public int getLastDocIn() {
        return lastDocIn;
    }

    public void setLastDocIn(int lastDocIn) {
        this.lastDocIn = lastDocIn;
    }

    public  String getCityName(){
        return cityName;
    }
}

