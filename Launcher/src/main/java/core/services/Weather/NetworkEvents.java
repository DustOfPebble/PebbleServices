package core.services.Weather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.OnNetworkActiveListener;
import android.net.NetworkInfo;

public class NetworkEvents implements OnNetworkActiveListener {

    private String LogTag = this.getClass().getSimpleName();
    private  WeatherProvider Service = null;
    private ConnectivityManager Connectivity = null;

    public NetworkEvents(WeatherProvider Service) {
        this.Service = Service;
        Connectivity = (ConnectivityManager) Service.getSystemService(Context.CONNECTIVITY_SERVICE);
        Connectivity.addDefaultNetworkActiveListener(this);
    }

    public boolean isConnected() {
        NetworkInfo Status = Connectivity.getActiveNetworkInfo();
        if (Status == null) return false;
        return Status.isConnected();
    }

    @Override
    public void onNetworkActive() {
        NetworkInfo Status = Connectivity.getActiveNetworkInfo();
        if (Status == null) return;
        if (!Status.isConnected()) return;
        Service.Enabled();
    }
}
