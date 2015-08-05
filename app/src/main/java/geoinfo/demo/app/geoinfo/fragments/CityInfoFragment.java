package geoinfo.demo.app.geoinfo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.models.City;

/**
 * Created by david on 4.8.2015..
 */
public class CityInfoFragment extends Fragment {
    public static final String TAG = "CityInfoFragment";

    TextView name;
    TextView position;
    TextView country;
    TextView timezone;
    TextView air_distance;
    float distance;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreateView");
        distance = getArguments().getFloat("distance");

        View rootView = inflater.inflate(
                R.layout.layout_city_info, container, false);

        name = (TextView)rootView.findViewById(R.id.city_name);
        position = (TextView)rootView.findViewById(R.id.city_position);
        country = (TextView)rootView.findViewById(R.id.city_country);
        timezone = (TextView)rootView.findViewById(R.id.city_timezone);
        air_distance = (TextView)rootView.findViewById(R.id.city_air_distance);
        return rootView;
    }

    public void updateInfo(City city) {
        if (city == null) {
            return;
        }
        name.setText("City Name: " + city.getName());
        position.setText("City Geo Position: " + city.getGeoPosition().latitude + "," + city.getGeoPosition().longitude);
        country.setText("City Country: " + city.getCountryName());
        air_distance.setText("Air distance: " + String.valueOf(distance) + " meters");
        timezone.setText("Timezone: " + city.getTimezone());
    }
}