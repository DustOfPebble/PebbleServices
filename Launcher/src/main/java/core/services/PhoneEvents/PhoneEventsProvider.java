package core.services.PhoneEvents;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import core.launcher.application.ServicesKeys;
import core.launcher.application.SmartWatchExtension;
import core.launcher.application.WatchState;
import lib.smartwatch.SmartwatchEvents;

public class PhoneEventsProvider extends Service implements PhoneEventsQueries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();

    private SmartWatchExtension Watch = null;
    private PhoneEventsAccess Connector=null;
    private PhoneEventsCatcher PhoneEvents = null;
    private boolean isRunning;

    private Bundle UpdateSnapshot = null;

    public PhoneEventsProvider(){
        UpdateSnapshot = new Bundle();
        Connector = new PhoneEventsAccess();
    }

    /**************************************************************
     *  Direct Callbacks implementation from PhoneEventsCatcher
     *  - Missed Calls
     *  - Missed Messages
     **************************************************************/
    public void Update(Bundle PhoneInfos) {
        UpdateSnapshot = PhoneInfos;
        Connector.push(UpdateSnapshot);
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        Connector.RegisterProvider(this);

        Watch = new SmartWatchExtension(getBaseContext());

        PhoneEvents = new PhoneEventsCatcher(this);
        PhoneEvents.enableReceiver(getBaseContext());
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) { Log.d(LogTag, "Starting service ..."); isRunning = true;}
        else Log.d(LogTag, "Service is already running !");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LogTag, "Binding service ...");
        return Connector;
    }

    @Override
    public void onDestroy() {
        Log.d(LogTag, "Service is about to quit !");
        super.onDestroy();
    }

    /**************************************************************
     *  Callbacks implementation for incoming commands
     **************************************************************/
    @Override
    public void Query() {
        Connector.push(UpdateSnapshot);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean isConnected) {
        if (!isConnected) {
            PhoneEvents.resetCount();
            return;
        }

        if (UpdateSnapshot.size() == 0 ) return;
        Connector.push(UpdateSnapshot);
        Watch.push(UpdateSnapshot);
    }


}
