package core.services.PhoneEvents;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import core.launcher.application.SmartwatchConstants;
import lib.smartwatch.SmartwatchBundle;
import lib.smartwatch.SmartwatchEvents;
import lib.smartwatch.SmartwatchManager;

public class Provider extends Service implements Queries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();

    private SmartwatchManager WatchConnector = null;
    private Bind Connector=null;
    private EventsCatcher PhoneEvents = null;
    private boolean isRunning;

    private Bundle StoredSnapshot = null;

    public Provider(){
        StoredSnapshot = new Bundle();
        Connector = new Bind();
    }

    private SmartwatchBundle make(Bundle Snapshot) {
        SmartwatchBundle WatchSet = new SmartwatchBundle();
        for (String key : Snapshot.keySet()) {
            // Managing data from push Service
            if (key.equals(Keys.CallsID))
                WatchSet.update(SmartwatchConstants.CallsCount, (byte) Snapshot.getInt(key), false);
            if (key.equals(Keys.MessagesID))
                WatchSet.update(SmartwatchConstants.MessagesCount, (byte) Snapshot.getInt(key), false);
        }
        return WatchSet;
    }

    /**************************************************************
     *  Direct Callbacks implementation from PhoneEventsCatcher
     *  - Missed Calls
     *  - Missed Messages
     **************************************************************/
    public void Update(Bundle Snapshot) {
        StoredSnapshot = Snapshot;
        Connector.push(Snapshot);
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        WatchConnector = new SmartwatchManager(getBaseContext(),this, SmartwatchConstants.WatchUUID);
        Connector.RegisterProvider(this);
        PhoneEvents = new EventsCatcher(this);
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
    public void query() {
        Connector.push(StoredSnapshot);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged() {
        if (!WatchConnector.isConnected()) {
            PhoneEvents.resetCount();
            return;
        }
        Connector.push(StoredSnapshot);
        Log.d(LogTag, "Pushing --> Smartwatch");
        WatchConnector.send(make(StoredSnapshot));
    }

    @Override
    public void requestUpdate() { }
}
