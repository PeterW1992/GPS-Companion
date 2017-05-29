package dissertation.GPSCompanionApp.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import dissertation.GPSCompanionApp.R;
import dissertation.GPSCompanionApp.helpers.BluetoothDataClient;
import dissertation.GPSCompanionApp.helpers.BluetoothHandler;
import dissertation.GPSCompanionApp.helpers.Constants;
import dissertation.GPSCompanionApp.helpers.DatabaseHandler;
import dissertation.GPSCompanionApp.helpers.Utils;

public class Settings extends AppCompatActivity implements View.OnClickListener, BluetoothDataClient {

    Button btnClearLocal, btnViewEditSettings;
    DatabaseHandler databaseHandler;
    Dialog dialog;

    TextView lblDeviceFileSize, lblDevicePointCount, lblDeviceStayCount, LblDeviceVisitCount,
            lblDeviceJourneyCount, lblDeviceLatestPoint, lblDeviceOldestPoint, lblDeviceStayUpdate,
            lblDeviceJourneyUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnClearLocal = (Button) findViewById(R.id.btn_clear_data);
        btnClearLocal.setOnClickListener(this);

        btnViewEditSettings = (Button) findViewById(R.id.btn_viewEditDeviceSettings);
        btnViewEditSettings.setOnClickListener(this);

        lblDeviceFileSize = (TextView) findViewById(R.id.lbl_deviceDBSizeValue);
        lblDevicePointCount = (TextView) findViewById(R.id.lbl_deviceDBPointCountValue);
        lblDeviceStayCount = (TextView) findViewById(R.id.lbl_deviceDBStayCountValue);
        LblDeviceVisitCount = (TextView) findViewById(R.id.lbl_deviceDBVisitCountValue);
        lblDeviceJourneyCount = (TextView) findViewById(R.id.lbl_deviceDBJourneyCountValue);
        lblDeviceLatestPoint = (TextView) findViewById(R.id.lbl_deviceDBLatestValue);
        lblDeviceOldestPoint = (TextView) findViewById(R.id.lbl_deviceDBOldestValue);
        lblDeviceStayUpdate = (TextView) findViewById(R.id.lbl_deviceDBStayUpdateValue);
        lblDeviceJourneyUpdate = (TextView) findViewById(R.id.lbl_deviceDBJourneyUpdateValue);

        databaseHandler = new DatabaseHandler(this);
        updateDeviceSummary();
        dialog = new Dialog(this);
        configToolbar();
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

    private void showSettingsDialog(ArrayList<String> settings){
        if (dialog != null)
            dialog.dismiss();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_device_settings, null);

        Button btnCloseDialog = (Button) dialogView.findViewById(R.id.btn_closeSettingsDialog);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        final Spinner spnStayLength = (Spinner) dialogView.findViewById(R.id.spn_stayLength);
        final Spinner spnStayRadius = (Spinner) dialogView.findViewById(R.id.spn_stayRadius);
        final Spinner spnJourneyComp = (Spinner) dialogView.findViewById(R.id.spn_journeyComp);

        final Button btnUpdateSettings = (Button) dialogView.findViewById(R.id.btn_updateSettings);
        btnUpdateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] settings = new String[3];
                settings[0] = (String) spnStayLength.getSelectedItem();
                settings[1] = (String) spnStayRadius.getSelectedItem();
                settings[2] = (String) spnJourneyComp.getSelectedItem();
                updateDeviceSettings(settings);
                dialog.dismiss();
            }
        });

        String[] lengthValues = getResources().getStringArray(R.array.arr_lengthValues);
        String[] radiusValues = getResources().getStringArray(R.array.arr_radiusValues);
        String[] compValues = getResources().getStringArray(R.array.arr_compValues);

        ArrayAdapter<String> stayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        stayAdapter.addAll(lengthValues);

        int currentLengthIndex = Utils.getIndexOfValue(lengthValues, settings.get(0));
        int currentRadiusIndex = Utils.getIndexOfValue(radiusValues, settings.get(1));
        int currentCompIndex = Utils.getIndexOfValue(compValues, settings.get(2));

        ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        radiusAdapter.addAll(radiusValues);

        ArrayAdapter<String> compAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        compAdapter.addAll(compValues);

        spnStayLength.setAdapter(stayAdapter);
        spnStayRadius.setAdapter(radiusAdapter);
        spnJourneyComp.setAdapter(compAdapter);

        spnStayLength.setSelection(currentLengthIndex);
        spnStayRadius.setSelection(currentRadiusIndex);
        spnJourneyComp.setSelection(currentCompIndex);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void updateDeviceSummary(){
        HashMap<String,String> data = databaseHandler.getLatestSummaryData();

        if (data != null) {

            lblDeviceFileSize.setText(data.get(databaseHandler.COL_DATABASE_SIZE));

            lblDevicePointCount.setText(data.get(databaseHandler.COL_GPS_POINTS));
            lblDeviceStayCount.setText(data.get(databaseHandler.COL_STAY_POINTS));
            LblDeviceVisitCount.setText(data.get(databaseHandler.COL_VISITS));
            lblDeviceJourneyCount.setText(data.get(databaseHandler.COL_JOURNEYS));

            String latestPoint = data.get(databaseHandler.COL_LATEST_POINT);
            String oldestPoint = data.get(databaseHandler.COL_OLDEST_POINT);
            String stayUpdate = data.get(databaseHandler.COL_LATEST_STAY_UPDATE);
            String journeyUpdate = data.get(databaseHandler.COL_LATEST_JOURNEY_UPDATE);

            if (latestPoint != null)
                lblDeviceLatestPoint.setText(Utils.getDateTimeReadable(latestPoint));
            if (oldestPoint != null)
                lblDeviceOldestPoint.setText(Utils.getDateTimeReadable(oldestPoint));
            if (stayUpdate != null)
                lblDeviceStayUpdate.setText(Utils.getDateTimeReadable(stayUpdate));
            if (journeyUpdate != null)
                lblDeviceJourneyUpdate.setText(Utils.getDateTimeReadable(journeyUpdate));
        }
    }

    private void updateDeviceSettings(String[] settings){
        BluetoothHandler bluetoothHandler = new BluetoothHandler(this, getDevice());
        bluetoothHandler.submitSettings(settings);
    }

    public BluetoothDevice getDevice(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
        SharedPreferences sharedPreferences = getSharedPreferences("prefs",MODE_PRIVATE);
        String deviceAddress = sharedPreferences.getString("choosenDevice", null);
        BluetoothDevice mmDevice = null;
        if (deviceAddress != null){
            mmDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        }
        return mmDevice;
    }

    /* Adds toolbar to activity */
    private void configToolbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        try {
            getSupportActionBar().setTitle(" " + getString(R.string.title_mainMenu));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception exception) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btn_clear_data:
                databaseHandler.clearLocalData();
                break;

            case R.id.btn_viewEditDeviceSettings:
                showLoadingDialog();
                BluetoothDevice device = getDevice();
                BluetoothHandler bluetoothHandler = new BluetoothHandler(this, device);
                bluetoothHandler.retrieveSettings();
                break;
        }
    }

    @Override
    public void returnData(Constants.RequestType requestType, Object data) {
        dialog.dismiss();
        switch (requestType) {

            case RETRIEVE_SETTINGS:
                ArrayList<String> setting = (ArrayList<String>) data;
                showSettingsDialog(setting);
                break;

            case UPDATE_SETTINGS:
                Toast.makeText(this, "Device Settings Updated", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void returnError(String errorMessage) {
        // Process Error
    }

}
