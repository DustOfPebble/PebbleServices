package core.services.Weather;

import android.os.Binder;
import android.util.Log;

public class WeatherAccess extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private WeatherQueries Queries = null;
    private WeatherUpdates Events = null;

    public void RegisterProvider(WeatherQueries Provider) { Queries = Provider; }
    public void RegisterListener(WeatherUpdates Listener) { Events = Listener; }

    public void Query() { Queries.Query(); }

    public void Weather(int ID, int Temp) {
        try { Events.Weather(ID, Temp);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Weather event");}
    }


}
