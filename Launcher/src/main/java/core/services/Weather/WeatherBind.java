package core.services.Weather;

import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

public class WeatherBind extends Binder {

    private static final String LogTag = WeatherBind.class.getSimpleName();

    private Queries Provider = null;
    private WeatherUpdates Listener = null;

    public void RegisterProvider(Queries Provider) { this.Provider = Provider; }
    public void RegisterListener(WeatherUpdates Listener) { this.Listener = Listener; }

    public void query() { Provider.query(); }

    public void push(Bundle UpdateSnapshot) {
        try { Listener.push(UpdateSnapshot);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Weather event");}
    }


}
