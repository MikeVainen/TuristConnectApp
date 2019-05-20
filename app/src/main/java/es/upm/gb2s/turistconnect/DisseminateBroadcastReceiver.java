package es.upm.gb2s.turistconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DisseminateBroadcastReceiver extends BroadcastReceiver {

    final String BRTAG = DisseminateBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.println(Log.DEBUG,BRTAG,"onReceive!");


        switch (intent.getAction()){
            //case scan
            case DisseminateService.ACTION_DISS_BLE_SCAN:
                Log.println(Log.DEBUG,BRTAG,DisseminateService.ACTION_DISS_BLE_SCAN);

                break;
            case DisseminateService.ACTION_DISS_CONNECT_BLE:

                break;
            case DisseminateService.ACTION_DISS_WIFI_CONN:

                break;

        }


        //context.startService(new Intent(context,DisseminateService.class));

    }
}
