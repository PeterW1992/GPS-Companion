package dissertation.GPSCompanionApp.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Peter on 14/01/2017.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static int VERSION = 2;
    private static String DATABASE_NAME = "GPSLogger";

    public static String TBL_GPSPOINTS = "tblGPSPoints", TBL_STAYPOINTS = "tblStayPoints", TBL_STAYPOINT_VISITS = "tblStayPointVisits",
            TBL_JOURNEYS = "tblJourneys", TBL_UPDATES = "tblUpdates", TBL_LOGGER_STATUS = "tblLoggerStatus";

    private String COL_ROWID = "rowid", COL_LAT = "Lat", COL_LON = "Lon", COL_DATETIME = "DateTime", COL_ALT = "Alt", COL_SPEED = "Speed",
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
            db.beginTransaction();
            for (StayPoint point : points){
                contentValues = new ContentValues();
                contentValues.put(COL_ROWID, point.get_ROW_ID());
                contentValues.put(COL_LAT,point.get_LAT());
                contentValues.put(COL_LON,point.get_LON());
            }
            db.endTransaction();
        }
    }

    public void addStayPointVisits(ArrayList<StayPointVisit> points){
        ContentValues contentValues;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (StayPointVisit point : points){
            contentValues = new ContentValues();
            contentValues.put(COL_STAYPOINTID, point.get_STAYPOINT_ID());
            contentValues.put(COL_START,point.get_START());
            contentValues.put(COL_END,point.get_END());
        }
        db.endTransaction();
    }

    public void addJourneys(ArrayList<Journey> journeys){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues;
        db.beginTransaction();
        for (Journey journey : journeys){
            contentValues = new ContentValues();
            contentValues.put(COL_ROWID, journey.getRowid());
            contentValues.put(COL_STARTSTAYPOINT, journey.getStartPoint());
            contentValues.put(COL_ENDSTAYPOINT, journey.getEndPoint());
            contentValues.put(COL_START, journey.getStartDateTime());
            contentValues.put(COL_END, journey.getEndDateTime());
            db.insertWithOnConflict(TBL_JOURNEYS,null,contentValues,SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.endTransaction();
    }

    public void addJourneyPoints(ArrayList<GPSPoint> gpsPoints){
        ContentValues contentValues;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
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
        db.endTransaction();
    }

    public void addUpdate(long start, long end){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.execSQL("INSERT INTO " + TBL_UPDATES + "(" + COL_UPDATE_STARTTIME + ", " + COL_UPDATE_ENDTIME + ") VALUES (" + start + ", " + end + ")");
        db.endTransaction();
    }

    public void addSummaryData(ArrayList<String> data){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
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
        }
        db.beginTransaction();
    }

    public Long getLatestUpdate(){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_UPDATE_STARTTIME + "), " + COL_UPDATE_ENDTIME + " FROM " + TBL_UPDATES, null);
        cursor.moveToNext();
        Long startTime = cursor.getLong(0);
        cursor.close();
        db.endTransaction();
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

    public HashMap<Double, ArrayList<GPSPoint>> getJourneyPoints(Set<Double> ids){
        HashMap<Double, ArrayList<GPSPoint>> journeys = new HashMap<>();
        for (Double id : ids){
            ArrayList<GPSPoint> gpsPoints = new ArrayList<>();
            String query = "SELECT " + COL_LAT + ", " + COL_LON  + " FROM " + TBL_GPSPOINTS +
                    " WHERE " + COL_JOURNEYID  + " = " + id + "  ORDER BY " + COL_DATETIME;
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(query,null);
            while (cursor.moveToNext()){
                double lat = cursor.getDouble(0);
                double lon = cursor.getDouble(1);

                gpsPoints.add(new GPSPoint(lat,lon));
            }

            journeys.put(id, gpsPoints);
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

    public String getLatestDateTime(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT MAX(" + COL_DATETIME + ") FROM " + TBL_GPSPOINTS, null);
        String dateTime  = null;
        while (results.moveToNext()){
            dateTime = results.getString(0);
        }
        results.close();
        return dateTime;
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
            uniqueDates.add( new StayPointVisit(rowid, start, end));
        }
        results.close();
        return uniqueDates;
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
    }

}