package core.services.Weather;

import android.util.Log;

import java.util.HashMap;

public class CodesKeys extends HashMap {
/*********************************************************
 * Includes from Smartwatch application
 * #define NoWeather 0
 * #define Sunny 1
 * #define SunnyCloudy 2
 * #define Cloudy 3
 * #define Rainy 4
 * #define Stormy 5
 * #define SunnyRainy 6
 * #define Snowy 7
 * #define Foggy 8
 * *********************************************************/
    private static final String LogTag = CodesKeys.class.getSimpleName();

    static final String Sunny = "01";
    static final String SunnyCloudy = "02";
    static final String Cloudy = "03";
    static final String HeavyCloudy = "04";
    static final String Rainy = "09";
    static final String Stormy = "11";
    static final String SunnyRainy = "10";
    static final String Snowy = "12";
    static final String Foggy = "50";

    static public final int NoWeatherID = 0;
    static public final int SunnyID = 1;
    static public final int SunnyCloudyID = 2;
    static public final int CloudyID = 3;
    static public final int HeavyCloudyID = 3;
    static public final int RainyID = 4;
    static public final int StormyID = 5;
    static public final int SunnyRainyID = 6;
    static public final int SnowyID = 7;
    static public final int FoggyID = 8;

    CodesKeys() {
        this.put(Sunny, SunnyID);
        this.put(SunnyCloudy, SunnyCloudyID);
        this.put(Cloudy, CloudyID);
        this.put(HeavyCloudy, HeavyCloudyID);
        this.put(Rainy, RainyID);
        this.put(Stormy, StormyID);
        this.put(SunnyRainy, SunnyRainyID);
        this.put(Snowy, SnowyID);
        this.put(Foggy, FoggyID);
    }

    public int Code(String WeatherCode) {
        int Id = NoWeatherID;
        if (containsKey(WeatherCode)) Id = (int)get(WeatherCode);
        Log.d(LogTag, "Matching  Weather Code ["+WeatherCode+"] => {"+Id+"}");
        return Id;
    }
}
