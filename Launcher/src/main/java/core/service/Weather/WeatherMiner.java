package core.service.Weather;

public class WeatherMiner {

    private String LogTag = this.getClass().getSimpleName();

    private WeatherProvider Listener;

    public WeatherMiner(WeatherProvider Listener) {
        this.Listener = Listener;
    }
}