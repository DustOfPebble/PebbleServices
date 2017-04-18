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

    public void Weather(int ID, String Name) {
        try { Events.Weather(ID, Name);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Weather event");}
    }

    public void Temperatures(double TempNow, double TemMax, double TempMin) {
        try {
            Events.Temperatures(TempNow, TemMax, TempMin); }
        catch (Exception Failed) { Log.d(LogTag, "Failed on Temperatures event");}
    }

}
