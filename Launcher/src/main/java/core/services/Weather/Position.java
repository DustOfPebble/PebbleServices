package core.services.Weather;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class Position {

    private static final String LogTag = Position.class.getSimpleName();

    private Miner Listener = null;
    private Context WeatherService = null;
    private LocationManager GPS = null;

public Position(Context Service, Miner Parent) {
        Listener = Parent;
        WeatherService =  Service;
    }

    @SuppressWarnings({"MissingPermission"})
    public void update() {
        if (GPS == null) GPS = (LocationManager) WeatherService.getSystemService(Context.LOCATION_SERVICE);
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
