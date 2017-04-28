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

public class Provider extends Service implements Queries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();
    private boolean isRunning;
    private boolean isWaitingConnectivity;

    private SmartwatchManager WatchConnector = null;
    private Bind Connector = null;
    private Network AccessNetwork = null;

    private core.services.Weather.Miner Miner = null;

    private core.services.Weather.WakeUp WakeUp = null;
    private long SleepDelay = 20*60*1000; // in ms
    private long NextUpdateTimeStamps = 0;

    private final int ID = R.string.ServiceWeather;

    public Provider(){
        Connector = new Bind();
        isRunning = false;
    }

    private SmartwatchBundle make(Bundle Snapshot) {
        SmartwatchBundle WatchSet = new SmartwatchBundle();
        for (String key : Snapshot.keySet()) {
            // Managing data from Weather Service
            if (key.equals(Keys.WeatherID))
                WatchSet.update(SmartwatchConstants.WeatherSkyNow, (byte) Snapshot.getInt(key), false);
            if (key.equals(Keys.TemperatureID))
                WatchSet.update(SmartwatchConstants.WeatherTemperatureNow, (byte) Snapshot.getInt(key), true);
            if (key.equals(Keys.TemperatureMaxID))
                WatchSet.update(SmartwatchConstants.WeatherTemperatureMax, (byte) Snapshot.getInt(key), true);
            if (key.equals(Keys.TemperatureMinID))
                WatchSet.update(SmartwatchConstants.WeatherTemperatureMin, (byte) Snapshot.getInt(key), true);
            if (key.equals(Keys.LocationNameID))
                WatchSet.update(SmartwatchConstants.WeatherLocationName, Snapshot.getString(key));
        }
        return WatchSet;
    }

    private void pushNotification(String Message){
        NotificationManager MessageSender = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder MessageFactory = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.launcher)
                .setContentTitle(Message);
        MessageSender.notify(ID,MessageFactory.build());
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
            pushNotification(getResources().getString(R.string.WeatherInfoNotFetched));
            return;
        }

        if (Snapshot.size() == 0) {
            pushNotification(getResources().getString(R.string.WeatherInfoNotFetched));
            return;
        }

        isWaitingConnectivity = false;
        pushNotification(getResources().getString(R.string.WeatherInfoFetched));
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
        AccessNetwork = new Network(this);

        Connector.RegisterProvider(this);
        Miner = new Miner(this);
        WakeUp = new WakeUp(this);
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
