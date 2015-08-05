package geoinfo.demo.app.geoinfo.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.fragments.CityInfoFragment;
import geoinfo.demo.app.geoinfo.fragments.WeatherInfoFragment;
import geoinfo.demo.app.geoinfo.listeners.CityInfoAvailableListener;
import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.models.Weather;
import geoinfo.demo.app.geoinfo.services.GeoInfoService;

public class CityInfoActivity extends FragmentActivity {
    public static final String TAG = "CityInfoActivity";
    List<Fragment> fragments;
    City city;
    float distance;
    FragmentManager fragmentManager;
    ListView left_drawer;
    ImageView menu_btn;
    Button current_location_btn;
    int current_fragment;
    GeoInfoService gis;
    GeoInfoServiceConnection gisc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_info);
        mapViews();
        if(savedInstanceState != null) {
            current_fragment = savedInstanceState.getInt("current_fragment", 0);
        } else {
            current_fragment = 0;
        }
        fragmentManager = getSupportFragmentManager();
        city = getIntent().getParcelableExtra("city");
        Double latitude = getIntent().getDoubleExtra("latitude", -1);
        Double longitude = getIntent().getDoubleExtra("longitude", -1);
        float[] results = new float[1];
        if (latitude != -1 && longitude != -1 && city != null) {
            Location.distanceBetween(latitude, longitude, city.getGeoPosition().latitude, city.getGeoPosition().longitude, results);
        }
        distance = results[0];
        initFragments();
        gisc = new GeoInfoServiceConnection();

        if(!GeoInfoService.isRunning()) {
            Intent i = new Intent(this, GeoInfoService.class);
            startService(i);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, GeoInfoService.class), gisc, 0);
    }

    public void mapViews() {
        left_drawer = (ListView)findViewById(R.id.left_drawer);
        List<String> list = new ArrayList<>();
        list.add("Map");
        list.add("City info");
        list.add("Weather info");
        left_drawer.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, list));
        left_drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    finish();
                    return;
                }
                current_fragment = position - 1;
                fragmentManager.beginTransaction().show(fragments.get(current_fragment)).commit();
                fragmentManager.beginTransaction().hide(fragments.get(current_fragment == 1 ? 0 : 1)).commit();
                DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
                dl.closeDrawer(Gravity.LEFT);
            }
        });

        menu_btn = (ImageView)findViewById(R.id.menu_btn);
        menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
                dl.openDrawer(Gravity.LEFT);
            }
        });

        current_location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gis.setCurrentLocation(city.getGeoPosition());
            }
        });
    }

    private void initFragments() {
        fragments = new ArrayList<>();
        CityInfoFragment cityInfoFragment = (CityInfoFragment)fragmentManager.findFragmentByTag("CityInfoFragment");
        WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment)fragmentManager.findFragmentByTag("WeatherInfoFragment");
        Bundle b;
        if(cityInfoFragment == null) {
            cityInfoFragment = new CityInfoFragment();
            b = new Bundle();
            b.putFloat("distance", distance);
            cityInfoFragment.setArguments(b);
            fragmentManager.beginTransaction().add(R.id.fragment_frame_layout, cityInfoFragment, "CityInfoFragment").commit();
        }
        if(weatherInfoFragment == null) {
            weatherInfoFragment = new WeatherInfoFragment();
            b = new Bundle();
            b.putParcelable("city", city);
            weatherInfoFragment.setArguments(b);
            fragmentManager.beginTransaction().add(R.id.fragment_frame_layout, weatherInfoFragment, "WeatherInfoFragment").commit();
        }

        fragments.add(0, cityInfoFragment);
        fragments.add(1, weatherInfoFragment);
        fragmentManager.beginTransaction().show(fragments.get(current_fragment)).commit();
        fragmentManager.beginTransaction().hide(fragments.get(current_fragment == 1 ? 0 : 1)).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("current_fragment", current_fragment);
        fragmentManager.beginTransaction().remove(fragments.get(0)).commit();
        fragmentManager.beginTransaction().remove(fragments.get(1)).commit();
        super.onSaveInstanceState(state);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(gisc);
    }

    public class GeoInfoServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final GeoInfoService.GeoInfoServiceBinder binder = (GeoInfoService.GeoInfoServiceBinder)service;
            gis = binder.getService();
            final ProgressDialog pd = new ProgressDialog(CityInfoActivity.this);
            pd.setCancelable(false);
            pd.setMessage("Retrieving detailed info...");
            pd.show();
            gis.updateInfo(city, new CityInfoAvailableListener() {
                @Override
                public void onInfo(City city, List<Weather> forecast) {
                    ((CityInfoFragment)fragments.get(0)).updateInfo(city);
                    ((WeatherInfoFragment)fragments.get(1)).updateInfo(forecast);
                    pd.dismiss();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
