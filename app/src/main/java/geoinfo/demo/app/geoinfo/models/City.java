package geoinfo.demo.app.geoinfo.models;

import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by david on 28/07/2015.
 */
public class City implements Parcelable {
    private LatLng geo_position;
    private String name;
    private String id;
    private String country_name;
    private String country_code;
    private String timezone;
    public City() {
    }

    public City(Parcel p) {
        String[] s = new String[7];
        p.readStringArray(s);
        name = s[0];
        id = s[1];
        geo_position = new LatLng(Double.valueOf(s[2]), Double.valueOf(s[3]));
        country_code = s[4];
        country_name = s[5];
        timezone = s[6];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLng getGeoPosition() {
        return geo_position;
    }

    public void setGeoPosition(LatLng geo_position) {
        this.geo_position = geo_position;
    }

    public void importFromJsonObject(JSONObject object) {
        try {
            setId(object.getString("id"));
            setName(object.getJSONObject("properties").getString("city"));
            LatLng p = new LatLng(
                    object.getJSONObject("geometry").getJSONArray("coordinates").getInt(1),
                    object.getJSONObject("geometry").getJSONArray("coordinates").getInt(0)
            );

            setGeoPosition(p);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                name,
                id,
                String.valueOf(geo_position.latitude),
                String.valueOf(geo_position.longitude),
                country_code,
                country_name,
                timezone
        });
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {

        @Override
        public City createFromParcel(Parcel source) {
            return new City(source);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    public String getCountryName() {
        return country_name;
    }

    public void setCountryName(String country_name) {
        this.country_name = country_name;
    }

    public String getCountryCode() {
        return country_code;
    }

    public void setCountryCode(String country_code) {
        this.country_code = country_code;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
