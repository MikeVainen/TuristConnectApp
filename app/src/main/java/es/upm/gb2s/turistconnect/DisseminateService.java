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
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    public static final String ACTION_DISS_BLE_SCAN = "com.mviana.turistconnect.dissemination.ble.ACTION_SCAN_GATT";
    public static final String ACTION_DISS_CONNECT_BLE = "com.mviana.turistconnect.dissemination.ble.ACTION_CONN_GATT";
    public static final String ACTION_DISS_GATT_CONNECTED = "com.mviana.turistconnect.dissemination.ble.ACTION_GATT_CONNECTED";
    public static final String ACTION_DISS_GATT_DISCONNECTED = "com.mviana.turistconnect.dissemination.ble.ACTION_GATT_DISCONN";
    public static final String ACTION_DISS_GATT_DATA_AVLBLE = "com.mviana.turistconnect.dissemination.ble.ACTION_DATA_AVLBLE";
    public static final String ACTION_DISS_GATT_DATA_EXTRA = "com.mviana.turistconnect.dissemination.ble.ACTION_DATA_EXTRA";

    public static final String ACTION_DISS_WIFI_SCAN = "com.mviana.turistconnect.dissemination.wifi.ACTION_SCAN_WIFI";
    public static final String ACTION_DISS_WIFI_CONN = "com.mviana.turistconnect.dissemination.wifi.ACTION_CONN_WIFI";

    private static final int RESULT_DISS_SCAN_SUCCESS = 1;
    private static final int RESULT_DISS_SCAN_NOMATCH = 0;
    private static final int RESULT_DISS_SCAN_FAILURE = -1;
    private static final int RESULT_DISS_CONN_SUCCESS = 1;
    private static final int RESULT_DISS_CONN_NOMATCH = 0;
    private static final int RESULT_DISS_CONN_FAILURE = -1;

    public static final String DISS_INTENT_RESULT_NAME = "diss_service_results";

    private static final long TIME_MAX_SCAN = 10000;
    private boolean bleScanning;
    private boolean wifiScanning;

    private List<BluetoothDevice> bledevices;

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

            if(result!= null && result.getDevice()!=null){
                switch(callbackType){
                    case(ScanSettings.CALLBACK_TYPE_ALL_MATCHES):

                        break;
                    case(ScanSettings.CALLBACK_TYPE_FIRST_MATCH):

                        break;

                    case(ScanSettings.CALLBACK_TYPE_MATCH_LOST):

                        break;
                }

                if(!bledevices.contains(result.getDevice())){
                    bledevices.add(result.getDevice());
                }


            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //super.onBatchScanResults(results);
            if(results!=null){
                Log.println(Log.DEBUG,TAG,Integer.toString(results.size()));
            }
            Log.println(Log.DEBUG,TAG,"Callback: onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.println(Log.DEBUG,TAG,"Callback: onScanFailed");
            switch (errorCode){
                case ScanCallback.SCAN_FAILED_ALREADY_STARTED:

                    break;
                case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:

                    break;

                case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:

                    break;

                case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:

                    break;
            }
        }
    };
    private BluetoothGattCallback gattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_DISS_GATT_CONNECTED;
                //broadcastUpdate(intentAction);
                //gatt.discoverServices();

            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_DISS_GATT_DISCONNECTED;
                //broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            }else{

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    private final IBinder mBinder = new LocalBinder();

    /**
     *
     * @param action the used action for the server after it finished its task
     * @param characteristic
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic){



    }


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
        this.bleScanning = false;
        this.wifiScanning = false;
        this.bledevices = Arrays.asList(new BluetoothDevice[]{});

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

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.println(Log.DEBUG,TAG, getString(R.string.disservice_active));
        if(intent!=null){
            if(intent.getAction().equals(ACTION_DISS_BLE_SCAN)){
                Log.println(Log.DEBUG,TAG,"SCAN Intent requested");
                onScanBLERequest();


            }else if(intent.getAction().equals(ACTION_DISS_WIFI_SCAN)){

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

    /**
     * onScanBLERequest performs a BLE scan of the pre-set filter devices in the area
     * To achieve so, the scan process is triggered while a handler will manage the end
     * of the scanning phase through a postDelayed thread.
     *
     * The matching devices found will be stored
     *
     * @return result of the BLE Scanning
     */
    private int onScanBLERequest(){
        Log.println(Log.DEBUG,"onScanBLERequest","Start Scan");
        int scanResult = RESULT_DISS_SCAN_NOMATCH;
        final CountDownLatch stopscanLatch = new CountDownLatch(1);
        handler = new Handler();
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleScanning = false;
                    blescanner.stopScan(leScanCallback);
                    stopscanLatch.countDown();

                }
            }, TIME_MAX_SCAN);
            bleScanning = true;
            blescanner.startScan((List<ScanFilter>) Arrays.asList(blefilters)
                    , blesetting.build(), leScanCallback);
            stopscanLatch.await(2 * TIME_MAX_SCAN, TimeUnit.MILLISECONDS);
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }
        Intent bcastDeadIntent = new Intent(ConnectActivity.BCAST_NOTIFICATION_BLE_SCAN);
        bcastDeadIntent.putExtra(DISS_INTENT_RESULT_NAME,scanResult);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bcastDeadIntent);

        return scanResult;
    }
    private void onConnectGatt(){
        if(!bledevices.isEmpty()){
            BluetoothGatt bleGatt = bledevices.get(0).connectGatt(this, false, gattcallback);
        }
    }


    private void GattServerConnection(int[] connectParams, BluetoothDevice gattserver){
        gattserver.connectGatt(this,false, gattcallback);
    }



}
