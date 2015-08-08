package geoinfo.demo.app.geoinfo.utilities;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by david on 28/07/2015.
 */
public class TimeZoneClient {
    public static final String TAG = "TimeZoneClient";
    public static final String URL = "https://maps.googleapis.com/maps/api/timezone/json";
    public static String KEY;

    private AsyncHttpClient client = new AsyncHttpClient();

    public void get(LatLng location, AsyncHttpResponseHandler responseHandler) {
        RequestParams rp = new RequestParams();
        rp.put("location", location.latitude + "," + location.longitude);
        rp.put("key", KEY);
        rp.put("timestamp", System.currentTimeMillis() / 1000);
        Log.d(TAG, "Retrieving city information: " + URL + rp.toString());
        client.get(URL, rp, responseHandler);
    }
}
