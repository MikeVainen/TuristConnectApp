package es.upm.gb2s.turistconnect;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class DisseminateService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    protected class LocalBinder extends Binder {
        DisseminateService getService(){
            return DisseminateService.this;
        }
    }

    private static final int SCAN_MAX_TIMEOUT = 10000;

    private static final String TAG = DisseminateService.class.getSimpleName();
    //Some pre-made scan filters for the possible comeback
    private static final ScanFilter[] blefilters = new ScanFilter[]{
      new ScanFilter.Builder()
              .setDeviceName("beacon")
            .build(),
      new ScanFilter.Builder()
              .setDeviceAddress("D4:49:FD:36:04:3B")
            .build()
    };

    private static final ScanFilter.Builder blefilter = new ScanFilter.Builder()
            .setDeviceName("Device_Name");

    private static final ScanSettings.Builder blesetting = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setReportDelay(100);

    private BluetoothLeScanner blescanner;
    private Handler handler;
    private boolean bindedService;

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.println(Log.DEBUG,TAG,"Callback: onScanResult");

            //result.getAdvertisingId devuelve el int con el id del advertiser
            //result.getDevice devuelve el BluetoothDevice asociado a la dirección MAC encontrada
            //let's call the connect2GattServer(BluetoothDevice GattServerdevice)
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.println(Log.DEBUG,TAG,"Callback: onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.println(Log.DEBUG,TAG,"Callback: onScanFailed");
        }
    };
    private BluetoothGattCallback gattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    private final IBinder mBinder = new LocalBinder();

    public static final String ACTION_DISS_START_SERV = "com.mviana.turistconnect.dissemination.START_SERVICE"; //Call to create the service instance in the system: get a hand on the BLE properties
    public static final String ACTION_DISS_KILL_SERV = "com.mviana.turistconnect.dissemination.KILL_SERVICE"; //Call to kill the service instance
    public static final String ACTION_DISS_START_SCAN = "com.mviana.turistconnect.dissemination.ble.CONNECT_DEVICE"; //Call to start the service
    public static final String ACTION_DISS_STOP_SCAN = "com.mviana.turistconnect.dissemination.ble.DISCONNECT_DEVICE";
    public static final String ACTION_DISS_RE_SCAN = "com.mviana.turistconnect.dissemination.ble.RE_SCAN";



    public DisseminateService(){
        super(TAG);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        handler = new Handler();
        final BluetoothManager bleman = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.blescanner = bleman.getAdapter().getBluetoothLeScanner();
        this.bindedService = false;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.println(Log.DEBUG,TAG, getString(R.string.disservice_stopped));
        Intent bcastDeadIntent = new Intent(this, ConnectActivity.DisseminateBroadcastReceiver.class);
        sendBroadcast(bcastDeadIntent);


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.println(Log.DEBUG,TAG, getString(R.string.disservice_active));
        if(intent!=null){
            if(intent.getAction().equals(ACTION_GATT_SCAN)){
                Log.println(Log.DEBUG,TAG,"SCAN Intent requested");
                blescanner.startScan( (List<ScanFilter>) Arrays.asList(blefilters), blesetting.build(),leScanCallback);


            }else if(intent.getAction().equals(ACTION_STOP_SCAN)){
                Log.println(Log.DEBUG,TAG,"SCAN Intent requested");
                blescanner.stopScan(leScanCallback);
            }
            else if(intent.getAction().equals(ACTION_TEST_DISS)){
                Log.println(Log.DEBUG,TAG,"Intent TEST call received");


            }



        }

    }

    @Override
    public IBinder onBind(Intent intent){
        bindedService = true;
        return mBinder;

    }

    @Override
    public boolean onUnbind(Intent intent){

        return true;

    }

    @Override
    public void onRebind(Intent intent){

    }



    private void GattServerConnection(int[] connectParams, BluetoothDevice gattserver){
        gattserver.connectGatt(this,false, gattcallback);
    }



}
