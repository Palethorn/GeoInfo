package geoinfo.demo.app.geoinfo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import geoinfo.demo.app.geoinfo.R;
import geoinfo.demo.app.geoinfo.models.Weather;
import geoinfo.demo.app.geoinfo.utilities.ImageDownloader;

/**
 * Created by david on 3.8.2015..
 */
public class ForecastAdapter extends ArrayAdapter<Weather> {

    Context context;
    int resource_id;
    List<Weather> objects;

    public ForecastAdapter(Context context, int resource, List<Weather> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource_id = resource;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // inflate the layout
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(resource_id, parent, false);
        }
        Weather w = objects.get(position);
        TextView tv = (TextView)convertView.findViewById(R.id.weather_status);
        tv.setText(w.getStatus());
        tv = (TextView)convertView.findViewById(R.id.temperature);
        tv.setText(String.valueOf(w.getDayTemp()) + "°C");
        tv = (TextView)convertView.findViewById(R.id.forecast_day_tag);
        tv.setText(w.getTag());
        ImageView iv = (ImageView)convertView.findViewById(R.id.weather_icon);

        if(w.getIconBitmap() == null && !w.getImageDownloadLock()) {
            w.setImageDownloadLock(true);
            retrieveWeatherStatusIcon(iv, w);
            return convertView;
        }
        iv.setImageBitmap(w.getIconBitmap());
        return convertView;
    }

    public void retrieveWeatherStatusIcon(ImageView iv, Weather w) {
        ImageDownloader id = new ImageDownloader(iv, w);
        id.execute();
    }
}
