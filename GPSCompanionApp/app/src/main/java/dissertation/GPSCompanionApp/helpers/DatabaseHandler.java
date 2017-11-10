package dissertation.GPSCompanionApp.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by Peter on 14/01/2017.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static int VERSION = 3;
    private static String DATABASE_NAME = "GPSLogger";

    public static String TBL_GPSPOINTS = "tblGPSPoints", TBL_STAYPOINTS = "tblStayPoints", TBL_STAYPOINT_VISITS = "tblStayPointVisits",
            TBL_JOURNEYS = "tblJourneys", TBL_UPDATES = "tblUpdates", TBL_LOGGER_STATUS = "tblLoggerStatus";

    public String COL_ROWID = "rowid", COL_LAT = "Lat", COL_LON = "Lon", COL_DATETIME = "DateTime", COL_ALT = "Alt", COL_SPEED = "Speed",
            COL_MODE = "Mode", COL_TRACK = "Track", COL_JOURNEYID = "JourneyId", COL_DATABASE_SIZE = "DatabaseSize", COL_GPS_POINTS = "GPSPoints",
            COL_STAY_POINTS = "StayPoints", COL_VISITS = "Visits", COL_JOURNEYS = "Journeys", COL_LATEST_POINT = "LatestPoint", COL_OLDEST_POINT = "OldestPoint",
            COL_LATEST_STAY_UPDATE = "LatestStayUpdate", COL_LATEST_JOURNEY_UPDATE = "LatestJourneyUpdate";

    private String COL_STAYPOINTID = "StayPointID", COL_START = "StartDateTime", COL_END = "EndDateTime",
            COL_STARTSTAYPOINT = "StartStayPoint", COL_ENDSTAYPOINT = "EndStayPoint";

    private String COL_UPDATE_STARTTIME = "StartTime", COL_UPDATE_ENDTIME = "EndTime";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME,null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBL_GPSPOINTS + " (" +
                COL_LAT + " REAL, " + COL_LON + " REAL, " + COL_DATETIME + " TEXT PRIMARY KEY, " + COL_ALT + " REAL, " +
                COL_SPEED + " REAL, " + COL_MODE + " INTEGER, " + COL_TRACK + " REAL, " + COL_JOURNEYID + " INTEGER," +
                " FOREIGN KEY (" + COL_JOURNEYID + ") REFERENCES " +  TBL_JOURNEYS + "(rowid))");

        db.execSQL("CREATE TABLE " + TBL_STAYPOINTS + " (" +
                COL_LAT + " REAL, " + COL_LON + " REAL , PRIMARY KEY(Lat, Lon))");

        db.execSQL("CREATE TABLE " + TBL_STAYPOINT_VISITS + " (" +
                COL_STAYPOINTID + " INTEGER, "+ COL_START + " TEXT, " + COL_END + " TEXT,  PRIMARY KEY(" + COL_START +
                ") FOREIGN KEY (" + COL_STAYPOINTID + ") REFERENCES " + TBL_STAYPOINTS + "(ROWID))");

        db.execSQL("CREATE TABLE " + TBL_JOURNEYS + "(" + COL_STARTSTAYPOINT + " INTEGER," + COL_ENDSTAYPOINT + " INTEGER, " + COL_START + " TEXT, " + COL_END + " TEXT," +
                " PRIMARY KEY (" + COL_START + ") FOREIGN KEY (" + COL_STARTSTAYPOINT + ") REFERENCES " + TBL_STAYPOINTS +
                "(ROWID) FOREIGN KEY (" + COL_ENDSTAYPOINT + ") REFERENCES " + TBL_STAYPOINTS + "(ROWID))");

        db.execSQL("CREATE TABLE " + TBL_UPDATES + "(" + COL_UPDATE_STARTTIME+ " INTEGER," + COL_UPDATE_ENDTIME + " INTEGER)");

        db.execSQL("CREATE TABLE " + TBL_LOGGER_STATUS + "(" + COL_DATABASE_SIZE + " REAL," + COL_GPS_POINTS + " INTEGER,"+ COL_STAY_POINTS + " INTEGER," +
                COL_VISITS + " INTEGER, "  +  COL_JOURNEYS + " INTEGER," + COL_LATEST_POINT + " TEXT," + COL_OLDEST_POINT + " INTEGER," + COL_LATEST_STAY_UPDATE +
                " TEXT," + COL_LATEST_JOURNEY_UPDATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_GPSPOINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_STAYPOINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_STAYPOINT_VISITS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_JOURNEYS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_UPDATES);
        onCreate(db);
    }

    public void addStayPoints(ArrayList<StayPoint> points){
        if (points != null){
            ContentValues contentValues;
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransactionNonExclusive();
            for (StayPoint point : points){
                contentValues = new ContentValues();
                contentValues.put(COL_ROWID, point.get_ROW_ID());
                contentValues.put(COL_LAT,point.get_LAT());
                contentValues.put(COL_LON,point.get_LON());
                db.insertWithOnConflict(TBL_STAYPOINTS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    public void addStayPointVisits(ArrayList<StayPointVisit> points){
        ContentValues contentValues;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransactionNonExclusive();
        for (StayPointVisit point : points){
            contentValues = new ContentValues();
            contentValues.put(COL_STAYPOINTID, point.get_STAYPOINT_ID());
            contentValues.put(COL_START,point.get_START());
            contentValues.put(COL_END,point.get_END());
            db.insertWithOnConflict(TBL_STAYPOINT_VISITS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void addJourneys(ArrayList<Journey> journeys){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues;
        db.beginTransactionNonExclusive();
        for (Journey journey : journeys){
            contentValues = new ContentValues();
            contentValues.put(COL_ROWID, journey.getRowid());
            contentValues.put(COL_STARTSTAYPOINT, journey.getStartPoint());
            contentValues.put(COL_ENDSTAYPOINT, journey.getEndPoint());
            contentValues.put(COL_START, journey.getStartDateTime());
            contentValues.put(COL_END, journey.getEndDateTime());
            db.insertWithOnConflict(TBL_JOURNEYS,null,contentValues,SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void addJourneyPoints(ArrayList<GPSPoint> gpsPoints){
        System.out.println("Amount of points: " + gpsPoints.size());
        ContentValues contentValues;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransactionNonExclusive();
        for (GPSPoint point : gpsPoints){
            contentValues = new ContentValues();
            contentValues.put(COL_ROWID, point.get_ROWID());
            contentValues.put(COL_LAT,point.get_LAT());
            contentValues.put(COL_LON,point.get_LON());
            contentValues.put(COL_DATETIME, point.get_dateTime());
            contentValues.put(COL_ALT,point.get_ALT());
            contentValues.put(COL_SPEED,point.get_SPEED());
            contentValues.put(COL_MODE,point.get_MODE());
            contentValues.put(COL_TRACK,point.get_TRACK());
            contentValues.put(COL_JOURNEYID, point.get_JOURNEYID());
            db.insertWithOnConflict(TBL_GPSPOINTS , null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void addUpdate(long start, long end){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransactionNonExclusive();
        db.execSQL("INSERT INTO " + TBL_UPDATES + "(" + COL_UPDATE_STARTTIME + ", " + COL_UPDATE_ENDTIME + ") VALUES (" + start + ", " + end + ")");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addSummaryData(ArrayList<String> data){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransactionNonExclusive();
        ContentValues contentValues = new ContentValues();
        if (data != null) {
            contentValues.put(COL_DATABASE_SIZE, data.get(0));
            contentValues.put(COL_GPS_POINTS, data.get(1));
            contentValues.put(COL_STAY_POINTS, data.get(2));
            contentValues.put(COL_VISITS, data.get(3));
            contentValues.put(COL_JOURNEYS, data.get(4));
            contentValues.put(COL_LATEST_POINT, data.get(5));
            contentValues.put(COL_OLDEST_POINT, data.get(6));
            contentValues.put(COL_LATEST_STAY_UPDATE, data.get(7));
            contentValues.put(COL_LATEST_JOURNEY_UPDATE, data.get(8));
            db.insertWithOnConflict(TBL_LOGGER_STATUS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public HashMap<String, String> getLatestSummaryData(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_ROWID + "), * FROM " + TBL_LOGGER_STATUS, null);
        HashMap<String,String> status = new HashMap<>();
        while (cursor.moveToNext()){
            for (int i = 0; i < cursor.getColumnCount() - 1; i++){
                status.put(cursor.getColumnName(i), cursor.getString(i));
            }
        }
        cursor.close();
        return status;
    }

    public Long getLatestUpdate(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_UPDATE_STARTTIME + "), " + COL_UPDATE_ENDTIME + " FROM " + TBL_UPDATES, null);
        cursor.moveToNext();
        Long startTime = cursor.getLong(0);
        cursor.close();
        return startTime;
    }

    public ArrayList<String> getJourneyDates(){
        ArrayList<String> uniqueDates = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT DISTINCT date(" + COL_START + ") as uDate FROM " + TBL_JOURNEYS + " ORDER BY uDate", null);
        while (results.moveToNext()){
            String dateTime = results.getString(0);
            uniqueDates.add(dateTime);
        }
        results.close();
        return uniqueDates;
    }

    public ArrayList<Journey> getJourneysForDates(ArrayList<String> dates){
        ArrayList<Journey> journeys = new ArrayList<>();
        String whereClause = "";
        for (int i = 0; i < dates.size(); i++){
            whereClause = whereClause + " " + COL_START +  " LIKE '" + dates.get(i) + "%' OR";
        }
        whereClause = whereClause.substring(0, whereClause.length() - 3);
        String query = "SELECT DISTINCT(" + TBL_JOURNEYS + ".rowid)," +  TBL_JOURNEYS + ".* FROM " + TBL_JOURNEYS + " INNER JOIN " +
                TBL_GPSPOINTS + " ON " + TBL_JOURNEYS + ".rowid = " + TBL_GPSPOINTS + "." + COL_JOURNEYID + " WHERE " + whereClause + " ORDER BY " + COL_START;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()){
            double rowid = cursor.getDouble(0);
            double startPoint = cursor.getDouble(1);
            double endPoint = cursor.getDouble(2);

            String startDateTime = cursor.getString(3);
            String endDateTime = cursor.getString(4);

            journeys.add(new Journey(rowid, startPoint,endPoint,startDateTime,endDateTime));
        }
        cursor.close();
        return journeys;
    }

    public ArrayList<Journey> getLocalJourneysBetween(Double id1, Double id2, boolean bothDirections){
        ArrayList<Journey> journeys = new ArrayList<>();
        String query;
        if (bothDirections) {
            query = "SELECT " + TBL_JOURNEYS + ".rowid," + "* FROM " + TBL_JOURNEYS +
                    " WHERE "+ COL_STARTSTAYPOINT  + " = " + id1 + " AND " + COL_ENDSTAYPOINT +"=" + id2 +" OR " +
                    COL_STARTSTAYPOINT + " = " + id2 + " AND " + COL_ENDSTAYPOINT + " = " + id1 + "  ORDER BY " + COL_START;
        } else {
            query = "SELECT " + TBL_JOURNEYS + ".rowid," + "* FROM " + TBL_JOURNEYS +
                    " WHERE " + COL_STARTSTAYPOINT  + " = " + id1 + " AND " + COL_ENDSTAYPOINT + "=" + id2 + "  ORDER BY " + COL_START;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            double rowid = cursor.getDouble(0);
            double startPoint = cursor.getDouble(1);
            double endPoint = cursor.getDouble(2);

            String startDateTime = cursor.getString(3);
            String endDateTime = cursor.getString(4);

            journeys.add(new Journey(rowid, startPoint,endPoint,startDateTime, endDateTime));
        }
        cursor.close();
        return journeys;
    }

    public ArrayList<Journey> getJourneyPoints(ArrayList<Journey> journeys){
        for (Journey aJourney : journeys){
            ArrayList<GPSPoint> gpsPoints = new ArrayList<>();
            String query = "SELECT " + COL_LAT + ", " + COL_LON + ", " + COL_SPEED  + " FROM " + TBL_GPSPOINTS +
                    " WHERE " + COL_JOURNEYID  + " = " + aJourney.getRowid() + "  ORDER BY " + COL_DATETIME;
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(query,null);
            while (cursor.moveToNext()){
                double lat = cursor.getDouble(0);
                double lon = cursor.getDouble(1);
                double speed = cursor.getDouble(2);
                gpsPoints.add(new GPSPoint(lat,lon,speed));
            }
            aJourney.addJourneyPoints(gpsPoints);
            cursor.close();
        }
        return journeys;
    }

    public double getAggForJourney(String func, String column,double id){
        String query = "SELECT " + func + "(" + column  + ") FROM " + TBL_GPSPOINTS +
                " WHERE " + COL_JOURNEYID  + " = " + id;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        double val = 0;
        while (cursor.moveToNext()){
            val = cursor.getDouble(0);
        }
        cursor.close();
        return val;
    }

    public ArrayList<StayPoint> getStayPoints(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT rowid, " + COL_LAT + ", " + COL_LON + " FROM " + TBL_STAYPOINTS, null);
        ArrayList<StayPoint> points = new ArrayList<>();
        while (results.moveToNext()){
            double rowid = results.getDouble(0);
            double lat = results.getDouble(1);
            double lon = results.getDouble(2);
            points.add(new StayPoint(rowid,lat, lon));
        }
        results.close();
        return points;
    }

    public ArrayList<StayPoint> getRelatedStayPoints(double stayPointID){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT DISTINCT(" + TBL_STAYPOINTS + "." + COL_ROWID + ")," + COL_LAT + "," + COL_LON + " FROM " + TBL_STAYPOINTS + " LEFT JOIN " + TBL_JOURNEYS + " ON " +
                        TBL_STAYPOINTS + "." + COL_ROWID + "=" + TBL_JOURNEYS + "." + COL_ENDSTAYPOINT + " WHERE " + COL_STARTSTAYPOINT + "=" + stayPointID + " OR " + COL_ENDSTAYPOINT + "=" + stayPointID, null);
        ArrayList<StayPoint> points = new ArrayList<>();
        while (results.moveToNext()){
            double rowid = results.getDouble(0);
            double lat = results.getDouble(1);
            double lon = results.getDouble(2);
            points.add(new StayPoint(rowid,lat, lon));
        }
        results.close();
        return points;
    }

    public String getLatestVisitDateTime(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT MAX(" + COL_START + ") FROM " + TBL_STAYPOINT_VISITS, null);
        String dateTime = null;
        while (results.moveToNext()){
            dateTime = results.getString(0);
        }
        results.close();
        return dateTime;
    }

    public String getLatestJourneyDateTime(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT MAX(" + COL_START + ") FROM " + TBL_JOURNEYS, null);
        String dateTime  = null;
        while (results.moveToNext()){
            dateTime = results.getString(0);
        }
        results.close();
        return dateTime;
    }

    public ArrayList<StayPointVisit> getStayPointVisitsFor(double rowid){
        ArrayList<StayPointVisit> uniqueDates = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT * FROM " + TBL_STAYPOINT_VISITS + " WHERE StayPointId = " + rowid + " ORDER BY " + COL_START, null);
        while (results.moveToNext()){
            String start = results.getString(1);
            String end = results.getString(2);
            uniqueDates.add(new StayPointVisit(rowid, start, end));
        }
        results.close();
        return uniqueDates;
    }

    public ArrayList<String> getUpdateData(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT * FROM " + TBL_UPDATES, null);
        ArrayList<String> updates = new ArrayList<>();
        while (results.moveToNext()){
            long startTime = results.getLong(0);
            long endTime = results.getLong(1);
            GregorianCalendar gregStart = new GregorianCalendar();
            gregStart.setTimeInMillis(startTime);

            GregorianCalendar gregEnd = new GregorianCalendar();
            gregEnd.setTimeInMillis(endTime);

            updates.add(Utils.getDateTimeReadable(gregStart) +  ", " + Utils.getDateTimeReadable(gregEnd));
        }
        results.close();
        return updates;
    }

    public ArrayList<String> getLoggerData(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT * FROM " + TBL_LOGGER_STATUS + " ORDER BY " + COL_LATEST_POINT, null);
        ArrayList<String> loggerStatus = new ArrayList<>();
        while (results.moveToNext()){
            double dbSize = results.getDouble(0);
            int gpsPoints = results.getInt(1);
            int stayPoints = results.getInt(2);
            int visits = results.getInt(3);
            int journeys = results.getInt(4);
            String latestPoint = results.getString(5);
            String latestStayUpdate = results.getString(7);
            String latestJourneyUpdate = results.getString(8);

            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMaximumFractionDigits(2);
            loggerStatus.add(numberFormat.format(dbSize) +  " MB ," + gpsPoints + ", " + stayPoints + ", " + visits + ", " + journeys +
                    ", " + Utils.getDateTimeReadable(latestPoint) + ", " + Utils.getDateTimeReadable(latestStayUpdate) + ", " + Utils.getDateTimeReadable(latestJourneyUpdate));
        }
        results.close();
        return loggerStatus;
    }


    public int getCountFor(String tableName){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        int recordCount = 0;
        while (results.moveToNext()){
            recordCount = results.getInt(0);
        }
        results.close();
        return recordCount;
    }

    public void clearLocalData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TBL_GPSPOINTS);
        db.execSQL("DELETE FROM " + TBL_STAYPOINTS);
        db.execSQL("DELETE FROM " + TBL_STAYPOINT_VISITS);
        db.execSQL("DELETE FROM " + TBL_JOURNEYS);
        db.execSQL("DELETE FROM " + TBL_UPDATES);
        db.execSQL("DELETE FROM " + TBL_LOGGER_STATUS);
    }

}
