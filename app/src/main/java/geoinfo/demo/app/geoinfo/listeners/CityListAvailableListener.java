package geoinfo.demo.app.geoinfo.listeners;

import java.util.HashMap;
import java.util.List;

import geoinfo.demo.app.geoinfo.models.City;

/**
 * Created by david on 5.8.2015..
 */
public interface CityListAvailableListener {
    void onList(HashMap<String, City> list);
}
