package es.upm.gb2s.turistconnect;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_SCAN_BT = 1;
    private static final int REQUEST_DISABLE_BT = -1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private BluetoothAdapter bluetoothAdapter;
    private DisseminateService disservice;
    private Handler dissHandler;

    private TextView tView;

    private ServiceConnection dissConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DisseminateService.LocalBinder binder = (DisseminateService.LocalBinder) service;
            disservice = binder.getService();
        }

        @Override
        public void onBindingDied(ComponentName name){

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bleman = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bleman.getAdapter();

        this.tView = (TextView) findViewById(R.id.connectTv);
        this.dissHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Update UI to show latest status of connection
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            Log.println(Log.ERROR,"Permissions","Requesting coarse location permissions");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        else{
            //checkBLE
            Log.println(Log.INFO,"Permissions","Coarse Location permissions granted");
            onManageBLE(REQUEST_ENABLE_BT);

        }



    }

    @Override
    protected void onStop(){
        super.onStop();
        //onManageBLE(REQUEST_DISABLE_BT);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        onManageBLE(REQUEST_DISABLE_BT);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.println(Log.INFO,"Permissions","Coarse Location permissions granted");
                    //Go to check BLE ->
                    onManageBLE(REQUEST_ENABLE_BT);
                }
                else{
                    final boolean needRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);


                }
        }

    }

    private void onManageBLE(int code){

        switch (code){
            case REQUEST_ENABLE_BT:
                if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
                    Intent enableBLEIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBLEIntent, REQUEST_ENABLE_BT);
                }
                else{
                    //BLE is already enabled!
                    onStartDisseminateService();
                }
                break;

            case REQUEST_DISABLE_BT:
                if(bluetoothAdapter !=null|| bluetoothAdapter.isEnabled()){
                    bluetoothAdapter.disable();
                }



        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this,getString(R.string.ble_req_deny),Toast.LENGTH_LONG).show();
            finish();
        }
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            Log.println(Log.INFO,"BLE",getString(R.string.man_en_ble));
            tView.setText(getString(R.string.connecting_text));
            if(bluetoothAdapter!=null){
                onStartDisseminateService();

            }
            else{
                Log.println(Log.ERROR,"BLE","Apparently adapter is null?");
            }

        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void onStartDisseminateService(){

        //check if Service is running.
        boolean disseminationRunning = false;
        ActivityManager actmanager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:
                actmanager.getRunningServices(Integer.MAX_VALUE)){
            if(DisseminateService.class.getName().equals(service.service.getClassName())) disseminationRunning = true;

        }
        final Intent dissIntent = new Intent(this, DisseminateService.class);
        if(!disseminationRunning){

            dissIntent.setAction(DisseminateService.ACTION_DISS_BLE_SCAN);
            startService(dissIntent);
            //bindService(dissIntent,dissConnection,Context.BIND_AUTO_CREATE);

        }
        else bindService(dissIntent,dissConnection,0);


    }








}
