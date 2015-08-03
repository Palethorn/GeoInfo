package geoinfo.demo.app.geoinfo.utilities;

import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import geoinfo.demo.app.geoinfo.models.Weather;

/**
 * Created by david on 3.8.2015..
 */
public class WeatherStatusIconClient {
    public static final String TAG = "WeatherStatusIconClient";
    public static final String URL = "http://openweathermap.org/img/w";

    private AsyncHttpClient client = new AsyncHttpClient();

    public void get(Weather w, AsyncHttpResponseHandler responseHandler) {
        String url = URL + "/" + w.getIcon() + ".png";
        Log.d(TAG, "Retrieving weather information: " + url);
        client.get(url, null, responseHandler);
    }
}
