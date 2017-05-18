package core.launcher.application;

import java.util.HashMap;

import core.services.Weather.CodesKeys;

public class Icons extends HashMap{
    public Icons() {
        put(CodesKeys.NoWeatherID, R.drawable.no_weather);
        put(CodesKeys.SunnyID,R.drawable.sunny);
        put(CodesKeys.CloudyID,R.drawable.cloudy);
        put(CodesKeys.RainyID,R.drawable.rainy);
        put(CodesKeys.SunnyRainyID,R.drawable.sunny_rainy);
        put(CodesKeys.SunnyCloudyID,R.drawable.sunny_cloudy);
        put(CodesKeys.StormyID,R.drawable.stormy);
        put(CodesKeys.SnowyID,R.drawable.snowy);
        put(CodesKeys.FoggyID, R.drawable.foggy);
    }
}
