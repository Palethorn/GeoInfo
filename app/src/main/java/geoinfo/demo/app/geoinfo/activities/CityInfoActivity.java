package geoinfo.demo.app.geoinfo.activities;
import android.app.ProgressDialog;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.models.Weather;
import geoinfo.demo.app.geoinfo.utilities.ForecastClient;
import geoinfo.demo.app.geoinfo.utilities.TimeZoneClient;
import geoinfo.demo.app.geoinfo.utilities.WeatherClient;

public class CityInfoActivity extends FragmentActivity {
    public static final String TAG = "CityInfoActivity";
    List<Fragment> fragments;
    City city;
    float distance;
    FragmentManager fragmentManager;
    ListView left_drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_info);
        mapViews();
        fragmentManager = getSupportFragmentManager();
        city = getIntent().getParcelableExtra("city");
        Double latitude = getIntent().getDoubleExtra("latitude", -1);
        Double longitude = getIntent().getDoubleExtra("longitude", -1);
        float[] results = new float[1];
        if (latitude != -1 || longitude != -1) {
            Location.distanceBetween(latitude, longitude, city.getGeoPosition().latitude, city.getGeoPosition().longitude, results);
        }
        distance = results[0];
        initFragments();
    }

    public void mapViews() {
        left_drawer = (ListView)findViewById(R.id.left_drawer);
        List<String> list = new ArrayList<>();
        list.add("City info");
        list.add("Weather info");
        left_drawer.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, list));
        left_drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_frame_layout, fragments.get(position))
                        .commit();
            }
        });
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

        fragmentManager.beginTransaction().add(R.id.fragment_frame_layout, cityInfoFragment).commit();
    }

    public static class CityInfoFragment extends Fragment {
        private City city;
        public static final String TAG = "CityInfoFragment";

        TextView name;
        TextView position;
        TextView country;
        TextView timezone;
        TextView air_distance;
        float distance;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            Log.d(TAG, "OnCreateView");
            city = getArguments().getParcelable("city");
            distance = getArguments().getFloat("distance");

            View rootView = inflater.inflate(
                    R.layout.layout_city_info, container, false);

            name = (TextView)rootView.findViewById(R.id.city_name);
            position = (TextView)rootView.findViewById(R.id.city_position);
            country = (TextView)rootView.findViewById(R.id.city_country);
            timezone = (TextView)rootView.findViewById(R.id.city_timezone);
            air_distance = (TextView)rootView.findViewById(R.id.city_air_distance);
            updateInfo();
            return rootView;
        }

        public void updateInfo() {
            if (city == null) {
                return;
            }
            name.setText("City Name: " + city.getName());
            position.setText("City Geo Position: " + city.getGeoPosition().latitude + "," + city.getGeoPosition().longitude);
            country.setText("City Country: " + city.getCountryName());
            air_distance.setText("Air distance: " + String.valueOf(distance) + " meters");
            final ProgressDialog pd = new ProgressDialog(getActivity());
            pd.setMessage("Retrieving timezone...");
            pd.show();
            TimeZoneClient tzc = new TimeZoneClient();
            tzc.get(city.getGeoPosition(), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    try {
                        JSONObject object = new JSONObject(result);
                        int offset = object.getInt("rawOffset");
                        offset = offset / 60 / 60;
                        timezone.setText("Timezone: GMT " + (offset < 0 ? "-" : "+") + String.valueOf(offset));
                    } catch (JSONException e) {
                        timezone.setText("Timezone: Could not retrieve");
                        e.printStackTrace();
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }

    public static class WeatherInfoFragment extends Fragment {
        private City city;
        private List<Weather> forecast;
        private Weather current_weather;
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            city = getArguments().getParcelable("city");
            View rootView = inflater.inflate(
                    R.layout.layout_weather_info, container, false);
            forecast = new ArrayList<>();
            updateInfo();
            return rootView;
        }

        public void updateInfo() {
            ForecastClient fc = new ForecastClient();
            fc.get(city, 6, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        JSONObject json = new JSONObject(new String(responseBody));
                        JSONArray array = json.getJSONArray("list");
                        for(int i = 1; i < array.length(); i++) {
                            Weather w = new Weather();
                            w.importFromJsonObject(array.getJSONObject(i));
                            forecast.add(w);
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
                        current_weather = new Weather();
                        current_weather.importFromJsonObject(json);
                        forecast.add(0, current_weather);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }

}
