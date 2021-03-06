package geoinfo.demo.app.geoinfo.utilities;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * Created by david on 28/07/2015.
 */
public class CityListClient {
    public static final String TAG = "CityListClient";
    public static final String URL = "https://raw.githubusercontent.com/mahemoff/geodata/master/cities.geojson";

    private AsyncHttpClient client = new AsyncHttpClient();

    public void get(AsyncHttpResponseHandler responseHandler) {
        client.get(URL, null, responseHandler);
    }
}
