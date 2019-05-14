package es.upm.gb2s.turistconnect;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

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

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTION_ATTEMPT = 1;
    private static final int STATE_CONNECTED = 2;

    private static final int SCAN_MAX_TIMEOUT = 10000;

    private static final String TAG = DisseminateService.class.getSimpleName();

    private BluetoothAdapter bleadapter;
    private BluetoothLeScanner blescanner;
    private String bledeviceaddress;
    private BluetoothGatt bleGatt;
    private int connectionState = STATE_DISCONNECTED;
    private boolean mScanning;
    private Handler handler;

    private BluetoothAdapter.LeScanCallback leScanCallBack = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {



        }

    };


private final IBinder mBinder = new LocalBinder();

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public DisseminateService(){
        super(TAG);
    }


    public DisseminateService(String name, BluetoothAdapter blea) {
        super(name);
        this.bleadapter = blea;
        this.blescanner = blea.getBluetoothLeScanner();

    }

    @Override
    public void onCreate(){
        super.onCreate();
        mScanning = false;
        handler = new Handler();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.println(Log.ERROR,TAG, getString(R.string.disservice_stopped));

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.println(Log.ERROR,TAG, getString(R.string.disservice_active));
        if(intent!=null){

        }

    }

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;

    }

    private void scanLEDevice(final boolean enable){
        if(enable){
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    mScanning = false;
                }
            }, SCAN_MAX_TIMEOUT);
            mScanning = true;
            //If call to specific peripherals use startScan (List<ScanFilter> filters, ScanSettings settings, ScanCallback callback)

        }
        else{
            mScanning = false;

        }
    }
}
