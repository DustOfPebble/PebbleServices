package core.service.Weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import core.launcher.application.R;
import core.launcher.application.SmartWatchExtension;
import core.launcher.application.WatchState;
import lib.smartwatch.SmartwatchEvents;

public class WeatherProvider extends Service implements WeatherQueries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();

    private NotificationManager InfoProvider;
    private Notification.Builder InfoCreator;

    private int WatchStatus = WatchState.Disconnected;
    private SmartWatchExtension Watch = null;
    private Bundle EventSnapshot = null;

    private WeatherAccess Connector=null;
    private WeatherMiner Miner = null;

    public WeatherProvider(){
        EventSnapshot = new Bundle();
        Connector = new WeatherAccess();
    }

    private void PushSystemNotification() {
        int  Info = -1;
        if (WatchStatus == WatchState.Connected) Info = R.string.Connected;
        if (WatchStatus == WatchState.Disconnected) Info = R.string.Disconnected;

        InfoCreator.setContentText(getText(Info));
        InfoProvider.notify(R.string.ID,InfoCreator.build());
    }

    /**************************************************************
     *  Callbacks implementation from Weather Miner
     *  - Missed Calls
     *  - Missed Messages
     **************************************************************/
    public void Update(Bundle WeatherInfos) {
        EventSnapshot = WeatherInfos;
        Watch.push(EventSnapshot);
    }
    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        InfoProvider = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        InfoCreator = new Notification.Builder(this);
        InfoCreator.setSmallIcon(R.drawable.phone_events);
        InfoCreator.setContentTitle(getText(R.string.ServiceWeatherName));

        Connector.RegisterProvider(this);

        Watch = new SmartWatchExtension(getBaseContext());
        WatchStatus = (Watch.isConnected()? WatchState.Connected:WatchState.Disconnected);

        PushSystemNotification();

        Miner = new WeatherMiner(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LogTag, "Starting service ...");
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
        InfoProvider.cancel(R.string.ID);
        super.onDestroy();
    }

    /**************************************************************
     *  Callbacks implementation for incoming commands
     **************************************************************/
    @Override
    public void Query() {
        Connector.Weather(0, "Guyancourt");
        Connector.Temperatures(18,-2,23);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean isConnected) {
        if (isConnected) {
            WatchStatus = WatchState.Disconnected;
            PushSystemNotification();
            return;
        }

        WatchStatus = WatchState.Connected;
        PushSystemNotification();
        return;
    }


}
