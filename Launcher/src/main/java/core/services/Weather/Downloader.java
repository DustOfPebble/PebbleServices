package core.services.Weather;

import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader extends Thread  {
    private static final String LogTag = Downloader.class.getSimpleName();

    static private  String Server = "api.openweathermap.org";
    static private  String Command = "/data/2.5/weather?";
    static private  String KeyAPI ="3d28c03d7fb2f5f4b4b7bc98367cc2cd";

    private Miner Listener = null;
    private String WeatherURL = null;
    private int maxLength = 10000; // in Bytes

    public Downloader(Miner Parent) {
        Listener = Parent;
    }

    public String setLocation(Coordinates KnownPosition){
        /********************************************************************************************
         * query template : api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={key}
         ********************************************************************************************/
        String Query = "http://";
        Query +=  Server;
        Query +=  Command;
        Query += "lat="+String.valueOf(KnownPosition.Latitude)+ "&lon="+String.valueOf(KnownPosition.Longitude);
        Query += "&appid="+KeyAPI;
        Log.d(LogTag,"Download :["+Query+"]");
        return  Query;
    }

    public void start(String Query) {
        WeatherURL = Query;
        this.start();
    }

    /**********************************************************************************************
     * Thread Main process
     **********************************************************************************************/
    @Override
    public void run(){
        String result = null;
        InputStream stream;
        HttpURLConnection connection;
        try {
            URL url = new URL(WeatherURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            stream = connection.getInputStream();
            if (stream != null) {
                InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
                char[] buffer = new char[maxLength];
                int numChars = 0;
                int readSize = 0;
                while (numChars < maxLength && readSize != -1) {
                    numChars += readSize;
                    int pct = (100 * numChars) / maxLength;
                    readSize = reader.read(buffer, numChars, buffer.length - numChars);
                }
                if (numChars != -1) {
                    numChars = Math.min(numChars, maxLength);
                    result = new String(buffer, 0, numChars);
                }
            }
            stream.close();
            connection.disconnect();
            Listener.Log(LogTag, "Download Succeed ["+result.length()+" Bytes]");
            Listener.process(result);
        } catch (Exception Error) {
            Listener.Log(LogTag, "Download Failed ["+Error.getMessage()+"]");
            Listener.process(null);
        }
    }
}



