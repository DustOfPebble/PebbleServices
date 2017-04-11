package lib.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NotificationsCatcher extends BroadcastReceiver {

    private String LogTag = this.getClass().getSimpleName();

    private static final String SMS = "android.provider.Telephony.SMS_RECEIVED";
    private static final String CALL = "android.intent.action.PHONE_STATE";

    private EventsCatcher EventManager;
    private IntentFilter EventFilters = new IntentFilter();

    private boolean hasRing = false;
    private boolean hasPickUp = false;


    public NotificationsCatcher(EventsCatcher Listener, Context ServiceContext) {
        EventManager = Listener;
        EventFilters.addAction(SMS);
        EventFilters.addAction(CALL);
        ServiceContext.registerReceiver(this, EventFilters);
    }

    /********************************************************************************************
     * Broadcast Receiver implementation for SMS and Calls
     *********************************************************************************************/
    @Override
    public void onReceive(Context context, Intent PhoneEvent) {
        if (PhoneEvent == null) return;
        String Action = PhoneEvent.getAction();
        if (SMS.equals(Action)) {
            Log.i(LogTag, "SMS received.");
            EventManager.Message();
            return;
        }

        if (CALL.equals(Action)) {
            String PhoneState = PhoneEvent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (PhoneState == null) {
                return;
            }
            if (PhoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                hasRing = true;
                return;
            }
            if (PhoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                hasPickUp = true;
                return;
            }

            if (PhoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (hasRing && !hasPickUp) {
                    EventManager.Call();
                    Log.i(LogTag, "Missed Phone call !");
                }
                hasRing = false;
                hasPickUp = false;
            }
        }
    }
}