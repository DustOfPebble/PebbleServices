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


    private int WatchStatus = WatchState.Disconnected;
    private int MissedCallsCount = 0;
    private int MissedMessagesCount =0;

    private Bundle EventSnapshot = null;

    public PhoneEventsProvider(){
        EventSnapshot = new Bundle();
        Connector = new PhoneEventsAccess();
    }

    /**************************************************************
     *  Direct Callbacks implementation from PhoneEventsCatcher
     *  - Missed Calls
     *  - Missed Messages
     **************************************************************/
    public void Call() {
        EventSnapshot.clear();
        MissedCallsCount++;
        EventSnapshot.putInt(ServicesKeys.CallsID, MissedCallsCount);
        EventSnapshot.putInt(ServicesKeys.MessagesID, MissedMessagesCount);
        Watch.push(EventSnapshot);

        Connector.PhoneEvents(MissedCallsCount,MissedMessagesCount);
    }

    public void Message() {
        EventSnapshot.clear();
        MissedMessagesCount++;
        EventSnapshot.putInt(ServicesKeys.CallsID, MissedCallsCount);
        EventSnapshot.putInt(ServicesKeys.MessagesID, MissedMessagesCount);
        Watch.push(EventSnapshot);

        Connector.PhoneEvents(MissedCallsCount,MissedMessagesCount);
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        Connector.RegisterProvider(this);

        Watch = new SmartWatchExtension(getBaseContext());
        WatchStatus = (Watch.isConnected()? WatchState.Connected: WatchState.Disconnected);

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
        Connector.PhoneEvents(MissedCallsCount,MissedMessagesCount);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean isConnected) {
        if (isConnected) {
            MissedCallsCount = 0;
            MissedMessagesCount = 0;
            WatchStatus = WatchState.Disconnected;
            return;
        }

        WatchStatus = WatchState.Connected;
    }


}
