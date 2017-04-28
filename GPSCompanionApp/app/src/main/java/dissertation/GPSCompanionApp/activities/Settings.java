package dissertation.GPSCompanionApp.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import dissertation.GPSCompanionApp.R;
import dissertation.GPSCompanionApp.helpers.BluetoothDataClient;
import dissertation.GPSCompanionApp.helpers.BluetoothHandler;
import dissertation.GPSCompanionApp.helpers.Constants;
import dissertation.GPSCompanionApp.helpers.SQLHelper;
import dissertation.GPSCompanionApp.helpers.Utils;

public class Settings extends AppCompatActivity implements View.OnClickListener, BluetoothDataClient {

    Button btnClearLocal, btnRetrieveSummary, btnViewEditSettings;
    SQLHelper sqlHelper;
    Dialog dialog;

    TextView lblDeviceFileSize, lblDevicePointCount, lblDeviceStayCount, LblDeviceVisitCount,
            lblDeviceJourneyCount, lblDeviceLatestPoint, lblDeviceOldestPoint, lblDeviceStayUpdate,
            lblDeviceJourneyUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnRetrieveSummary = (Button) findViewById(R.id.btn_retrieveSummary);
        btnRetrieveSummary.setOnClickListener(this);

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

        sqlHelper = new SQLHelper(this);

        dialog = new Dialog(this);

        configToolbar();
        updateDeviceSummary();
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

    private void getDeviceSummary(){
        showLoadingDialog();
        BluetoothDevice device = getDevice();
        if (device != null){
            BluetoothHandler bluetoothHandler = new BluetoothHandler(this, device);
            bluetoothHandler.retrieveSummary();
        }
    }

    private void updateDeviceSettings(String[] settings){
        BluetoothHandler bluetoothHandler = new BluetoothHandler(this, getDevice());
        bluetoothHandler.submitSettings(settings);
    }

    private void updateDeviceSummary(){
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        String data = prefs.getString("deviceSummary", null);
        if (data != null) {
            ArrayList<String> summaryData = new ArrayList<>();
            String[] strArray = data.split("\\|");
            for (int i = 0; i < strArray.length; i++){
                summaryData.add(strArray[i]);
            }
            lblDeviceFileSize.setText(summaryData.get(0));
            lblDevicePointCount.setText(summaryData.get(1));
            lblDeviceStayCount.setText(summaryData.get(2));
            LblDeviceVisitCount.setText(summaryData.get(3));
            lblDeviceJourneyCount.setText(summaryData.get(4));
            String latestPoint = summaryData.get(5);
            String oldestPoint = summaryData.get(6);
            String stayUpdate = summaryData.get(7);
            String journeyUpdate = summaryData.get(8);
            if (!latestPoint.equals("-"))
                lblDeviceLatestPoint.setText(Utils.getDateTimeReadable(latestPoint));
            if (!oldestPoint.equals("-"))
                lblDeviceOldestPoint.setText(Utils.getDateTimeReadable(oldestPoint));
            if (!stayUpdate.equals("-"))
                lblDeviceStayUpdate.setText(Utils.getDateTimeReadable(stayUpdate));
            if (!journeyUpdate.equals("-"))
                lblDeviceJourneyUpdate.setText(Utils.getDateTimeReadable(journeyUpdate));
        }
    }

    public BluetoothDevice getDevice(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
        SharedPreferences sharedPreferences = getSharedPreferences("prefs",MODE_PRIVATE);
        String deviceAddress = sharedPreferences.getString("choosenDevice", null);
        BluetoothDevice mmDevice = null;
        if (deviceAddress != null){
            mmDevice =  mBluetoothAdapter.getRemoteDevice(deviceAddress);
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
                sqlHelper.clearLocalData();
                break;

            case R.id.btn_retrieveSummary:
                getDeviceSummary();
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

            case RETRIEVE_SUMMARY:
                ArrayList<String> summaryData = (ArrayList<String>) data;
                String str = "";
                for (int i = 0; i < summaryData.size(); i++){
                    str = str + "|" + summaryData.get(i);
                }
                str = str.substring(1);
                SharedPreferences.Editor editor = getSharedPreferences("prefs", MODE_PRIVATE).edit();
                editor.putString("deviceSummary", str);
                editor.commit();
                updateDeviceSummary();
                break;

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