package lib.service;

import android.util.Log;

public class NotificationsCatcher {

    private String LogTag = this.getClass().getSimpleName();
    private String PackageCallManager = "com.phone";
    private String PackageMessageManager = "com.phone";

    private EventsCatcher EventManager;

    public NotificationsCatcher(EventsCatcher Listener){
        EventManager = Listener;
    }

    /********************************************************************************************
     * Callback implementation for NotificationListener
     *********************************************************************************************/
/*    @Override
    public void  onNotificationPosted(StatusBarNotification Notification) {
        String Sender = Notification.getPackageName();
        Log.d(LogTag, "Notification Posted by "+Sender);

        EventManager.Call();
        EventManager.Message();
    }
*/
}
