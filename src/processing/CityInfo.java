package processing;


import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;
import java.util.List;

public class CityInfo {
    private String city, country, population, currency;
    private Country countryData;
    private CountryService service;

    /**
     * constructor
     * @param city - the city name
     */
    public CityInfo(String city) {
        if (city.length() == 0){
            this.city = "";
            population = "";
            currency = "";
            country = "";
            return;
        }

        this.city = city;
        service = eu.fayder.restcountries.v1.rest.CountryService.getInstance();
        setCountryData();
        population = null;
        currency = null;
    }

    /**
     * sets the data of the country
     * if not found sets as null
     */
    private void setCountryData() {
        List<Country> countries = service.getByCapital(city);
        if (countries.size() > 0)
            countryData = countries.get(0);
        else countryData = null;
    }

    /**
     *
     * @return - the country
     * if not found return ""
     */
    public String getCountry() {
        if (country == null) {
            if (countryData == null) country = "";
            else country = countryData.getName();
        }
        return country;
    }

    /**
     *
     * @return the population
     * if not found returns ""
     */
    public String getPopulation() {
        if (population == null) {
            if (countryData == null) population = "";
            else population = "" + countryData.getPopulation();
        }
        return population;
    }

    /**
     *
     * @return the currency of the city
     *  if not found returns ""
     */
    public String getCurrency() {
        if (currency == null) {
            if (countryData == null) currency = "";
            else {
                List<String> currencies = countryData.getCurrencies();
                currency = currencies.get(0);
            }
        }
        return currency;
    }
}
