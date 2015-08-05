package geoinfo.demo.app.geoinfo.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        gisc = new GeoInfoServiceConnection();

        if(!GeoInfoService.isRunning()) {
            Intent i = new Intent(this, GeoInfoService.class);
            startService(i);
        }
    }

    private void updateCurrentLocation() {
        if(map != null && gis.getCurrentLocation() != null) {
            current_location_marker = map.addMarker(
                    new MarkerOptions()
                            .title("Current location")
                            .position(new LatLng(gis.getCurrentLocation().getLatitude(), gis.getCurrentLocation().getLongitude()))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pd.setMessage("Please wait...");
        pd.show();
        bindService(new Intent(this, GeoInfoService.class), gisc, 0);
    }

    private void initMap() {
        if (map == null) {
            ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        } else {
            pd.dismiss();
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
                    if(gis.getCurrentLocation() != null) {
                        intent.putExtra("latitude", gis.getCurrentLocation().getLatitude());
                        intent.putExtra("longitude", gis.getCurrentLocation().getLongitude());
                    }
                    startActivity(intent);
                    return true;
                }
            });

            gis.retrieveCityList(new CityListAvailableListener() {
                @Override
                public void onList(HashMap<String, City> list) {
                    initMarkers();
                }
            }, new ProgressUpdateListener() {
                @Override
                public void onProgress(Double... progress) {
                    pd.setMessage("Found " + progress[0].intValue() + " of " + progress[1].intValue() + " cities");
                }
            });
        }
    }

    public void initMarkers() {
        map.clear();
        for (String k: gis.getCities().keySet()) {
            City c = gis.getCities().get(k);
            map.addMarker(
                    new MarkerOptions()
                            .title(c.getId())
                            .position(new LatLng(c.getGeoPosition().latitude, c.getGeoPosition().longitude)));
        }
        pd.dismiss();
    }

    public class GeoInfoServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final GeoInfoService.GeoInfoServiceBinder binder = (GeoInfoService.GeoInfoServiceBinder)service;
            gis = binder.getService();
            initMap();
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
        super.onDestroy();
    }
}
