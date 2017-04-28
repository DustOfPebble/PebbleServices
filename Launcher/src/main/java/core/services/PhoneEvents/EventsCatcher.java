package core.services.PhoneEvents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class EventsCatcher extends BroadcastReceiver {

    private String LogTag = this.getClass().getSimpleName();

    private static final String SMS = "android.provider.Telephony.SMS_RECEIVED";
    private static final String CALL = "android.intent.action.PHONE_STATE";

    private Provider EventManager;
    private IntentFilter EventFilters = new IntentFilter();

    private int MissedCallsCount = 0;
    private int MissedMessagesCount = 0;

    private boolean hasRing = false;
    private boolean hasPickUp = false;


    public EventsCatcher(Provider Listener ) {
        EventManager = Listener;
        EventFilters.addAction(SMS);
        EventFilters.addAction(CALL);
    }

    public void enableReceiver(Context ServiceContext) {
        ServiceContext.registerReceiver(this, EventFilters);
    }

    public void resetCount() {
        MissedCallsCount = 0;
        MissedMessagesCount = 0;
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
            MissedMessagesCount++;
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
                    MissedCallsCount++;
                    Log.i(LogTag, "Missed Phone call !");
                }
                hasRing = false;
                hasPickUp = false;
            }

            Bundle PhoneInfos = new Bundle();
            PhoneInfos.putInt(Keys.MessagesID, MissedMessagesCount);
            PhoneInfos.putInt(Keys.CallsID, MissedCallsCount);
            EventManager.Update(PhoneInfos);
        }
    }
}