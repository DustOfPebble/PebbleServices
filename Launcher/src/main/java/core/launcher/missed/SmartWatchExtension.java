package core.launcher.missed;

import android.content.Context;
import android.os.Bundle;

import lib.smartwatch.SmartwatchBundle;
import lib.smartwatch.SmartwatchEvents;
import lib.smartwatch.SmartwatchManager;

/******************************************************************************
 *  This belongs to main app because it have to customized
 *      - Key values
 *      - Values types
 ******************************************************************************/
public class SmartWatchExtension implements SmartwatchEvents {

    private SmartwatchManager WatchConnector = null;
    private SmartwatchBundle DataSet = null;
    private Boolean isWatchConnected = false;

    public SmartWatchExtension(Context context) {
        WatchConnector = new SmartwatchManager(context,this, SmartwatchConstants.WatchUUID);
        isWatchConnected = WatchConnector.isConnected();
        DataSet = new SmartwatchBundle();
    }

    public boolean isConnected() {
        return isWatchConnected;
    }

    // On receive we do nothing because we are disconnected ...
    void push(Bundle Values) {
        if (!isWatchConnected) return;
        for (String key : Values.keySet()) {
            if (key == MissedKey.CallsID)
                DataSet.update(SmartwatchConstants.MissedCalls, Values.getInt(key));
            if (key == MissedKey.MessagesID)
                DataSet.update(SmartwatchConstants.MissedMessages, Values.getInt(key));
        }
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean ConnectState) {
        isWatchConnected = ConnectState;
        if (isWatchConnected == false) return;
        if (DataSet.size() == 0) return;
        WatchConnector.send(DataSet);
    }

}
