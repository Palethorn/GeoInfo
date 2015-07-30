package geoinfo.demo.app.geoinfo.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.utilities.CityListClient;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    public static final String TAG = "MainActivity";
    private GoogleMap map;
    private HashMap<String, City> cities;
    private Location current_location;
    private String provider;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        cities = new HashMap<>();
        initMap();
        initLocation();
    }

    private void initLocation() {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        current_location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 5000, 1, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMap();
    }

    private void initMap() {
        if (map == null) {
            ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    private void setUpMap() {
        pd.setTitle("Retrieving city list");
        pd.setMessage("Please wait...");
        pd.show();
        CityListClient clc = new CityListClient();
        clc.get(new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                ParseCitiesAsyncTask pcat = new ParseCitiesAsyncTask();
                pcat.execute(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (map != null) {
            this.map = map;
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Intent intent = new Intent(getApplicationContext(), CityInfoActivity.class);
                    intent.putExtra("city", cities.get(marker.getTitle()));
                    if(current_location != null) {
                        intent.putExtra("latitude", current_location.getLatitude());
                        intent.putExtra("longitude", current_location.getLongitude());
                    }
                    startActivity(intent);
                    return true;
                }
            });
            setUpMap();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        current_location = location;
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

    class ParseCitiesAsyncTask extends AsyncTask<String, City, Void> {
        public int progress_count = 0;
        public int total = 0;
        @Override
        protected Void doInBackground(String... params) {
            Geocoder gc = new Geocoder(MainActivity.this, Locale.getDefault());
            try {
                JSONObject json = new JSONObject(params[0]);
                JSONArray array = json.getJSONArray("features");
                total = array.length();
                for (int i = 0; i < array.length(); i++) {
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
                    publishProgress(city);
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pd.dismiss();
        }

        @Override
        protected void onProgressUpdate(City... values) {
            progress_count++;
            pd.setMessage("Found " + progress_count + " of " + total + " cities");
            if (map != null) {
                Marker m = map.addMarker(new MarkerOptions().position(values[0].getGeoPosition()).title(values[0].getId()));
            }
        }
    }
}
