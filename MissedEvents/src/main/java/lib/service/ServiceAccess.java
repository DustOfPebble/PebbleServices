package lib.service;

import android.os.Binder;
import android.util.Log;

public class ServiceAccess extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private ServiceQueries Queries = null;
    private ServiceEvents Events = null;

    public void RegisterProvider(ServiceQueries Provider) { Queries = Provider; }
    public void RegisterListener(ServiceEvents Listener) { Events = Listener; }

    public void Query() { Queries.Query(); }

    public void CallsCount(int Count) {
        try { Events.CallsCount(Count);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on MissedCalls event");}
    }

    public void MessagesCount(int Count) {
        try {
            Events.MessagesCount(Count); }
        catch (Exception Failed) { Log.d(LogTag, "Failed on MessagesCount event");}
    }

}
