package dissertation.GPSCompanionApp.helpers;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Peter on 13/01/2017.
 */

public class GPSPoint {
    private double _ROWID;
    private double _LAT;
    private double _LON;
    private String _dateTime;
    private Double _ALT;
    private Double _SPEED;
    private Integer _MODE;
    private Double _TRACK;
    private Double _JOURNEYID;

    public GPSPoint(double rowid, double lat, double lon, String dateTime, Double alt, Double speed, Integer mode, Double track, Double journeyID){
        this._ROWID = rowid;
        this._LAT = lat;
        this._LON = lon;
        this._dateTime = dateTime;
        this._ALT = alt;
        this._SPEED = speed;
        this._MODE = mode;
        this._TRACK = track;
        this._JOURNEYID = journeyID;
    }

    public GPSPoint(double lat, double lon){
        this._LAT = lat;
        this._LON = lon;
    }

    public LatLng getLatLng(){
        return new LatLng(_LAT, _LON);
    }

    public double get_LAT() {
        return _LAT;
    }

    public double get_LON() {
        return _LON;
    }

    public String get_dateTime() {
        return _dateTime;
    }

    public Double get_ALT() {
        return _ALT;
    }

    public Double get_SPEED() {
        return _SPEED;
    }

    public Integer get_MODE() {
        return _MODE;
    }

    public Double get_TRACK() {
        return _TRACK;
    }

    public double get_ROWID() {
        return _ROWID;
    }

    public Double get_JOURNEYID() {
        return _JOURNEYID;
    }
}
