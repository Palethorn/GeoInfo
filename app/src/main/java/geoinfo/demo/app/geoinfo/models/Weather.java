package geoinfo.demo.app.geoinfo.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by david on 30/07/2015.
 */
public class Weather {
    private Date date;
    private double day_temp;
    private double min_temp;
    private double max_temp;
    private double night_temp;
    private double eve_temp;
    private double morn_temp;
    private double pressure;
    private int humidity;
    private String status;
    private String description;
    private String icon;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getDayTemp() {
        return day_temp;
    }

    public void setDayTemp(float day_temp) {
        this.day_temp = day_temp;
    }

    public double getMinTemp() {
        return min_temp;
    }

    public void setMinTemp(float min_temp) {
        this.min_temp = min_temp;
    }

    public double getMaxTemp() {
        return max_temp;
    }

    public void setMaxTemp(float max_temp) {
        this.max_temp = max_temp;
    }

    public double getNightTemp() {
        return night_temp;
    }

    public void setNightTemp(float night_temp) {
        this.night_temp = night_temp;
    }

    public double getEveTemp() {
        return eve_temp;
    }

    public void setEveTemp(float eve_temp) {
        this.eve_temp = eve_temp;
    }

    public double getMornTemp() {
        return morn_temp;
    }

    public void setMornTemp(float morn_temp) {
        this.morn_temp = morn_temp;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void importFromJsonObject(JSONObject o) {
        try {
            int dt = o.getInt("dt");
            date = new Date(dt * 1000);
            if(o.has("pressure")) {
                pressure = o.getDouble("pressure");
            }
            if(o.has("humiditiy")) {
                humidity = o.getInt("humidity");
            }
            if(o.has("main")) {
                JSONObject main = o.getJSONObject("main");
                day_temp = main.has("temp") ? main.getDouble("temp") : 0.0;
                pressure = main.has("pressure") ? main.getDouble("pressure") : 0.0;
                humidity = main.has("humidity") ? main.getInt("humidity") : 0;
                max_temp = main.has("temp_max") ? main.getDouble("temp_max") : 0.0;
                min_temp = main.has("temp_min") ? main.getDouble("temp_min") : 0.0;
            } else if(o.has("temp")) {
                JSONObject temp = o.getJSONObject("temp");
                day_temp = temp.has("day") ? temp.getDouble("day") : 0.0;
                min_temp = temp.has("min") ? temp.getDouble("min") : 0.0;
                max_temp = temp.has("max") ? temp.getDouble("max") : 0.0;
                night_temp = temp.has("night") ? temp.getDouble("night") : 0.0;
                eve_temp = temp.has("eve") ? temp.getDouble("eve") : 0.0;
                morn_temp = temp.has("morn") ? temp.getDouble("morn") : 0.0;
            }

            if(o.has("weather")) {
                JSONArray weather = o.getJSONArray("weather");
                JSONObject w = weather.length() > 0 ? weather.getJSONObject(0) : new JSONObject();
                status = w.has("main") ? w.getString("main") : "";
                description = w.has("description") ? w.getString("description") : "";
                icon = w.has("icon") ? w.getString("icon") : "";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
