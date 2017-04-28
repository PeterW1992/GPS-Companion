package dissertation.GPSCompanionApp.helpers;

import java.util.GregorianCalendar;

/**
 * Created by Peter on 24/02/2017.
 */

public class Journey implements Comparable{
    private double rowid;
    private double startPoint;
    private double endPoint;
    private String startDateTime;
    private String endDateTime;

    public Journey(double rowid, double startPoint, double endPoint, String startDateTime, String endDateTime){
        this.rowid = rowid;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public double getStartPoint() {
        return startPoint;
    }

    public double getEndPoint() {
        return endPoint;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public long getDuration(){
        GregorianCalendar start = Utils.getGregDateTimeFrom(getStartDateTime());
        GregorianCalendar end = Utils.getGregDateTimeFrom(getEndDateTime());
        return end.getTimeInMillis() - start.getTimeInMillis();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        GregorianCalendar thisStartTime = Utils.getGregDateTimeFrom(this.getStartDateTime());
        GregorianCalendar otherStartTime = Utils.getGregDateTimeFrom(((Journey) o).getStartDateTime());
        return thisStartTime.compareTo(otherStartTime);
    }

    @Override
    public String toString() {
        long diff = Utils.getGregDateTimeFrom(endDateTime).getTimeInMillis() - Utils.getGregDateTimeFrom(startDateTime).getTimeInMillis();
        return Utils.getTimeReadable(startDateTime) + " - " + Utils.getDurationFormat(diff);
    }

    public double getRowid() {
        return rowid;
    }
}
