package core.service.Weather;

public interface WeatherUpdates {
    void Weather(int IconID, String LocationName);
    void Temperatures(double Now, double Max, double Min);
}
