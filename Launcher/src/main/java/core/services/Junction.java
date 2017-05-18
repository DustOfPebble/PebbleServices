package core.services;

import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

public class Junction extends Binder {

    private static final String LogTag = Junction.class.getSimpleName();

    private Queries Provider = null;
    private Signals Listener = null;

    public void RegisterProvider(Queries Provider) { this.Provider = Provider; }
    public void RegisterListener(Signals Listener) { this.Listener = Listener; }

    public void query() { Provider.query(); }

    public void push(Bundle UpdateSnapshot) {
        try { Listener.push(UpdateSnapshot);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Weather event");}
    }


}
