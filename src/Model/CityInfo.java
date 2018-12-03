package Model;

import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.List;

public class CityInfo {
    String city, country, population, currency;
    Country countryData;
    CountryService service;

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

    private void setCountryData() {
        List<Country> countries = service.getByCapital(city);
        if (countries.size() > 0)
            countryData = countries.get(0);
        else countryData = null;
    }

    public String getCountry() {
        if (country == null) {
            if (countryData == null) country = "";
            else country = countryData.getName();
        }
        return country;
    }

    public String getPopulation() {
        if (population == null) {
            if (countryData == null) population = "";
            else population = "" + countryData.getPopulation();
        }
        return population;
    }

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
