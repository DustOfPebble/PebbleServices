package lib.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class NotificationsCatcher extends BroadcastReceiver {

    private String LogTag = this.getClass().getSimpleName();

    private static final String SMS = "android.provider.Telephony.SMS_RECEIVED";
    private static final String CALL = "android.intent.action.PHONE_STATE";

    private EventsCatcher EventManager;
    private IntentFilter EventFilters= new IntentFilter();

    public NotificationsCatcher(EventsCatcher Listener, Context ServiceContext){
        EventManager = Listener;
        EventFilters.addAction(SMS);
        EventFilters.addAction(CALL);
        ServiceContext.registerReceiver(this,EventFilters);
    }

    /********************************************************************************************
     * Broadcast Receiver implementation for SMS and Calls
     *********************************************************************************************/
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String Action = intent.getAction();
        if (SMS.equals(Action)) {
            Log.i(LogTag, "SMS received.");
            EventManager.Message();
            return;
        }

        if (CALL.equals(Action)) {
            Log.i(LogTag, "Call received.");
            EventManager.Call();
            return;
        }

        Log.i(LogTag, "Received Action:" + Action);
    }
}
