package geoinfo.demo.app.geoinfo.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

import geoinfo.demo.app.geoinfo.models.Weather;

/**
 * Created by david on 3.8.2015..
 */
public class ImageDownloader extends AsyncTask<Weather, Void, Bitmap> {
    public static final String URL = "http://openweathermap.org/img/w";

    ImageView iv;
    Weather w;

    public ImageDownloader(ImageView iv, Weather w) {
        super();
        this.iv = iv;
        this.w = w;
    }

    @Override
    protected Bitmap doInBackground(Weather... params) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(URL + "/" + w.getIcon() + ".png");
        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            Bitmap b = BitmapFactory.decodeStream(inputStream);
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        w.setIconBitmap(result);
        iv.setImageBitmap(result);
        w.setImageDownloadLock(false);
    }
}
