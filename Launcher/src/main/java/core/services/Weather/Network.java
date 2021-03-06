package core.services.Weather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.OnNetworkActiveListener;
import android.net.NetworkInfo;

import core.services.Hub;

public class Network implements OnNetworkActiveListener {

    private static final String LogTag = Network.class.getSimpleName();
    private Hub Service = null;
    private ConnectivityManager Connectivity = null;

    public Network(Hub Service) {
        this.Service = Service;
        Connectivity = (ConnectivityManager) Service.getSystemService(Context.CONNECTIVITY_SERVICE);
        Connectivity.addDefaultNetworkActiveListener(this);
    }

    public boolean isConnected() {
        NetworkInfo Status = Connectivity.getActiveNetworkInfo();
        if (Status == null) return false;
        return Status.isConnectedOrConnecting();
    }

    @Override
    public void onNetworkActive() {
//        Service.Log(LogTag, "Network Listener called !" );
        NetworkInfo Status = Connectivity.getActiveNetworkInfo();
        if (Status == null) return;
        if (!Status.isConnectedOrConnecting()) return;
        Service.ConnectivityEnabled();
    }
}
