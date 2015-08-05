package geoinfo.demo.app.geoinfo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.adapters.ForecastAdapter;
import geoinfo.demo.app.geoinfo.models.City;
import geoinfo.demo.app.geoinfo.models.Weather;
/**
 * Created by david on 4.8.2015..
 */
public class WeatherInfoFragment extends Fragment {
    private ListView forecast_list_view;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.layout_weather_info, container, false);
        mapViews(rootView);
        return rootView;
    }

    public void mapViews(View rootView) {
        forecast_list_view = (ListView)rootView.findViewById(R.id.weather_list);
    }

    public void updateInfo(List<Weather> forecast) {
        ForecastAdapter fa = new ForecastAdapter(getActivity(), R.layout.weather_item_layout, forecast);
        forecast_list_view.setAdapter(fa);
    }
}
