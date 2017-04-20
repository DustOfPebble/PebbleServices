package core.services.Weather;


import android.util.Log;

import java.security.Permission;

public class WeatherMiner  {

    private String LogTag = this.getClass().getSimpleName();

    static private  String Sample = "http://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=b1b15e88fa797225412429c1c50c122a1";

    private WeatherProvider Listener;
    private WeatherGPS GPS = null;
    private WeatherDownloader Downloader = null;

    public WeatherMiner(WeatherProvider Parent) {
        Listener = Parent;
        Downloader = new WeatherDownloader(Listener, this);
        GPS = new WeatherGPS(Listener, this);
        GPS.refresh();
    }

    /**************************************************************
     *  Callbacks implementation from Workers
     **************************************************************/
    public void UpdateGPS(double Longitude, double Latitude) {
        String Query = Downloader.setLocation(Longitude, Latitude);
        Downloader.download(Query);
    }

    public void process(String Downloaded){
        if (Downloaded != null) Log.d(LogTag, "JSON => "+Downloaded);
 //       Downloader.interrupt();
    }
}