package core.services.Weather;

import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

public class WeatherAccess extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private WeatherQueries Provider = null;
    private WeatherUpdates Listener = null;

    public void RegisterProvider(WeatherQueries Provider) { this.Provider = Provider; }
    public void RegisterListener(WeatherUpdates Listener) { this.Listener = Listener; }

    public void query() { Provider.query(); }

    public void push(Bundle UpdateSnapshot) {
        try { Listener.push(UpdateSnapshot);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Weather event");}
    }


}
