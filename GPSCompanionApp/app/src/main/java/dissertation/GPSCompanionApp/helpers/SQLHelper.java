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
public class SQLHelper extends SQLiteOpenHelper {
    private static int VERSION = 25;
    private static String DATABASE_NAME = "GPSPoints";
    public static String TBL_GPSPOINTS = "tblGPSPoints", TBL_STAYPOINTS = "tblStayPoints", TBL_STAYPOINT_VISITS = "tblStayPointVisits",
            TBL_JOURNEYS = "tblJourneys";
    private String COL_LAT = "Lat";
    private String COL_LON = "Lon";
    private String COL_DATETIME = "DateTime";
    private String COL_ALT = "Alt";
    private String COL_SPEED = "Speed";
    private String COL_MODE = "Mode";
    private String COL_TRACK = "Track";
    private String PKSTAYPOINTS = " PRIMARY KEY(Lat, Lon))";

    private String COL_STAYPOINTID = "StayPointID";
    private String COL_START = "StartDateTime";
    private String COL_END = "EndDateTime";
    private String COL_STARTSTAYPOINT = "StartStayPoint";
    private String COL_ENDSTAYPOINT = "EndStayPoint";
    private String FK_CONSTRAINT = " PRIMARY KEY(" + COL_START + ") FOREIGN KEY (" + COL_STAYPOINTID + ") REFERENCES " + TBL_STAYPOINTS + "(ROWID))";

    private String COL_JOURNEYID = "JourneyId";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME,null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBL_GPSPOINTS + " (" +
                COL_LAT + " REAL, " + COL_LON + " REAL, " +
                COL_DATETIME + " TEXT PRIMARY KEY, " +
                COL_ALT + " REAL, " + COL_SPEED + " REAL, " +
                COL_MODE + " INTEGER, " + COL_TRACK + " REAL, " + COL_JOURNEYID + " INTEGER, FOREIGN KEY (" + COL_JOURNEYID + ") REFERENCES " +  TBL_JOURNEYS + "(rowid))");

        db.execSQL("CREATE TABLE " + TBL_STAYPOINTS + " (" +
                COL_LAT + " REAL, " + COL_LON + " REAL , " + PKSTAYPOINTS);

        db.execSQL("CREATE TABLE " + TBL_STAYPOINT_VISITS + " (" +
                COL_STAYPOINTID + " INTEGER, "+ COL_START + " TEXT, " + COL_END + " TEXT," + FK_CONSTRAINT);

        db.execSQL("CREATE TABLE " + TBL_JOURNEYS + "(" + COL_STARTSTAYPOINT + " INTEGER," + COL_ENDSTAYPOINT + " INTEGER, " + COL_START + " TEXT, " + COL_END + " TEXT," +
                " PRIMARY KEY (" + COL_START + ") FOREIGN KEY (" + COL_STARTSTAYPOINT + ") REFERENCES " + TBL_STAYPOINTS +
                "(ROWID) FOREIGN KEY (" + COL_ENDSTAYPOINT + ") REFERENCES " + TBL_STAYPOINTS + "(ROWID))" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_GPSPOINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_STAYPOINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_STAYPOINT_VISITS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_JOURNEYS);
        onCreate(db);
    }

    public void addStayPoints(ArrayList<StayPoint> points){
        if (points != null){
            ContentValues contentValues;
            SQLiteDatabase db = getWritableDatabase();
            long ptsAdded = 0;
            for (StayPoint point : points){
                contentValues = new ContentValues();
                contentValues.put("rowid", point.get_ROW_ID());
                contentValues.put("Lat",point.get_LAT());
                contentValues.put("Lon",point.get_LON());
                if (db.insertWithOnConflict(TBL_STAYPOINTS,null,contentValues,SQLiteDatabase.CONFLICT_IGNORE) > 0){
                    ptsAdded += 1;
                }
        }
            System.out.println(ptsAdded + " Stay points added to db");
        }
    }

    public void addStayPointVisits(ArrayList<StayPointVisit> points){
        ContentValues contentValues;
        SQLiteDatabase db = getWritableDatabase();
        long ptsAdded = 0;
        for (StayPointVisit point : points){
            contentValues = new ContentValues();
            contentValues.put("StayPointID", point.get_STAYPOINT_ID());
            contentValues.put(COL_START,point.get_START());
            contentValues.put(COL_END,point.get_END());
            if (db.insertWithOnConflict(TBL_STAYPOINT_VISITS,null,contentValues,SQLiteDatabase.CONFLICT_IGNORE) > 0){
                ptsAdded += 1;
            }
        }
        System.out.println(ptsAdded + " Stay point visits added to db");
    }

    public void addJourneys(ArrayList<Journey> journeys){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues;
        for (Journey journey : journeys){
            contentValues = new ContentValues();
            contentValues.put("rowid", journey.getRowid());
            contentValues.put(COL_STARTSTAYPOINT, journey.getStartPoint());
            contentValues.put(COL_ENDSTAYPOINT, journey.getEndPoint());
            contentValues.put(COL_START, journey.getStartDateTime());
            contentValues.put(COL_END, journey.getEndDateTime());
            db.insertWithOnConflict(TBL_JOURNEYS,null,contentValues,SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void addJourneyPoints(ArrayList<GPSPoint> gpsPoints){
        ContentValues contentValues;
        SQLiteDatabase db = getWritableDatabase();
        for (GPSPoint point : gpsPoints){
            contentValues = new ContentValues();
            contentValues.put("rowid", point.get_ROWID());
            contentValues.put("Lat",point.get_LAT());
            contentValues.put("Lon",point.get_LON());
            contentValues.put("dateTime", point.get_dateTime());
            contentValues.put("Alt",point.get_ALT());
            contentValues.put("Speed",point.get_SPEED());
            contentValues.put("Mode",point.get_MODE());
            contentValues.put("Track",point.get_TRACK());
            contentValues.put("JourneyID", point.get_JOURNEYID());
            db.insertWithOnConflict(TBL_GPSPOINTS , null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
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
        ArrayList<Journey> journeys = new ArrayList<Journey>();
        String whereClause = "";
        for (int i = 0; i < dates.size(); i++){
            whereClause = whereClause + " " + COL_START +  " LIKE '" + dates.get(i) + "%' OR";
        }
        whereClause = whereClause.substring(0, whereClause.length() - 3);
        String query = "SELECT DISTINCT(" + TBL_JOURNEYS + ".rowid)," +  TBL_JOURNEYS + ".* FROM " + TBL_JOURNEYS + " INNER JOIN " +
                TBL_GPSPOINTS + " ON " + TBL_JOURNEYS + ".rowid = " + TBL_GPSPOINTS + "." + COL_JOURNEYID + " WHERE " + whereClause + " ORDER BY " + COL_START;
        System.out.println(query);
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

    public ArrayList<Double> getAllLocalJourneyIds(){
        ArrayList<Double> journeyIds = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor results = db.rawQuery("SELECT DISTINCT(" + TBL_JOURNEYS + ".rowid) FROM " + TBL_JOURNEYS + " INNER JOIN " + TBL_GPSPOINTS + " ON " +
                TBL_JOURNEYS + ".rowid = " + TBL_GPSPOINTS + "." + COL_JOURNEYID, null);
        while (results.moveToNext()){
            double rowId = results.getDouble(0);
            journeyIds.add(rowId);
        }
        results.close();
        return journeyIds;
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
                    " WHERE "+ COL_STARTSTAYPOINT  + " = " + id1 + " AND " + COL_ENDSTAYPOINT +"=" + id2 +"  ORDER BY " + COL_START;
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
    }
}
