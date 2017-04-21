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

    public void refresh() {
        refreshLocation();
    }

    private void processLocation(Location Here) {
        Listener.UpdateGPS(Here.getLongitude(),Here.getLatitude());
    }

    @SuppressWarnings({"MissingPermission"})
    private void refreshLocation(){

        TrigGPS.postDelayed(this, RefreshDelay);

        if (GPS == null)  GPS = (LocationManager) WeatherService.getSystemService(Context.LOCATION_SERVICE);
        if (GPS == null) return;

        Location Updated;

        Updated = GPS.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (Updated != null) {
            processLocation(Updated);
            return;
        }
        Updated = GPS.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (Updated != null) {
            processLocation(Updated);
            return;
        }
    }

    /****************************************************************
     * Callback implementation for Runnable for querying Position
     ****************************************************************/
    @Override
    public void run() { refreshLocation(); }

}
