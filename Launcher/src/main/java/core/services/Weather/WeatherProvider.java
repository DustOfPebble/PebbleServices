package core.services.Weather;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import core.launcher.application.SmartwatchConstants;
import lib.smartwatch.SmartwatchBundle;
import lib.smartwatch.SmartwatchEvents;
import lib.smartwatch.SmartwatchManager;

public class WeatherProvider extends Service implements WeatherQueries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();
    private boolean isRunning;

    private SmartwatchManager WatchConnector = null;
    private WeatherAccess Connector=null;

    private WeatherMiner Miner = null;
    private Bundle StoredSnapshot = null;

    private long RefreshDelay = 5*60*1000; // in s

    public WeatherProvider(){
        StoredSnapshot = new Bundle();
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

    /**************************************************************
     *  Callbacks implementation from WeatherMiner
     **************************************************************/
    public void Update(Bundle Snapshot) {
        StoredSnapshot = Snapshot;
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
        Connector.RegisterProvider(this);
        Miner = new WeatherMiner(this);
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Log.d(LogTag, "Starting service ...");
            isRunning = true;
        }
        else Log.d(LogTag, "Service is already running !");

        Miner.start();
/*        // Program an Alarm is the device is asleep
        AlarmManager alarmManager = (AlarmManager) getSystemService(getBaseContext().ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, WeatherProvider.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + RefreshDelay, pendingIntent);
*/
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
        if (StoredSnapshot.size() == 0) return;
        Connector.push(StoredSnapshot);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged() {
        if (!WatchConnector.isConnected()) return;
        Miner.start();
    }

    @Override
    public void requestUpdate() {
        Log.d(LogTag, "Watch requesting update ");
        Miner.start();
    }
}
