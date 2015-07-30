package geoinfo.demo.app.geoinfo.activities;

import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.utilities.TimeZoneClient;

public class CityInfoActivity extends FragmentActivity {

    CityInfoPagerAdapter cityInfoPagerAdapter;
    ViewPager viewPager;
    List<Fragment> fragments;
    LatLng current_location;
    City city;
    float distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_info);
        city = getIntent().getParcelableExtra("city");
        Double latitude = getIntent().getDoubleExtra("latitude", -1);
        Double longitude = getIntent().getDoubleExtra("longitude", -1);
        float[] results = new float[1];
        if(latitude != -1 || longitude != -1) {
            Location.distanceBetween(latitude, longitude, city.getGeoPosition().latitude, city.getGeoPosition().longitude, results);
        }
        distance = results[0];
        initFragments();
        cityInfoPagerAdapter =
                new CityInfoPagerAdapter(
                        getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(cityInfoPagerAdapter);
    }

    private void initFragments() {
        fragments = new ArrayList<>();
        CityInfoFragment cityInfoFragment = new CityInfoFragment();
        Bundle b = new Bundle();
        b.putFloat("distance", distance);
        b.putParcelable("city", city);
        cityInfoFragment.setArguments(b);
        fragments.add(0, cityInfoFragment);

        WeatherInfoFragment weatherInfoFragment = new WeatherInfoFragment();
        b = new Bundle();
        b.putParcelable("city", city);
        weatherInfoFragment.setArguments(b);
        fragments.add(1, weatherInfoFragment);
    }

    public class CityInfoPagerAdapter extends FragmentStatePagerAdapter {
        public CityInfoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment frag = fragments.get(i);
            return frag;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    public static class CityInfoFragment extends Fragment {
        private City city;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            city = getArguments().getParcelable("city");
            float distance = getArguments().getFloat("distance");
            View rootView = inflater.inflate(
                    R.layout.layout_city_info, container, false);

            TextView name = (TextView)rootView.findViewById(R.id.city_name);
            name.setText("City Name: " + city.getName());
            TextView position = (TextView)rootView.findViewById(R.id.city_position);
            position.setText("City Geo Position: " + city.getGeoPosition().latitude + "," + city.getGeoPosition().longitude);
            TextView country = (TextView)rootView.findViewById(R.id.city_country);
            country.setText("City Country: " + city.getCountryName());
            final TextView timezone = (TextView)rootView.findViewById(R.id.city_timezone);
            TextView air_distance = (TextView)rootView.findViewById(R.id.city_air_distance);
            air_distance.setText("Air distance: " + String.valueOf(distance) + " meters");
            TimeZoneClient tzc = new TimeZoneClient();
            tzc.get(city.getGeoPosition(), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    try {
                        JSONObject object = new JSONObject(result);
                        int offset = object.getInt("rawOffset");
                        offset = offset / 60 / 60;
                        timezone.setText("Timezone: GMT" + (offset < 0 ? "-" : "+") + String.valueOf(offset));
                    } catch (JSONException e) {
                        timezone.setText("Timezone: Could not retrieve");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });

            return rootView;
        }
    }

    public static class WeatherInfoFragment extends Fragment {
        private City city;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            city = getArguments().getParcelable("city");
            View rootView = inflater.inflate(
                    R.layout.layout_weather_info, container, false);

            return rootView;
        }
    }

}
