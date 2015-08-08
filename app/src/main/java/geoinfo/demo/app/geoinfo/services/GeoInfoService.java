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

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.listeners.CityInfoAvailableListener;
import geoinfo.demo.app.geoinfo.listeners.CityListAvailableListener;
import geoinfo.demo.app.geoinfo.listeners.LocationChangedListener;
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
    private LocationChangedListener locationChangedListener;
    LocationManager locationManager;
    ParseCitiesAsyncTask parseCitiesAsyncTask;
    CityListClient cityListClient;

    private HashMap<String, City> cities;
    private LatLng current_location;

    @Override
    public void onCreate() {
        super.onCreate();
        no_of_jobs = 0;
        forecast = new ArrayList<>();
        cities = new HashMap<>();
        instance = this;

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location l = locationManager.getLastKnownLocation(provider);
        if (l != null) {
            setCurrentLocation(new LatLng(l.getLatitude(), l.getLongitude()));
        } else {
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        }
        retrievingList = false;
    }

    public void retrieveCityList(CityListAvailableListener cityListAvailableListener, ProgressUpdateListener progressUpdateListener) {
        this.cityListAvailableListener = cityListAvailableListener;
        if (!cities.isEmpty() && !retrievingList) {
            if (cityListAvailableListener != null) {
                cityListAvailableListener.onList(this.cities);
            }
            return;
        }
        cities.clear();
        this.progressUpdateListener = progressUpdateListener;
        retrievingList = true;
        cityListClient = new CityListClient();
        cityListClient.get(new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(!retrievingList) {
                    return;
                }
                String result = new String(responseBody);
                parseCitiesAsyncTask = new ParseCitiesAsyncTask();
                parseCitiesAsyncTask.execute(result);
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
        setCurrentLocation(new LatLng(location.getLatitude(), location.getLongitude()));
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

    public LatLng getCurrentLocation() {
        return current_location;
    }

    public void setLocationChangedListener(LocationChangedListener listener) {
        locationChangedListener = listener;
    }

    public void setCurrentLocation(LatLng current_location) {
        locationManager.removeUpdates(this);
        this.current_location = current_location;

        if (locationChangedListener != null) {
            locationChangedListener.locationChanged(current_location);
        }
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
        if (this.city != null && this.city.getId().equals(city.getId())) {
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
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                JSONArray array;
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    array = json.getJSONArray("list");
                } catch (JSONException e) {
                    return;
                }
                for (int i = 1; i < array.length(); i++) {
                    Weather w = new Weather();
                    try {
                        w.importFromJsonObject(array.getJSONObject(i));
                    } catch (JSONException e) {
                        continue;
                    }
                    w.setTag(sdf.format(w.getDate().getTime()));
                    forecast.add(w);
                }
                checkJobs();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                checkJobs();
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                checkJobs();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                checkJobs();
            }
        });
        TimeZoneClient.KEY = getResources().getString(R.string.google_maps_key);
        TimeZoneClient tzc = new TimeZoneClient();
        tzc.get(city.getGeoPosition(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(result);
                    int offset = object.getInt("rawOffset");
                    offset = offset / 60 / 60;
                    GeoInfoService.this.city.setTimezone("GMT " + (offset < 0 ? "" : "+") + String.valueOf(offset));
                } catch (JSONException e) {
                    GeoInfoService.this.city.setTimezone("Could not retrieve");
                    e.printStackTrace();
                }
                checkJobs();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                checkJobs();
            }
        });
    }

    private void checkJobs() {
        no_of_jobs--;
        if (no_of_jobs == 0) {
            infoAvailableListener.onInfo(this.city, this.forecast);
        }
    }

    public void cancelCityListParsing() {
        if(parseCitiesAsyncTask != null && !parseCitiesAsyncTask.isCancelled()) {
            parseCitiesAsyncTask.cancel(true);
        }
        retrievingList = false;
    }

    class ParseCitiesAsyncTask extends AsyncTask<String, Double, Void> {
        public int total = 0;

        @Override
        protected Void doInBackground(String... params) {
            Geocoder gc = new Geocoder(GeoInfoService.this, Locale.getDefault());
            JSONObject json;
            JSONArray array;
            try {
                json = new JSONObject(params[0]);
                array = json.getJSONArray("features");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            total = array.length();
            for (int i = 0; i < 100 /*array.length()*/; i++) {
                if(isCancelled()) {
                    return null;
                }
                JSONObject object;
                try {
                    object = array.getJSONObject(i);
                } catch (JSONException e) {
                    continue;
                }
                City city = new City();
                city.importFromJsonObject(object);
                List<Address> addresses = null;
                try {
                    addresses = gc.getFromLocation(city.getGeoPosition().latitude,
                            city.getGeoPosition().longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null && addresses.size() > 0) {
                    city.setCountryName(addresses.get(0).getCountryName());
                    city.setCountryCode(addresses.get(0).getCountryCode());
                }
                if(!isCancelled()) {
                    cities.put(city.getId(), city);
                }
                publishProgress((double)i, (double)total);
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
            if (progressUpdateListener != null) {
                progressUpdateListener.onProgress(values);
            }
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            cities.clear();
        }
    }
}
