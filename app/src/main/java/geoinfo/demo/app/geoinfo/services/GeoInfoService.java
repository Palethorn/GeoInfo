package geoinfo.demo.app.geoinfo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import geoinfo.demo.app.geoinfo.listeners.CityInfoAvailableListener;
import geoinfo.demo.app.geoinfo.listeners.CityListAvailableListener;
import geoinfo.demo.app.geoinfo.listeners.ProgressUpdateListener;
import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.models.Weather;
import geoinfo.demo.app.geoinfo.utilities.CityListClient;
import geoinfo.demo.app.geoinfo.utilities.ForecastClient;
import geoinfo.demo.app.geoinfo.utilities.TimeZoneClient;
import geoinfo.demo.app.geoinfo.utilities.WeatherClient;

/**
 * Created by david on 5.8.2015..
 */
public class GeoInfoService extends Service implements LocationListener {

    private final IBinder binder = new GeoInfoServiceBinder();
    private static GeoInfoService instance;
    private City city;
    private CityListAvailableListener cityListAvailableListener;
    private CityInfoAvailableListener infoAvailableListener;
    private ProgressUpdateListener progressUpdateListener;
    List<Weather> forecast;
    private int no_of_jobs;
    private boolean retrievingList;

    private HashMap<String, City> cities;
    private LatLng current_location;

    @Override
    public void onCreate() {
        super.onCreate();
        no_of_jobs = 0;
        forecast = new ArrayList<>();
        cities = new HashMap<>();
        instance = this;

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location l = locationManager.getLastKnownLocation(provider);
        current_location = new LatLng(l.getLatitude(), l.getLongitude());
        locationManager.requestLocationUpdates(provider, 5000, 1, this);
        retrievingList = false;
    }

    public void retrieveCityList(CityListAvailableListener cityListAvailableListener, ProgressUpdateListener progressUpdateListener) {
        this.cityListAvailableListener = cityListAvailableListener;
        if(!cities.isEmpty() && !retrievingList) {
            if (cityListAvailableListener != null) {
                cityListAvailableListener.onList(this.cities);
            }
            return;
        }
        this.progressUpdateListener = progressUpdateListener;
        retrievingList = true;
        CityListClient clc = new CityListClient();
        clc.get(new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                ParseCitiesAsyncTask pcat = new ParseCitiesAsyncTask();
                pcat.execute(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
            }
        });
    }

    public static GeoInfoService getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public HashMap<String, City> getCities() {
        return cities;
    }

    @Override
    public void onLocationChanged(Location location) {
        current_location = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void updateCurrentLocation() {

        Geocoder gc = new Geocoder(this);
        City c = new City();
        try {
            List<Address> res = gc.getFromLocation(getCurrentLocation().latitude, getCurrentLocation().longitude, 1);
            c.setCountryName(res.get(0).getCountryName());
            c.setName(res.get(0).getLocality());
            c.setCountryCode(res.get(0).getCountryCode());
            c.setId(c.getName());
            c.setGeoPosition(getCurrentLocation());
            cities.put(c.getId(), c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LatLng getCurrentLocation() {
        return current_location;
    }

    public void setCurrentLocation(LatLng current_location) {
        this.current_location = current_location;
    }

    public class GeoInfoServiceBinder extends Binder {
        public GeoInfoService getService() {
            return GeoInfoService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void updateInfo(City city, CityInfoAvailableListener listener) {
        infoAvailableListener = listener;
        if (this.city != null && this.city.getId() == city.getId()) {
            if (infoAvailableListener != null) {
                infoAvailableListener.onInfo(this.city, this.forecast);
            }
            return;
        }
        this.city = city;
        no_of_jobs = 3;
        forecast.clear();
        ForecastClient fc = new ForecastClient();
        fc.get(city, 6, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    JSONObject json = new JSONObject(new String(responseBody));
                    JSONArray array = json.getJSONArray("list");
                    for (int i = 1; i < array.length(); i++) {
                        Weather w = new Weather();
                        w.importFromJsonObject(array.getJSONObject(i));
                        w.setTag(sdf.format(w.getDate().getTime()));
                        forecast.add(w);
                        checkJobs();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
        WeatherClient wc = new WeatherClient();
        wc.get(city, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    Weather current_weather = new Weather();
                    current_weather.importFromJsonObject(json);
                    current_weather.setTag("Today");
                    forecast.add(0, current_weather);
                    checkJobs();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        TimeZoneClient tzc = new TimeZoneClient();
        tzc.get(city.getGeoPosition(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(result);
                    int offset = object.getInt("rawOffset");
                    offset = offset / 60 / 60;
                    GeoInfoService.this.city.setTimezone("GMT " + (offset < 0 ? "-" : "+") + String.valueOf(offset));
                } catch (JSONException e) {
                    GeoInfoService.this.city.setTimezone("Could not retrieve");
                    e.printStackTrace();
                }
                checkJobs();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void checkJobs() {
        no_of_jobs--;
        if (no_of_jobs == 0) {
            infoAvailableListener.onInfo(this.city, this.forecast);
        }
    }

    class ParseCitiesAsyncTask extends AsyncTask<String, Double, Void> {
        public int total = 0;
        @Override
        protected Void doInBackground(String... params) {
            Geocoder gc = new Geocoder(GeoInfoService.this, Locale.getDefault());
            try {
                JSONObject json = new JSONObject(params[0]);
                JSONArray array = json.getJSONArray("features");
                total = array.length();
                for (int i = 0; i < 100 /*array.length()*/; i++) {
                    JSONObject object = array.getJSONObject(i);
                    City city = new City();
                    city.importFromJsonObject(object);
                    List<Address> addresses = gc.getFromLocation(city.getGeoPosition().latitude,
                            city.getGeoPosition().longitude, 1);
                    if (addresses.size() > 0) {
                        city.setCountryName(addresses.get(0).getCountryName());
                        city.setCountryCode(addresses.get(0).getCountryCode());
                    }
                    cities.put(city.getId(), city);
                    publishProgress((double)i, (double)total);
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            retrievingList = false;
            cityListAvailableListener.onList(cities);
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            if(progressUpdateListener != null) {
                progressUpdateListener.onProgress(values);
            }
        }
    }
}
