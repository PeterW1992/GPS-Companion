package dissertation.GPSCompanionApp.helpers;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Peter on 02/02/2017.
 */

public class StayPoint {
    private double _LAT;
    private double _LON;
    private double _ROW_ID;

    public StayPoint(double row_id, double lat, double lon){
        this._ROW_ID = row_id;
        this._LAT = lat;
        this._LON = lon;
    }

    public double get_LAT() {
        return _LAT;
    }

    public double get_LON() {
        return _LON;
    }

    public LatLng getLatLng(){
        return new LatLng(_LAT, _LON);
    }

    public double get_ROW_ID() {
        return _ROW_ID;
    }
}
