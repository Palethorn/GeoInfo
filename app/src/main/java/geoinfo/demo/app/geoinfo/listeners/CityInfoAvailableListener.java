package geoinfo.demo.app.geoinfo.listeners;

import java.util.List;

import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.models.Weather;

/**
 * Created by david on 5.8.2015..
 */
public interface CityInfoAvailableListener {
    void onInfo(City c, List<Weather> forecast);
}
