package core.launcher.missed;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import lib.service.EventsCatcher;
import lib.service.NotificationsCatcher;
import lib.service.ServiceAccess;
import lib.service.ServiceQueries;
import lib.service.ServiceState;
import lib.smartwatch.SmartwatchEvents;

public class MissedEventsProvider extends Service implements ServiceQueries, EventsCatcher, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();

    private NotificationManager InfoProvider;
    private Notification.Builder InfoCreator;

    private NotificationsCatcher SystemListener = null;

    private SmartWatchExtension Watch = null;

    private ServiceAccess Connector=null;

    private int ServiceStatus = ServiceState.Disconnected;
    private int MissedCallsCount = 0;
    private int MissedMessagesCount =0;

    private Bundle EventSnapshot = null;

    public MissedEventsProvider(){
        EventSnapshot = new Bundle();
        Connector = new ServiceAccess();
    }

    private void PushSystemNotification() {
        int  Info = -1;
        if (ServiceStatus == ServiceState.Connected) Info = R.string.Connected;
        if (ServiceStatus == ServiceState.Disconnected) Info = R.string.Disconnected;

        InfoCreator.setContentText(getText(Info));
        InfoProvider.notify(R.string.ID,InfoCreator.build());
    }

    /**************************************************************
     *  Callbacks implementation from Notification Listener
     *  - Missed Calls
     *  - Missed Messages
     **************************************************************/
    @Override
    public void Call() {
        EventSnapshot.clear();
        MissedCallsCount++;
        EventSnapshot.putInt(MissedKey.CallsID, MissedCallsCount);
        EventSnapshot.putInt(MissedKey.MessagesID, MissedMessagesCount);
        Watch.push(EventSnapshot);

        Connector.CallsCount(MissedCallsCount);
    }

    @Override
    public void Message() {
        EventSnapshot.clear();
        MissedMessagesCount++;
        EventSnapshot.putInt(MissedKey.CallsID, MissedCallsCount);
        EventSnapshot.putInt(MissedKey.MessagesID, MissedMessagesCount);
        Watch.push(EventSnapshot);

        Connector.CallsCount(MissedMessagesCount);
    }


    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        InfoProvider = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        InfoCreator = new Notification.Builder(this);
        InfoCreator.setSmallIcon(R.drawable.missed_events);
        InfoCreator.setContentTitle(getText(R.string.ServiceName));

        PushSystemNotification();

        SystemListener = new NotificationsCatcher(this);
        Connector.RegisterProvider(this);

        Watch = new SmartWatchExtension(getBaseContext());
        ServiceStatus = (Watch.isConnected()? ServiceState.Connected:ServiceState.Disconnected);
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
        Connector.CallsCount(MissedCallsCount);
        Connector.MessagesCount(MissedMessagesCount);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean ConnectState) {
        if (ConnectState == false) {
            MissedCallsCount = 0;
            MissedMessagesCount = 0;
            ServiceStatus = ServiceState.Disconnected;
            PushSystemNotification();
            return;
        }

        ServiceStatus = ServiceState.Connected;
        PushSystemNotification();
        return;
    }


}
