package core.services.Weather;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class Miner {
    private static final String LogTag = Miner.class.getSimpleName();

    private WeatherProvider Listener;
    private Position GPS = null;
    private core.services.Weather.Downloader Downloader = null;
    private CodesKeys IconOf = new CodesKeys();

    private static boolean isRunning = false;

    public Miner(WeatherProvider Parent) {
        Listener = Parent;
        GPS = new Position(Listener, this);
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        GPS.update();
    }

    private int ID(String Code) { return IconOf.Code(Code); }
    /**************************************************************
     *  Callbacks implementation from Workers
     **************************************************************/
    public void UpdateGPS(double Longitude, double Latitude) {
        Downloader = new Downloader(this);
        String Query = Downloader.setLocation(Longitude, Latitude);
        Downloader.start(Query);
    }

    // Called by Downloader when finished and successful
    public void process(String Downloaded){
        isRunning = false;
        if (Downloaded == null) {
            Listener.Update(null);
            return;
        }

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
            WeatherInfo.putInt(Keys.WeatherID, WeatherID);
            WeatherInfo.putInt(Keys.TemperatureID, Temperature);
            Listener.Update(WeatherInfo);

        } catch (Exception Error) {
            Log.d(LogTag, "Error in JSon processing => "+ Downloaded);
            Listener.Update(new Bundle());
        }
    }
}