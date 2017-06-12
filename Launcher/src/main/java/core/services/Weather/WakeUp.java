package core.services.Weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

import core.services.Hub;

public class WakeUp extends BroadcastReceiver {
    private static final String LogTag = WakeUp.class.getSimpleName();

    private static int ID = 0;
    private static final String WakeUpService = "Weather";
    private IntentFilter WakeUpFilter = null;

    private AlarmManager Trigger = null;

    private PowerManager DreamBox = null;
    private PowerManager.WakeLock StayAwake = null;
    private long LifeTime = 60000; // in ms
    private static final String Coffee = "Cafeine";
    private Intent ServiceStarter;
    private Intent WakeUpEvent = null;
    private Hub Service =  null;

    public WakeUp(Hub ServiceContext){
        Service = ServiceContext;
        DreamBox = (PowerManager) Service.getSystemService(Service.POWER_SERVICE);
        Trigger = (AlarmManager) Service.getSystemService(Service.ALARM_SERVICE);
        StayAwake = DreamBox.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Coffee);

        ServiceStarter = new Intent(ServiceContext, Hub.class);

        WakeUpFilter  = new IntentFilter();
        WakeUpFilter.addAction(WakeUpService);
        Service.registerReceiver(this, WakeUpFilter);

        WakeUpEvent = new Intent(WakeUpService);
    }

    public void setNext(long SleepDelay) {
        ID++;
        PendingIntent WaitingEvent = PendingIntent.getBroadcast(Service, ID, WakeUpEvent, PendingIntent.FLAG_ONE_SHOT);
        Trigger.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SleepDelay, WaitingEvent);
        Service.Log(LogTag, "Setting a Wake Up in "+SleepDelay+"ms");
    }

    /**************************************************************
     *  Receiver called by AlarmManager
     **************************************************************/
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (!WakeUpService.equals(intent.getAction())) return;
        StayAwake.acquire(LifeTime);
        context.startService(ServiceStarter);
        Service.Log(LogTag, "Waking Up Service ["+ Hub.class.getSimpleName() +"]");
    }
}
