package geoinfo.demo.app.geoinfo.listeners;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by david on 6.8.2015..
 */
public interface LocationChangedListener {
    void locationChanged(LatLng location);
}
