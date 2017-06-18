package core.services.Weather;


import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import core.services.Hub;

public class Miner {
    private static final String LogTag = Miner.class.getSimpleName();

    private Hub Listener;
    private Position GPS = null;
    private Downloader Fetcher = null;
    private CodesKeys IconOf = new CodesKeys();

    private static boolean isRunning = false;

    public Miner(Hub Parent) {
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
        Fetcher = new Downloader(this);
        String Query = Fetcher.setLocation(Longitude, Latitude);
        Fetcher.start(Query);
    }

    public void  Log(String Tag, String Message) { Listener.Log(Tag,Message); }

    // Called by Fetcher when finished and successful
    public void process(String Downloaded){
        isRunning = false;
        if (Downloaded == null) {
            Listener.Update(null);
            Listener.Log(LogTag, "Download failed !");
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
            WeatherInfo.putInt(WeatherKeys.WeatherID, WeatherID);
            WeatherInfo.putInt(WeatherKeys.TemperatureID, Temperature);
            Listener.Log(LogTag, "Weather informations processed.");
            Listener.Update(WeatherInfo);

        } catch (Exception Error) {
            Listener.Log(LogTag, "Error in JSon processing => "+ Downloaded);
            Listener.Update(new Bundle());
        }
    }
}