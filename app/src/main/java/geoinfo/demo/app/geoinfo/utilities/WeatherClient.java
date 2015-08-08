package geoinfo.demo.app.geoinfo.utilities;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import geoinfo.demo.app.geoinfo.models.City;

/**
 * Created by david on 31/07/2015.
 */
public class WeatherClient {
    public static final String TAG = "WeatherClient";
    public static final String URL = "http://api.openweathermap.org/data/2.5/weather";

    private AsyncHttpClient client = new AsyncHttpClient();

    public void get(City city, AsyncHttpResponseHandler responseHandler) {
        RequestParams rp = new RequestParams();
        rp.put("lat", city.getGeoPosition().latitude);
        rp.put("lon", city.getGeoPosition().longitude);
        rp.put("mode", "json");
        rp.put("units", "metric");
        client.get(URL, rp, responseHandler);
    }
}
