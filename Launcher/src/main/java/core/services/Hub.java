package core.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import core.launcher.pebble.R;
import core.launcher.pebble.SmartwatchConstants;
import core.services.PhoneEvents.EventsCatcher;
import core.services.PhoneEvents.PhoneKeys;
import core.services.Weather.WeatherKeys;
import core.services.Weather.Miner;
import core.services.Weather.Network;
import lib.smartwatch.SmartwatchBundle;
import lib.smartwatch.SmartwatchEvents;
import lib.smartwatch.SmartwatchManager;

public class Hub extends Service implements Queries, SmartwatchEvents {
    private static final String LogTag = Hub.class.getSimpleName();
    private static final long WaitDelay = 30*60*1000; // in ms
    private static final long Now = 0;

    private boolean Initializing;
    private boolean isRunning;
//    private boolean isWaitingConnectivity;

    private SmartwatchManager WatchConnector = null;
    private Junction Connector = null;
    private Logger DebugLog = null;

    private Network AccessNetwork = null;
    private Miner DataMiner = null;
//    private WakeUp WakeUp = null;
    private long NextUpdateTimeStamps = 0;

    private EventsCatcher PhoneEvents = null;

    private Intent ClassCallback = null;

    private final int ID = R.string.ServiceWeather;

    public Hub(){ Initializing = true; }

    private static long setUpdate(long Delay) {return System.currentTimeMillis() + Delay;}

    public void Log(String Tag,String Message ) { DebugLog.debug(Tag,Message); }

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

            if (key.equals(PhoneKeys.CallsID))
                WatchSet.update(SmartwatchConstants.CallsCount, (byte) Snapshot.getInt(key), false);
            if (key.equals(PhoneKeys.MessagesID))
                WatchSet.update(SmartwatchConstants.MessagesCount, (byte) Snapshot.getInt(key), false);

        }
        return WatchSet;
    }

    private void pushNotification(String Message){
        NotificationManager MessageSender = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent IntentCallBack = PendingIntent.getActivity(this,0, ClassCallback,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder MessageFactory = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.launcher)
                .setContentTitle(Message)
                .setContentIntent(IntentCallBack);
        MessageSender.notify(ID,MessageFactory.build());
    }

    /**************************************************************
     *  Callbacks implementation from NetworkEnabler
     **************************************************************/
    public void ConnectivityEnabled() {
        //Log(LogTag, "Connectivity enabled !");
        if (System.currentTimeMillis() < NextUpdateTimeStamps) return;
        //if (!isWaitingConnectivity) return;
        if (DataMiner.isRunning) return;
        Log(LogTag, "Starting Weather update...");
        DataMiner.start();
    }

    /**************************************************************
     *  Callbacks implementation from WeatherMiner
     **************************************************************/
    public void Update(Bundle Snapshot) {
        if (Snapshot == null) {
            pushNotification(getResources().getString(R.string.WeatherInfoNotFetched));
            NextUpdateTimeStamps = setUpdate(Now);
            return;
        }

        if (Snapshot.size() == 0) {
            pushNotification(getResources().getString(R.string.WeatherInfoNotFetched));
            NextUpdateTimeStamps = setUpdate(Now);
            return;
        }

//        isWaitingConnectivity = false;
        pushNotification(getResources().getString(R.string.WeatherInfoFetched));
        Connector.push(Snapshot);
        NextUpdateTimeStamps = setUpdate(WaitDelay);
        if (!WatchConnector.isConnected()) return;
        Log(LogTag, "Pushing --> Smartwatch");
        WatchConnector.send(make(Snapshot));
    }
    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();

        if (Initializing)  {
            Connector = new Junction();
            WatchConnector = new SmartwatchManager(getBaseContext(),this, SmartwatchConstants.WatchUUID);
            AccessNetwork = new Network(this);
            DataMiner = new Miner(this);
//            WakeUp = new WakeUp(this);
            PhoneEvents = new EventsCatcher();
            DebugLog = new Logger(this);
            Initializing = false;
        }
        DebugLog.Live(true);

        Connector.RegisterProvider(this);
        isRunning = false;
//        isWaitingConnectivity = false;

        PhoneEvents.enableReceiver(getBaseContext());

        NextUpdateTimeStamps = setUpdate(Now);
//        WakeUp.setNext(SleepDelay);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Log(LogTag, "Starting service ...");
            isRunning = true;
        }

        if (System.currentTimeMillis() > NextUpdateTimeStamps) {
//            WakeUp.setNext(SleepDelay);
            NextUpdateTimeStamps =  setUpdate(Now);
        }

//        isWaitingConnectivity = true;
        if (AccessNetwork.isConnected()) {
//            isWaitingConnectivity = false;
            Log(LogTag, "Service started with connectivity enabled ==> Updating");
            DataMiner.start();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log(LogTag, "Binding service ...");
        return Connector;
    }

    @Override
    public void onDestroy() {
        Log(LogTag, "Service is about to quit !");
        super.onDestroy();
    }

    /**************************************************************
     *  Callbacks implementation for incoming commands
     **************************************************************/
    @Override
    public void query() {
        if (AccessNetwork.isConnected()) DataMiner.start();
        else NextUpdateTimeStamps = setUpdate(Now);
//        isWaitingConnectivity =  true;
//        Connector.push(PhoneEvents.History());
    }

    @Override
    public void setNotificationCallback(Intent ActivityCallback){
        ClassCallback = ActivityCallback;
    }
    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged() {
        if (!WatchConnector.isConnected()) PhoneEvents.resetCount();
        else {
            Connector.push(PhoneEvents.History());
            Log(LogTag, "Pushing --> Smartwatch");
            WatchConnector.send(make(PhoneEvents.History()));

            if (AccessNetwork.isConnected()) DataMiner.start();
            else NextUpdateTimeStamps = setUpdate(Now);
        }

//        if (AccessNetwork.isConnected()) DataMiner.start();
//        else isWaitingConnectivity =  true;
    }

    @Override
    public void requestUpdate() {
        Log(LogTag, "Watch requesting update ");
    }
}
