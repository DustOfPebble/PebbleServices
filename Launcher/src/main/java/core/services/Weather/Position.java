package core.services.Weather;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class Position {

    private static final String LogTag = Position.class.getSimpleName();

    private Context WeatherService = null;
    private LocationManager GPS = null;

public Position(Context Service) { WeatherService =  Service; }

    @SuppressWarnings({"MissingPermission"})
    public Coordinates update() {
        if (GPS == null) GPS = (LocationManager) WeatherService.getSystemService(Context.LOCATION_SERVICE);
        if (GPS == null) return null;
        Location Updated;

        Updated = GPS.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (Updated != null) {
            return new Coordinates(Updated.getLongitude(),Updated.getLatitude());
        }
        Updated = GPS.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (Updated != null) {
            return new Coordinates(Updated.getLongitude(),Updated.getLatitude());
        }
        return null;
    }

}
