package geoinfo.demo.app.geoinfo.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.listeners.CityListAvailableListener;
import geoinfo.demo.app.geoinfo.listeners.LocationChangedListener;
import geoinfo.demo.app.geoinfo.listeners.ProgressUpdateListener;
import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.services.GeoInfoService;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String TAG = "MainActivity";
    private GoogleMap map;
    private ProgressDialog pd;
    private Marker current_location_marker;
    GeoInfoService gis;
    GeoInfoServiceConnection gisc;
    private boolean city_listing_in_progress;
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        city_listing_in_progress = false;
        initProgressDialog();
        initWifiRequirements();
        initService();
    }

    private void initWifiRequirements() {
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        wifiReceiver = new WifiReceiver();
        this.registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void initProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
    }

    private void initService() {
        gisc = new GeoInfoServiceConnection();
        if (!GeoInfoService.isRunning()) {
            Intent i = new Intent(this, GeoInfoService.class);
            startService(i);
        }
    }

    private void updateCurrentLocation(LatLng location) {
        if (current_location_marker != null) {
            current_location_marker.remove();
        }
        if (map != null && gis.getCurrentLocation() != null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title("Current location");
            markerOptions.position(location);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            current_location_marker = map.addMarker(markerOptions);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleProgressDialog("Please wait...", true);

        bindService(new Intent(this, GeoInfoService.class), gisc, 0);
    }

    private void initMap() {
        if (map == null) {
            ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        } else {
            toggleProgressDialog("", false);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (map != null) {
            this.map = map;
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Intent intent = new Intent(getApplicationContext(), CityInfoActivity.class);
                    intent.putExtra("city", gis.getCities().get(marker.getTitle()));
                    if (gis.getCurrentLocation() != null) {
                        intent.putExtra("latitude", gis.getCurrentLocation().latitude);
                        intent.putExtra("longitude", gis.getCurrentLocation().longitude);
                    }
                    startActivity(intent);
                    return true;
                }
            });
            setupCityList();
        }
    }

    public void setupCityList() {
        if (city_listing_in_progress || gis == null || !GeoInfoService.isRunning() || networkInfo == null) {
            return;
        }
        if (networkInfo.isConnectedOrConnecting()) {
            city_listing_in_progress = true;
            gis.retrieveCityList(new CityListAvailableListener() {
                @Override
                public void onList(HashMap<String, City> list) {
                    city_listing_in_progress = false;
                    initMarkers();
                }
            }, new ProgressUpdateListener() {
                @Override
                public void onProgress(Double... progress) {
                    toggleProgressDialog("Found " + progress[0].intValue() + " of " + progress[1].intValue() + " cities", true);
                }
            });
        } else {
            toggleProgressDialog("Please enable wifi", true);
        }
    }

    public void initMarkers() {
        map.clear();
        for (String k : gis.getCities().keySet()) {
            City c = gis.getCities().get(k);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(c.getId());
            markerOptions.position(new LatLng(c.getGeoPosition().latitude, c.getGeoPosition().longitude));
            map.addMarker(markerOptions);
        }
        toggleProgressDialog("", false);
    }

    public class GeoInfoServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final GeoInfoService.GeoInfoServiceBinder binder = (GeoInfoService.GeoInfoServiceBinder)service;
            gis = binder.getService();
            updateCurrentLocation(gis.getCurrentLocation());
            initMap();
            gis.setLocationChangedListener(new LocationChangedListener() {
                @Override
                public void locationChanged(LatLng location) {
                    updateCurrentLocation(location);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    public void onPause() {
        unbindService(gisc);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiReceiver);
        super.onDestroy();
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnectedOrConnecting()) {
                toggleProgressDialog("Please wait...", true);
                setupCityList();
            } else {
                city_listing_in_progress = false;
                if (gis != null && GeoInfoService.isRunning()) {
                    gis.cancelCityListParsing();
                }
                toggleProgressDialog("Please enable wifi", true);
            }
        }
    }

    private void toggleProgressDialog(String message, boolean open) {
        pd.setMessage(message);

        if (pd.isShowing() == open) {
            return;
        }
        if (!open) {
            pd.dismiss();
            return;
        }
        pd.show();
    }
}
