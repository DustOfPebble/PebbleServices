package core.services.PhoneEvents;

import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

public class PhoneEventsAccess extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private PhoneEventsQueries Provider = null;
    private PhoneEventsUpdates Listener = null;

    public void RegisterProvider(PhoneEventsQueries Provider) { this.Provider = Provider; }
    public void RegisterListener(PhoneEventsUpdates Listener) { this.Listener = Listener; }

    public void Query() { Provider.Query(); }

    public void push(Bundle UpdateSnapshot) {
        try { Listener.push(UpdateSnapshot);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Updates event");}
    }

}
