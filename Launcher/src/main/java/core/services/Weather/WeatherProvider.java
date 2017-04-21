package core.services.Weather;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import core.launcher.application.SmartWatchExtension;
import lib.smartwatch.SmartwatchEvents;

public class WeatherProvider extends Service implements WeatherQueries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();
    private boolean isRunning;

    private SmartWatchExtension Watch = null;
    private WeatherAccess Connector=null;

    private WeatherMiner Miner = null;
    private Bundle UpdateSnapshot = null;

    public WeatherProvider(){
        UpdateSnapshot = new Bundle();
        Connector = new WeatherAccess();
        isRunning = false;
    }

    /**************************************************************
     *  Callbacks implementation from WeatherMiner
     **************************************************************/
    public void Update(Bundle WeatherInfos) {
        UpdateSnapshot = WeatherInfos;
        Watch.push(UpdateSnapshot);
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
        Miner = new WeatherMiner(this);
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
        if (UpdateSnapshot.size() == 0) return;
        Connector.push(UpdateSnapshot);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean isConnected) {
        if (!isConnected) return;
        if (UpdateSnapshot.size() == 0) return;
        Watch.push(UpdateSnapshot);
    }
}
