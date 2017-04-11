package lib.core.heartspy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import lib.events.SensorEvents;
import lib.service.ServiceAccess;
import lib.service.ServiceCommands;
import lib.service.ServiceState;
import lib.wrist.sensor.SensorDetector;
import lib.wrist.sensor.SensorManager;

public class WeatherProvider extends Service implements SensorEvents, ServiceCommands {

    private String LogTag = this.getClass().getSimpleName();

    private NotificationManager InfoProvider;
    private Notification.Builder InfoCreator;

    private SensorManager SensorListener = null;
    private SensorDetector SensorFinder = null;
    private int SensorSearchTimeOut = 60000; // in ms TimeOut

    private SmartWatchExtension Watch = null;

    private ServiceAccess Connector=null;

    private int ServiceStatus = ServiceState.Waiting;
    private Bundle SensorSnapshot = null;

    public WeatherProvider(){
        SensorSnapshot = new Bundle();
        Connector = new ServiceAccess();
    }

    private void PushSystemNotification() {
        int  Info = -1;
        if (ServiceStatus == ServiceState.Waiting) Info = R.string.WaitingMode;
        if (ServiceStatus == ServiceState.Running) Info = R.string.RunningMode;
        if (ServiceStatus == ServiceState.Searching) Info = R.string.SearchingMode;

        InfoCreator.setContentText(getText(Info));
        InfoProvider.notify(R.string.ID,InfoCreator.build());
    }

    /**************************************************************
     *  Callbacks implementation for
     *  - Sensor detection
     *  - Sensor selection
     *  - Sensor update value
     *  - Sensor disconnection
     **************************************************************/
    @Override
    public void Updated(int Value) {
        SensorSnapshot.clear();
        SensorSnapshot.putInt(WeatherStateKeys.UpdatingValue, Value);
        Watch.push(SensorSnapshot);

        Connector.Update(Value);
    }

    @Override
    public void Detected(BluetoothDevice DiscoveredSensor){
        if (DiscoveredSensor == null) return;
        SensorListener.checkDevice(DiscoveredSensor);
    }
    @Override
    public void Selected(){
        SensorFinder.stopSearch();

        ServiceStatus = ServiceState.Running;
        PushSystemNotification();

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(WeatherStateKeys.isSelected, true);
        Watch.push(SensorSnapshot);
    }

    @Override
    public void Failed(){
        ServiceStatus = ServiceState.Waiting;
        PushSystemNotification();

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(WeatherStateKeys.isSelected, false);
        Watch.push(SensorSnapshot);
    }

    @Override
    public void Removed(){
        ServiceStatus = ServiceState.Waiting;
        PushSystemNotification();

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(WeatherStateKeys.isSelected, false);
        Watch.push(SensorSnapshot);
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        InfoProvider = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        InfoCreator = new Notification.Builder(this);
        InfoCreator.setSmallIcon(R.drawable.icon_heartspy);
        InfoCreator.setContentTitle(getText(R.string.ServiceName));

        PushSystemNotification();

        SensorListener = new SensorManager(this, getBaseContext());
        SensorFinder = new SensorDetector(this, SensorSearchTimeOut);
        Watch = new SmartWatchExtension(getBaseContext());

        Connector.RegisterProvider(this);
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
     *  Callbacks implementation for incoming messages
     **************************************************************/
    @Override
    public void SearchSensor() {
        if (ServiceStatus == ServiceState.Searching) return;
        SensorFinder.startSearch();
        ServiceStatus = ServiceState.Searching;
        PushSystemNotification();

        Connector.StateChanged(ServiceStatus);
    }

    @Override
    public void Stop() {
        if (ServiceStatus == ServiceState.Searching) {
            SensorFinder.stopSearch();
            ServiceStatus = ServiceState.Waiting;
            PushSystemNotification();
            Connector.StateChanged(ServiceStatus);
        }
        if (ServiceStatus == ServiceState.Running) {
            SensorListener.disconnect();
        }
    }

    @Override
    public void Query() {
        Connector.StateChanged(ServiceStatus);
    }



}
