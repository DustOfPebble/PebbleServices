package core.services.PhoneEvents;

import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

public class PhoneEventsBind extends Binder {

    private String LogTag = PhoneEventsBind.class.getSimpleName();

    private Queries Provider = null;
    private PhoneEventsUpdates Listener = null;

    public void RegisterProvider(Queries Provider) { this.Provider = Provider; }
    public void RegisterListener(PhoneEventsUpdates Listener) { this.Listener = Listener; }

    public void query() { Provider.query(); }

    public void push(Bundle UpdateSnapshot) {
        try { Listener.push(UpdateSnapshot);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Updates event");}
    }

}
