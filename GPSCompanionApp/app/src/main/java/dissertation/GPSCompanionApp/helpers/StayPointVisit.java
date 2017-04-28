package dissertation.GPSCompanionApp.helpers;

import java.util.GregorianCalendar;

/**
 * Created by Peter on 02/02/2017.
 */

public class StayPointVisit {
    private String _START;
    private String _END;
    private double _STAYPOINT_ID;

    public StayPointVisit(double stayPointId, String start, String end){
        this._STAYPOINT_ID = stayPointId;
        this._START = start;
        this._END = end;
    }

    public String get_START() {
        return _START;
    }

    public String get_END() {
        return _END;
    }

    public double get_STAYPOINT_ID() {
        return _STAYPOINT_ID;
    }

    public long getDuration(){
        GregorianCalendar startTime = Utils.getGregDateTimeFrom(get_START());
        GregorianCalendar endTime = Utils.getGregDateTimeFrom(get_END());
        return endTime.getTimeInMillis() - startTime.getTimeInMillis();
    }

    @Override
    public String toString(){
        return get_STAYPOINT_ID() + ", Start: " +  get_START() + ", End: " +  get_END();
    }
}
