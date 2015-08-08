package geoinfo.demo.app.geoinfo.utilities;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import geoinfo.demo.app.geoinfo.models.City;

/**
 * Created by david on 28/07/2015.
 */
public class ForecastClient {
    public static final String TAG = "ForecastClient";
    public static final String URL = "http://api.openweathermap.org/data/2.5/forecast/daily";

    private AsyncHttpClient client = new AsyncHttpClient();

    public void get(City city, int count,  AsyncHttpResponseHandler responseHandler) {
        RequestParams rp = new RequestParams();
        rp.put("lat", city.getGeoPosition().latitude);
        rp.put("lon", city.getGeoPosition().longitude);
        rp.put("cnt", count);
        rp.put("mode", "json");
        rp.put("units", "metric");
        client.get(URL, rp, responseHandler);
    }
}
