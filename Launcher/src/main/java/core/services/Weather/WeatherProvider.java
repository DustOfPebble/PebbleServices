package core.services.Weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;

import core.launcher.application.R;
import core.launcher.application.SmartwatchConstants;
import lib.smartwatch.SmartwatchBundle;
import lib.smartwatch.SmartwatchEvents;
import lib.smartwatch.SmartwatchManager;

public class WeatherProvider extends Service implements WeatherQueries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();
    private boolean isRunning;
    private boolean isWaitingConnectivity;

    private SmartwatchManager WatchConnector = null;
    private WeatherAccess Connector = null;
    private NetworkEvents AccessNetwork = null;

    private WeatherMiner Miner = null;

    private WakeUpManager WakeUp = null;
    private long SleepDelay = 20*60*1000; // in ms
    private long NextUpdateTimeStamps = 0;

    private int ID = 0;

    public WeatherProvider(){
        Connector = new WeatherAccess();
        isRunning = false;
    }

    private SmartwatchBundle make(Bundle Snapshot) {
        SmartwatchBundle WatchSet = new SmartwatchBundle();
        for (String key : Snapshot.keySet()) {
            // Managing data from Weather Service
            if (key.equals(WeatherKeys.WeatherID))
                WatchSet.update(SmartwatchConstants.WeatherSkyNow, (byte) Snapshot.getInt(key), false);
            if (key.equals(WeatherKeys.TemperatureID))
                WatchSet.update(SmartwatchConstants.WeatherTemperatureNow, (byte) Snapshot.getInt(key), true);
            if (key.equals(WeatherKeys.TemperatureMaxID))
                WatchSet.update(SmartwatchConstants.WeatherTemperatureMax, (byte) Snapshot.getInt(key), true);
            if (key.equals(WeatherKeys.TemperatureMinID))
                WatchSet.update(SmartwatchConstants.WeatherTemperatureMin, (byte) Snapshot.getInt(key), true);
            if (key.equals(WeatherKeys.LocationNameID))
                WatchSet.update(SmartwatchConstants.WeatherLocationName, Snapshot.getString(key));
        }
        return WatchSet;
    }

    private void pushNotification(String Message){
        NotificationManager MessageSender = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder MessageFactory = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.launcher)
                .setContentTitle("WakeUp Event ")
                .setContentText(Message);

        MessageSender.notify(ID,MessageFactory.build());
        ID++;
    }

    /**************************************************************
     *  Callbacks implementation from NetworkEnabler
     **************************************************************/
    public void Enabled() {
        if (!isWaitingConnectivity) return;
        Log.d(LogTag, "Connectivity enabled --> Updating");
        Miner.start();
    }

    /**************************************************************
     *  Callbacks implementation from WeatherMiner
     **************************************************************/
    public void Update(Bundle Snapshot) {
        if (Snapshot == null) {
//            pushNotification("Download failed");
            return;
        }

        if (Snapshot.size() == 0) {
//            pushNotification("JSON parse error.");
            return;
        }
        isWaitingConnectivity = false;
        pushNotification("Download succeed.");
        Connector.push(Snapshot);
        if (!WatchConnector.isConnected()) return;
        Log.d(LogTag, "Pushing --> Smartwatch");
        WatchConnector.send(make(Snapshot));
    }
    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        WatchConnector = new SmartwatchManager(getBaseContext(),this, SmartwatchConstants.WatchUUID);
        AccessNetwork = new NetworkEvents(this);

        Connector.RegisterProvider(this);
        Miner = new WeatherMiner(this);
        WakeUp = new WakeUpManager(this);
        isRunning = false;
        isWaitingConnectivity = false;
        NextUpdateTimeStamps =  System.currentTimeMillis() + SleepDelay;
        WakeUp.setNext(SleepDelay);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Log.d(LogTag, "Starting service ...");
            isRunning = true;
        }

        if (System.currentTimeMillis() > NextUpdateTimeStamps) {
            WakeUp.setNext(SleepDelay);
            NextUpdateTimeStamps =  System.currentTimeMillis() + SleepDelay;
        }

        isWaitingConnectivity = false;
        if (AccessNetwork.isConnected()) {
            Log.d(LogTag, "Service started with connectivity enabled ==> Updating");
            Miner.start();
        }

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
        if (AccessNetwork.isConnected()) { Miner.start(); return;}
        isWaitingConnectivity =  true;
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged() {
        if (!WatchConnector.isConnected()) return;

        if (AccessNetwork.isConnected()) {
            Miner.start();
            return;
        }

        isWaitingConnectivity =  true;
    }

    @Override
    public void requestUpdate() {
        Log.d(LogTag, "Watch requesting update ");
    }
}
