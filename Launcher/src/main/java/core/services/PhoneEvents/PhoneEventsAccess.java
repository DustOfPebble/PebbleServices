package core.services.PhoneEvents;

import android.os.Binder;
import android.util.Log;

public class PhoneEventsAccess extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private PhoneEventsQueries Queries = null;
    private PhoneEventsUpdates Events = null;

    public void RegisterProvider(PhoneEventsQueries Provider) { Queries = Provider; }
    public void RegisterListener(PhoneEventsUpdates Listener) { Events = Listener; }

    public void Query() { Queries.Query(); }

    public void PhoneEvents(int Calls, int Messages) {
        try { Events.PhoneEvents(Calls, Messages);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Updates event");}
    }

}
