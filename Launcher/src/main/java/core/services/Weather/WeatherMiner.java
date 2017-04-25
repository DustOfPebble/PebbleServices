package core.services.Weather;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherMiner  {

    private String LogTag = this.getClass().getSimpleName();

    private WeatherProvider Listener;
    private WeatherGPS GPS = null;
    private WeatherDownloader Downloader = null;

    private static boolean isRunning = false;

    public WeatherMiner(WeatherProvider Parent) {
        Listener = Parent;
        GPS = new WeatherGPS(Listener, this);
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        GPS.update();
    }

    private int ID(String Code) {
        if  (WeatherCode.Sunny.equals(Code)) return WeatherCode.SunnyID;
        if  (WeatherCode.SunnyCloudy.equals(Code)) return WeatherCode.SunnyCloudyID;
        if  (WeatherCode.Cloudy.equals(Code)) return WeatherCode.CloudyID;
        if  (WeatherCode.HeavyCloudy.equals(Code)) return WeatherCode.HeavyCloudyID;
        if  (WeatherCode.Rainy.equals(Code)) return WeatherCode.RainyID;
        if  (WeatherCode.SunnyRainy.equals(Code)) return WeatherCode.SunnyRainyID;
        if  (WeatherCode.Stormy.equals(Code)) return WeatherCode.StormyID;
        if  (WeatherCode.Snowy .equals(Code)) return WeatherCode.SnowyID;
        return 0;
    }
    /**************************************************************
     *  Callbacks implementation from Workers
     **************************************************************/
    public void UpdateGPS(double Longitude, double Latitude) {
        Downloader = new WeatherDownloader(this);
        String Query = Downloader.setLocation(Longitude, Latitude);
        Downloader.start(Query);
    }

    // Called by Downloader when finished and successful
    public void process(String Downloaded){
        isRunning = false;
        if (Downloaded == null) return;

        int WeatherID = 0;
        int Temperature = 0;
        try {
            JSONObject Block = new JSONObject(Downloaded);
            JSONArray Weathers = Block.getJSONArray("weather");
            JSONObject Weather = Weathers.getJSONObject(0);
            String WeatherCode = Weather.getString("icon");
            WeatherID = ID(WeatherCode.substring(0,2));

            JSONObject Conditions = Block.getJSONObject("main");
            double Kelvin = Conditions.getDouble("temp");
            Temperature = (int)(Kelvin - 273.15);

            Bundle WeatherInfo = new Bundle();
            WeatherInfo.putInt(WeatherKeys.WeatherID, WeatherID);
            WeatherInfo.putInt(WeatherKeys.TemperatureID, Temperature);
            Listener.Update(WeatherInfo);

        } catch (Exception Error) { Log.d(LogTag, "Error in JSon processing => "+ Downloaded);}
    }
}