package core.launcher.application;

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
    public void push(Bundle Values) {
        if (!isWatchConnected) return;
        for (String key : Values.keySet()) {

            // Managing data from PhoneEvents Service
            if (key.equals(ServicesKeys.CallsID))
                DataSet.update(SmartwatchConstants.CallsCount, Values.getByte(key), false);
            if (key.equals(ServicesKeys.MessagesID))
                DataSet.update(SmartwatchConstants.MessagesCount, Values.getByte(key), false);

            // Managing data from Weather Service
            if (key.equals(ServicesKeys.WeatherID))
                DataSet.update(SmartwatchConstants.WeatherSkyNow, Values.getByte(key), false);
            if (key.equals(ServicesKeys.TemperatureID))
                DataSet.update(SmartwatchConstants.WeatherTemperatureNow, Values.getByte(key), true);
            if (key.equals(ServicesKeys.TemperatureMaxID))
                DataSet.update(SmartwatchConstants.WeatherTemperatureMax, Values.getByte(key), true);
            if (key.equals(ServicesKeys.TemperatureMinID))
                DataSet.update(SmartwatchConstants.WeatherTemperatureMin, Values.getByte(key), true);
            if (key.equals(ServicesKeys.LocationNameID))
                DataSet.update(SmartwatchConstants.WeatherLocationName, Values.getString(key));
        }
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean ConnectState) {
        isWatchConnected = ConnectState;
        if (!isWatchConnected) return;
        if (DataSet.size() == 0) return;
        WatchConnector.send(DataSet);
    }

}