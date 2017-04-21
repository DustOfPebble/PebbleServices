package core.services.Weather;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;

public class WeatherGPS implements Runnable {

    private WeatherMiner Listener = null;
    private Context WeatherService = null;
    private LocationManager GPS = null;
    private Handler TrigGPS = new Handler();
    private int RefreshDelay = 5*60*1000; // in ms

    public WeatherGPS(Context Service, WeatherMiner Parent) {
        Listener = Parent;
        WeatherService =  Service;
        GPS = (LocationManager) Service.getSystemService(Context.LOCATION_SERVICE);
    }

    public void start() {
        TrigGPS.postDelayed(this, 0);
    }

    public void stop() {
        TrigGPS.removeCallbacks(this);
    }

    /****************************************************************
     * Callback implementation for Runnable for querying Position
     ****************************************************************/
    @SuppressWarnings({"MissingPermission"})
    @Override
    public void run() {
        TrigGPS.postDelayed(this, RefreshDelay);

        if (GPS == null)  GPS = (LocationManager) WeatherService.getSystemService(Context.LOCATION_SERVICE);
        if (GPS == null) return;

        Location Updated;

        Updated = GPS.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (Updated != null) {
            Listener.UpdateGPS(Updated.getLongitude(),Updated.getLatitude());
            return;
        }
        Updated = GPS.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (Updated != null) {
            Listener.UpdateGPS(Updated.getLongitude(),Updated.getLatitude());
            return;
        }
    }
}
