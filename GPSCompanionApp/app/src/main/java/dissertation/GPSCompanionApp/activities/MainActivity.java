package dissertation.GPSCompanionApp.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import dissertation.GPSCompanionApp.*;
import dissertation.GPSCompanionApp.helpers.*;
import dissertation.GPSCompanionApp.adapters.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, BluetoothDataClient {

    /* Global */
    DatabaseHandler databaseHandler;
    HashMap<Double, ArrayList<GPSPoint>> gpsPoints;
    ArrayList<StayPoint> stayPoints;
    HashMap<LatLng, Double> stayPointMap;
    HashMap<String,BluetoothDevice> foundDevices;
    ArrayList<Double> journeySelectIDs = new ArrayList<>();
    SharedPreferences sharedPreferences;
    BluetoothAdapter mBluetoothAdapter;
    Dialog dialog;

    public boolean journeySelectMode = false;
    Long updateStartTime, updateEndTime;

    int REQUEST_ENABLE_BT = 1;
    MenuItem toolbarMenu;

    NavigationView navigationView;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sharedPreferences = getSharedPreferences("prefs",MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(this);

        navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showChooseDialog();
            }
        });
        loadData();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
        }

        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if(permissionGranted) {
            // {Some Code}
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, bluetoothFilter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        toolbarMenu = item;
        switch (id) {
            case R.id.action_settings:
                Intent i = new Intent(this, Settings.class);
                startActivity(i);
                return true;

            case R.id.btn_journeySelectMode:
                journeySelectIDs = new ArrayList<>();
                toggleJourneyMode();
                break;

            case R.id.acbtn_selectByDate:
                if (dialog != null)
                    dialog.dismiss();
                View dateView = getLayoutInflater().inflate(R.layout.dialog_journey_select_by_date, null);
                final ArrayList<String> datesToShow = new ArrayList<>();
                final ListView lstDates = (ListView) dateView.findViewById(R.id.lst_localDates);
                final ArrayAdapter lstAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

                lstDates.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
                lstAdapter.addAll(databaseHandler.getJourneyDates());
                lstDates.setAdapter(lstAdapter);
                Button btnCloseDialog = (Button) dateView.findViewById(R.id.btn_closeDateDialog);
                btnCloseDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                Button btnShowJourneys = (Button) dateView.findViewById(R.id.btn_viewDates);
                btnShowJourneys.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ArrayList<Journey> journeys = databaseHandler.getJourneysForDates(datesToShow);

                        Set<Double> journeyIDs = new HashSet<>();

                        for (Journey aJourney : journeys){
                            journeyIDs.add(aJourney.getRowid());
                        }
                        gpsPoints = databaseHandler.getJourneyPoints(journeyIDs);
                        loadData();
                    }
                });

                lstDates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String date = (String) lstDates.getItemAtPosition(position);
                        if (datesToShow.contains(date)) {
                            datesToShow.remove(date);
                            view.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
                        } else {
                            view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            datesToShow.add(date);
                        }
                        System.out.println(date);
                    }
                });
                dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dateView);
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.btn_retrieveLatestData:
                retrieveData();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        if (gpsPoints != null && gpsPoints.size() > 0){
            addPointsToMap(googleMap);
        }
        if (stayPoints != null  && stayPoints.size() > 0){
            addStayPointsToMap(googleMap);
        }

        googleMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                double rowID = stayPointMap.get(circle.getCenter());
                if (journeySelectMode){
                    journeySelectIDs.add(rowID);
                    if (journeySelectIDs.size() == 2){
                        showJourneySelectResult(true);
                    }
                } else {
                    showStayPointInfo(rowID);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // {Some Code}
                }
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                foundDevices.put(deviceName, device);
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showChooseDialog();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void loadData(){
        stayPointMap = new HashMap<>();
        stayPoints = databaseHandler.getStayPoints();

        long lastUpdate = databaseHandler.getLatestUpdate();

        TextView lblLatestDate = (TextView) navigationView.getHeaderView(0).findViewById(R.id.lbl_latestUpdateDate);
        int gpsPointCount = databaseHandler.getCountFor(DatabaseHandler.TBL_GPSPOINTS);
        int stayPointCount = databaseHandler.getCountFor(DatabaseHandler.TBL_STAYPOINTS);
        int stayPointVisitCount =  databaseHandler.getCountFor(DatabaseHandler.TBL_STAYPOINT_VISITS);
        int journeyCount = databaseHandler.getCountFor(DatabaseHandler.TBL_JOURNEYS);

        Menu menu = navigationView.getMenu();
        menu.getItem(1).setTitle("Stay Points: " + stayPointCount);
        menu.getItem(2).setTitle("Stay Point Visits: " + stayPointVisitCount);
        menu.getItem(3).setTitle("Journeys: " + journeyCount);
        menu.getItem(4).setTitle("Total Points: " + gpsPointCount);

        if (lastUpdate > 0){
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeInMillis(lastUpdate);

            lblLatestDate.setText("Latest update: " + Utils.getDateReadable(gregorianCalendar));
        }
        updateMap();
    }

    private void retrieveData(){
        BluetoothDevice raspberryPi;
        String latestVisit = databaseHandler.getLatestVisitDateTime();
        String latestJourney = databaseHandler.getLatestJourneyDateTime();

        if (latestVisit == null){
            latestVisit = "";
        }
        if (latestJourney == null){
            latestJourney = "";
        }
        raspberryPi = getDevice();
        if (raspberryPi != null){
            updateStartTime = System.currentTimeMillis();
            BluetoothHandler bluetoothHandler = new BluetoothHandler(this,raspberryPi);
            bluetoothHandler.retrieveDataAfter(latestVisit, latestJourney);
            showLoadingDialog();
        } else {
            Toast.makeText(this, "A Device has not been selected", Toast.LENGTH_LONG).show();
        }
    }

    public BluetoothDevice getDevice(){
        SharedPreferences sharedPreferences = getSharedPreferences("prefs",MODE_PRIVATE);
        String deviceAddress = sharedPreferences.getString("choosenDevice", null);
        BluetoothDevice mmDevice = null;
        if (deviceAddress != null){
            mmDevice =  mBluetoothAdapter.getRemoteDevice(deviceAddress);
        }
        return mmDevice;
    }

    private void discoverDevices(){
        foundDevices = new HashMap<>();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    private void addPointsToMap(GoogleMap googleMap){
        for (Double id : gpsPoints.keySet()){
            ArrayList<LatLng> latLngs = new ArrayList<>();
            ArrayList<GPSPoint> gpsPointsForID = gpsPoints.get(id);
            int i = 0;
            while (i < gpsPointsForID.size()){
                latLngs.add(gpsPointsForID.get(i).getLatLng());
                i++;
            }
            if (latLngs.size() > 0){
                int middleIndex = latLngs.size() / 2;
                LatLng middlePoint = latLngs.get(middleIndex);
                googleMap.addPolyline(new PolylineOptions().addAll(latLngs).zIndex(5));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(middlePoint,10));
            }
        }
    }

    private void addStayPointsToMap(GoogleMap googleMap){
        int i = 0;
        StayPoint point;
        double avgLat = 0;
        double avgLon = 0;
        while (i < stayPoints.size()){
            point = stayPoints.get(i);
            avgLat += point.get_LAT();
            avgLon += point.get_LON();
            int strokeColour = getResources().getColor(R.color.mediumGreen);
            int fillColour = getResources().getColor(R.color.lightGrey);
            stayPointMap.put(point.getLatLng(), point.get_ROW_ID());
            googleMap.addCircle( new CircleOptions().center(point.getLatLng()).radius(200).strokeColor(strokeColour).fillColor(fillColour).clickable(true).zIndex(10));
            i++;
        }
        avgLat = avgLat / stayPoints.size();
        avgLon = avgLon / stayPoints.size();
        LatLng centre = new LatLng(avgLat, avgLon);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centre, 10));
    }

    private void updateMap(){
        mapFragment.getMapAsync(this);
    }

    private void showLoadingDialog(){
        if (dialog != null)
            dialog.dismiss();
        View loadingDialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(loadingDialogView);
        dialog.show();
    }

    private void showChooseDialog(){
        if (dialog != null)
            dialog.dismiss();

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_device_choose, null);
        final ListView lstDeviceChoice = (ListView) dialogView.findViewById(R.id.lst_bluetoothDevices);
        final HashSet<BluetoothDevice> devices = new HashSet<>(mBluetoothAdapter.getBondedDevices());
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1);
        for (BluetoothDevice device : devices){
            arrayAdapter.add(device.getName());
        }
        if (foundDevices != null){
            arrayAdapter.addAll(foundDevices.keySet());
        }
        lstDeviceChoice.setAdapter(arrayAdapter);
        lstDeviceChoice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String deviceName = lstDeviceChoice.getItemAtPosition(position).toString();
                BluetoothDevice device = foundDevices.get(deviceName);
                if (device.getBondState() == BluetoothDevice.BOND_NONE){
                    if (device.createBond()){
                        editor.putString("choosenDevice", device.getAddress());
                        editor.commit();
                        dialog.dismiss();
                    }
                } else {
                    editor.putString("choosenDevice", device.getAddress());
                    editor.commit();
                    dialog.dismiss();
                }

            }
        });
        Button btnDiscoverDevices = (Button) dialogView.findViewById(R.id.btn_discoverDevices);
        btnDiscoverDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices();
                dialog.dismiss();
            }
        });

        dialog = new Dialog(this);
        dialog.setTitle("Select Device");
        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showStayPointInfo(double rowID){
        if (dialog != null)
            dialog.dismiss();
        ArrayList<StayPointVisit> visits =  databaseHandler.getStayPointVisitsFor(rowID);
        View dialogView = this.getLayoutInflater().inflate(R.layout.dialog_stay_point_info, null);

        TextView txtHeader = (TextView) dialogView.findViewById(R.id.lbl_dialogHeader);
        txtHeader.setText("Visits Info");

        TextView txtVisitAmount = (TextView) dialogView.findViewById(R.id.lbl_visitAmountValue);
        txtVisitAmount.setText(visits.size() + "");

        TextView txtFirstVisit = (TextView) dialogView.findViewById(R.id.lbl_firstVisitValue);
        TextView txtLastVisit = (TextView) dialogView.findViewById(R.id.lbl_lastVisitValue);
        TextView txtTotalDuration = (TextView) dialogView.findViewById(R.id.lbl_totalTimeSpentValue);
        TextView txtAverageVisit = (TextView) dialogView.findViewById(R.id.lbl_averageVisitValue);

        Button btnCloseDialog = (Button) dialogView.findViewById(R.id.btn_closeStayInfoDialog);

        long totalDuration = 0;
        for (StayPointVisit visit : visits){
            totalDuration += Utils.getGregDateTimeFrom(visit.get_END()).getTimeInMillis() - Utils.getGregDateTimeFrom(visit.get_START()).getTimeInMillis();
        }

        String firstVisitDT = visits.get(0).get_START();
        String endVisitDT = visits.get(visits.size() - 1).get_START();

        txtFirstVisit.setText(Utils.getDateReadable(firstVisitDT.substring(0, firstVisitDT.indexOf("T"))));
        txtLastVisit.setText(Utils.getDateReadable(endVisitDT.substring(0, endVisitDT.indexOf("T"))));
        txtTotalDuration.setText(Utils.getDurationFormat(totalDuration));
        String averageTime = Utils.getDurationFormat(totalDuration / visits.size());
        txtAverageVisit.setText(averageTime);

        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ExpandableListView exp_visits = (ExpandableListView) dialogView.findViewById(R.id.exp_visits);
        ExpandListViewSingleChildStayPointVisits adapter = new ExpandListViewSingleChildStayPointVisits(this, visits);
        exp_visits.setAdapter(adapter);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showJourneySelectResult(final boolean uniDirection){
        if (dialog != null)
            dialog.dismiss();

        double location1 = journeySelectIDs.get(0);
        double location2 = journeySelectIDs.get(1);

        final ArrayList<Journey> journeys = databaseHandler.getLocalJourneysBetween(location1, location2, uniDirection);

        if (journeys.size() > 0) {
            dialog = new Dialog(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_journey_select_info, null);
            TextView txtHeader = (TextView) dialogView.findViewById(R.id.lbl_dialogHeader);
            txtHeader.setText(getResources().getString(R.string.lbl_selectResultHeader));
            final ExpandableListView listView = (ExpandableListView) dialogView.findViewById(R.id.exp_journeys);

            TextView txtTotalJourneys = (TextView) dialogView.findViewById(R.id.lbl_journeyAmountValue);
            TextView txtTotalTime = (TextView) dialogView.findViewById(R.id.lbl_journeysTotalTimeValue);
            TextView txtLastJourney = (TextView) dialogView.findViewById(R.id.lbl_lastJourneyValue);
            TextView txtAverageTime = (TextView) dialogView.findViewById(R.id.lbl_averageJourneyTimeValue);

            String lastJourney = "";

            long averageTime = 0;
            long fastestTime = 0;
            long time = 0;
            Set<Double> journeyIDs = new HashSet<>();
            for (int i = 0; i < journeys.size(); i++) {
                Journey aJourney = journeys.get(i);
                if (i == journeys.size() - 1) {
                    lastJourney = aJourney.getStartDateTime();
                }
                long diff = aJourney.getDuration();
                if (fastestTime == 0 || diff < fastestTime) {
                    fastestTime = diff;
                }
                time += diff;

                journeyIDs.add(aJourney.getRowid());
            }

            if (time != 0 && journeys.size() != 0) {
                averageTime = time / journeys.size();
            }
            gpsPoints = databaseHandler.getJourneyPoints(journeyIDs);
            loadData();
            ExpandListViewSingleChildJourneys adapter = new ExpandListViewSingleChildJourneys(this, journeys, fastestTime);

            txtTotalJourneys.setText(journeys.size() + "");
            txtTotalTime.setText(Utils.getDurationFormat(time));
            txtLastJourney.setText(Utils.getDateReadable(lastJourney));
            txtAverageTime.setText(Utils.getDurationFormat(averageTime));
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    toggleJourneyMode();
                }
            });

            Button btnCloseDialog = (Button) dialogView.findViewById(R.id.btn_closeJourneyInfoDialog);
            btnCloseDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            Button btnToggleDirection = (Button) dialogView.findViewById(R.id.btn_toggleJourneyDirection);
            if (uniDirection){
                btnToggleDirection.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.journey_direction_both_ways),null,null,null);
                btnToggleDirection.setText(getString(R.string.btn_journeyDirectionBothWay));
            } else {
                btnToggleDirection.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.journey_direction_one_way),null,null,null);
                btnToggleDirection.setText(getString(R.string.btn_journeyDirectionOneWay));
            }
            btnToggleDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showJourneySelectResult(!uniDirection);
                }
            });

            listView.setAdapter(adapter);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            dialog.show();
        } else {
            toggleJourneyMode();
            Toast.makeText(this, "No journeys between these locations", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleJourneyMode(){
        journeySelectMode = !journeySelectMode;
        if (journeySelectMode){
            toolbarMenu.setIcon(R.drawable.vector_timeline_active);
        } else {
            toolbarMenu.setIcon(R.drawable.vector_timeline_normal);
        }
    }

    @Override
    public void returnData(Constants.RequestType requestType, Object data) {
        if (dialog != null)
            dialog.dismiss();

        switch (requestType){

            case RETRIEVE_ALL_DATA:
                Long transferDur = BluetoothHandler.transferEndTime - BluetoothHandler.transferStartTime;
                Long processDur = BluetoothHandler.processEndTime - BluetoothHandler.processStartTime;
                System.out.println("Transfer duration: " + transferDur / 1000 + ", Process Time: " + processDur / 1000);
                Long start = System.currentTimeMillis();
                HashMap<String, Object> listData = (HashMap<String, Object>) data;
                ArrayList<StayPoint> stayPoints = (ArrayList<StayPoint>) listData.get("StayPoints");
                ArrayList<StayPointVisit> stayVisits = (ArrayList<StayPointVisit>) listData.get("StayVisits");
                ArrayList<Journey> journeys = (ArrayList<Journey>) listData.get("Journeys");
                ArrayList<GPSPoint> gpsPoints = (ArrayList<GPSPoint>) listData.get("JourneyPoints");
                ArrayList<String> loggerStatus = (ArrayList<String>) listData.get("LoggerStatus");
                databaseHandler.addStayPoints(stayPoints);
                databaseHandler.addStayPointVisits(stayVisits);
                databaseHandler.addJourneys(journeys);
                databaseHandler.addJourneyPoints(gpsPoints);
                databaseHandler.addSummaryData(loggerStatus);
                Long end = System.currentTimeMillis();
                dialog.dismiss();
                System.out.println("Database insert duration: " + (end - start) / 1000);
                updateEndTime = System.currentTimeMillis();
                databaseHandler.addUpdate(updateStartTime,updateEndTime);
                break;
        }
        loadData();
    }

    @Override
    public void returnError(String errorMessage) {
        dialog.dismiss();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_error, null);
        TextView txtErrorMessage = (TextView) dialogView.findViewById(R.id.lbl_errorContents);
        Button btnCloseDialog = (Button) dialogView.findViewById(R.id.btn_closeErrorDialog) ;
        txtErrorMessage.setText("Error connecting to tracker, is it switched on and within 50 meters?");
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        dialog.show();
    }
}


/*

ArrayList<String> summaryData = (ArrayList<String>) data;
                String str = "";
                for (int i = 0; i < summaryData.size(); i++){
                    str = str + "|" + summaryData.get(i);
                }
                str = str.substring(1);
                SharedPreferences.Editor editor = getSharedPreferences("prefs", MODE_PRIVATE).edit();
                editor.putString("deviceSummary", str);
                editor.commit();
 */