package core.services.Weather;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Permission;

import core.launcher.application.ServicesKeys;

public class WeatherMiner  {

    private String LogTag = this.getClass().getSimpleName();

    private WeatherProvider Listener;
    private WeatherGPS GPS = null;
    private WeatherDownloader Downloader = null;

    public WeatherMiner(WeatherProvider Parent) {
        Listener = Parent;
        GPS = new WeatherGPS(Listener, this);
        GPS.refresh();
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
        Downloader.download(Query);
    }

    public void process(String Downloaded){
        int WeatherID = 0;
        int Temperature = 0;
        try {
            JSONObject Block = new JSONObject(Downloaded);
            JSONArray Weathers = Block.getJSONArray("weather");
            JSONObject Weather = Weathers.getJSONObject(0);
            String WeatherCode = Weather.getString("icon");
            WeatherID = ID(WeatherCode.substring(0,1));

            JSONObject Conditions = Block.getJSONObject("main");
            double Kelvin = Conditions.getDouble("temp");
            Temperature = (int)(Kelvin - 273.15);

            Bundle WeatherInfo = new Bundle();
            WeatherInfo.putInt(ServicesKeys.WeatherID, WeatherID);
            WeatherInfo.putInt(ServicesKeys.TemperatureID, Temperature);
            Listener.Update(WeatherInfo);

        } catch (Exception Error) { Error.printStackTrace();}
    }
}