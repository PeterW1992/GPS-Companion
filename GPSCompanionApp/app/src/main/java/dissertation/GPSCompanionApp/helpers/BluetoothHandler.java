package dissertation.GPSCompanionApp.helpers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/*
 * Created by Peter on 14/01/2017.
 */
public class BluetoothHandler {
    BluetoothDataClient returnTo;
    BluetoothDevice raspberryPi;
    BluetoothSocket mmSocket;
    InputStream inputStream;
    OutputStream outputStream;
    UUID uuid;
    Constants.RequestType requestType;
    public static Long transferStartTime, transferEndTime, processStartTime, processEndTime;

    public BluetoothHandler(BluetoothDataClient sender, BluetoothDevice raspberryPi){
        returnTo = sender;
        this.raspberryPi = raspberryPi;
        uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    }

    private boolean connectToPi(){
        try {
            mmSocket = raspberryPi.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            if (mmSocket.isConnected()){
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            PostBackError postBackError = new PostBackError();
            postBackError.execute("Error in connecting to pi: " + e);
            e.printStackTrace();
            return false;
        }
    }

    public void retrieveDataAfter(String visitDate, String journeyDate){
        requestType = Constants.RequestType.RETRIEVE_ALL_DATA;
        BluetoothThread bluetoothThread = new BluetoothThread();
        String[] message = new String[1];
        String[] params = new String[2];
        params[0] = visitDate;
        params[1] = journeyDate;
        JSONObject jsonObject = createJSONCommand("getDataAfter", params);
        message[0] = jsonObject.toString();
        transferStartTime = System.currentTimeMillis();
        bluetoothThread.execute(message);
    }

    public void retrieveSettings(){
        requestType = Constants.RequestType.RETRIEVE_SETTINGS;
        BluetoothThread bluetoothThread = new BluetoothThread();
        String[] message = new String[1];
        JSONObject jsonObject = createJSONCommand("getSettings", new String[0]);
        message[0] = jsonObject.toString();
        bluetoothThread.execute(message);
    }

    public void submitSettings(String[] settings){
        requestType = Constants.RequestType.UPDATE_SETTINGS;
        BluetoothThread bluetoothThread = new BluetoothThread();
        String[] message = new String[1];
        String[] params = settings;

        JSONObject jsonObject = createJSONCommand("updateSettings", params);
        message[0] = jsonObject.toString();
        bluetoothThread.execute(message);
    }

    private JSONObject createJSONCommand(String command, String[] params){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cmd",command);
            for (int i = 0; i < params.length; i++){
                jsonObject.put("p" + (i + 1),params[i]);
            }
        } catch (Exception e){

        }
        return jsonObject;
    }

    private void processResponse(String output){

        switch (requestType) {

            case RETRIEVE_ALL_DATA:
                processAllData(output);
                break;

            case RETRIEVE_SETTINGS:
                processSettings(output);
                break;

            case UPDATE_SETTINGS:
                returnTo.returnData(requestType, true);
                break;
        }
    }

    private void processAllData(String output){
        processStartTime = System.currentTimeMillis();
        ArrayList<StayPoint> stayPoints = new ArrayList<>();
        ArrayList<StayPointVisit> stayPointVisits = new ArrayList<>();
        ArrayList<Journey> journeys = new ArrayList<>();
        ArrayList<GPSPoint> gpsPoints = new ArrayList<>();
        ArrayList<String> loggerStatus = new ArrayList<>();

        HashMap<String,Object> data = new HashMap<>();

        try {
            JsonReader jsonReader = new JsonReader(new StringReader(output));
            jsonReader.setLenient(true);
            jsonReader.beginObject();
            String name;
            while (jsonReader.hasNext()){
                name = jsonReader.nextName();
                switch (name){
                    case "stayVisits":
                        stayPointVisits = processStayVisits(jsonReader);
                        break;

                    case "stayPoints":
                        stayPoints = processStayPoints(jsonReader);
                        break;

                    case "journeys":
                        journeys = processJourneys(jsonReader);
                        break;

                    case "journeyPoints":
                        gpsPoints = processJourneyPoints(jsonReader);
                        break;

                    case "loggerStatus":
                        loggerStatus = processSummary(jsonReader);
                }
            }
        } catch (Exception exception){
            System.out.println(exception);
        }
        data.put("StayPoints", stayPoints);
        data.put("StayVisits", stayPointVisits);
        data.put("Journeys", journeys);
        data.put("JourneyPoints", gpsPoints);
        data.put("LoggerStatus", loggerStatus);
        processEndTime = System.currentTimeMillis();
        returnTo.returnData(requestType, data);
    }

    private ArrayList<StayPoint> processStayPoints(JsonReader jsonReader){
        ArrayList<StayPoint> stayPoints = new ArrayList<>();
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext() && !(jsonReader.peek() == JsonToken.END_ARRAY)){
                jsonReader.beginArray();

                double rowid = -1;
                double lat = -1, lon = -1;
                JsonToken type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    rowid = jsonReader.nextDouble();
                }
                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    lat = jsonReader.nextDouble();
                }
                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    lon = jsonReader.nextDouble();
                }
                jsonReader.endArray();
                stayPoints.add(new StayPoint(rowid,lat,lon));
            }
            jsonReader.endArray();
        } catch (Exception e){
            System.out.println(e);

    }
        return stayPoints;
    }

    private ArrayList<StayPointVisit> processStayVisits(JsonReader jsonReader){
        ArrayList<StayPointVisit> stayPointVisits = new ArrayList<>();
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext() && !(jsonReader.peek() == JsonToken.END_ARRAY)) {
                jsonReader.beginArray();

                double rowid = -1;

                String startDateTime = null, endDateTime = null;

                JsonToken type = jsonReader.peek();
                if (type == JsonToken.NUMBER) {
                    rowid = jsonReader.nextDouble();
                }
                type = jsonReader.peek();
                if (type == JsonToken.STRING) {
                    startDateTime = jsonReader.nextString();
                }
                type = jsonReader.peek();
                if (type == JsonToken.STRING) {
                    endDateTime = jsonReader.nextString();
                }
                jsonReader.endArray();
                stayPointVisits.add(new StayPointVisit(rowid, startDateTime, endDateTime));
            }
            jsonReader.endArray();
        } catch (Exception e){
            System.out.println(e);
        }
        return stayPointVisits;
    }

    private ArrayList<Journey> processJourneys(JsonReader jsonReader){
        ArrayList<Journey> journeys = new ArrayList<>();
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext() && !(jsonReader.peek() == JsonToken.END_ARRAY)){
                jsonReader.beginArray();

                double rowid = -1, startId = -1, endId = -1;
                String startDateTime = null, endDateTime = null;

                JsonToken type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    rowid = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    startId = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    endId = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.STRING){
                    startDateTime = jsonReader.nextString();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.STRING){
                    endDateTime = jsonReader.nextString();
                } else {
                    jsonReader.skipValue();
                }
                jsonReader.endArray();
                journeys.add(new Journey(rowid,startId, endId, startDateTime,endDateTime));
            }
            jsonReader.endArray();
        } catch (Exception e){
            System.out.println(e);
        }
        return journeys;
    }

    private ArrayList<GPSPoint> processJourneyPoints(JsonReader jsonReader){
        ArrayList<GPSPoint> gpsPoints = new ArrayList<>();
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Double rowid = null, lat = null, lon = null, alt = null, speed = null, track = null, journeyId = null;
                Integer mode = null;
                String dateTime = null;
                jsonReader.beginArray();

                JsonToken type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    rowid = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    lat = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    lon = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.STRING){
                    dateTime = jsonReader.nextString();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    alt = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    speed = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    mode = jsonReader.nextInt();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    track = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    journeyId = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }

                gpsPoints.add(new GPSPoint(rowid, lat, lon, dateTime, alt, speed, mode, track, journeyId));
                jsonReader.endArray();
            }
            jsonReader.endArray();
        } catch (Exception e ){
            System.out.println(e);
        }
        return gpsPoints;
    }

    private ArrayList<String> processSummary(JsonReader jsonReader){
        ArrayList<String> summaryData = new ArrayList<>();

        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                jsonReader.beginArray();
                String data = "-";
                if (jsonReader.peek() == JsonToken.STRING) {
                    data = jsonReader.nextString();
                } else {
                    jsonReader.skipValue();
                }
                summaryData.add(data);
                jsonReader.endArray();
            }
            jsonReader.endArray();
        } catch (Exception e ){
            System.out.println("Error in processSummary: " + e);
        }
        return summaryData;
    }

    private void processSettings(String output){
        ArrayList<String> settings = new ArrayList<>();
        System.out.println(output);
        try {
            JsonReader jsonReader = new JsonReader(new StringReader(output));
            jsonReader.setLenient(true);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Double data = null;

                jsonReader.beginArray();

                JsonToken type = jsonReader.peek();
                if (type == JsonToken.NUMBER){
                    data = jsonReader.nextDouble();
                } else {
                    jsonReader.skipValue();
                }
                jsonReader.endArray();
                settings.add(Double.toString(data));
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
        returnTo.returnData(Constants.RequestType.RETRIEVE_SETTINGS, settings);
    }

    private void returnError(String error){
        returnTo.returnError(error);
    }

    public class BluetoothThread extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String ... params) {
            String returnStr = null;
            String command = params[0];
            System.out.println(command);
            if (connectToPi()) {
                ByteArrayOutputStream buffer;
                try {
                    byte[] messageBtyes = command.getBytes();
                    outputStream = mmSocket.getOutputStream();
                    outputStream.write(messageBtyes);
                    inputStream = mmSocket.getInputStream();
                    InputStream in = inputStream;
                    buffer = new ByteArrayOutputStream();
                    int read;
                    byte[] input = new byte[4096];
                    while (33 != (read = in.read(input))) {
                        buffer.write(input, 0, read);
                        String output = buffer.toString();
                        if (output.endsWith("EndOfTransmission")) {
                            returnStr = buffer.toString();
                            mmSocket.close();
                            inputStream.close();
                            outputStream.close();
                            break;
                        }
                    }
                    returnStr = buffer.toString();

                } catch (IOException e) {
                    System.out.println("Error in doInBackground: " + e);
                }
            }
            return returnStr;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                transferEndTime = System.currentTimeMillis();
                processResponse(result);
            } else {
                returnError("Null result");
            }
        }
    }

    public class PostBackError extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String ... params) {
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            returnError(result);
        }
    }
}
