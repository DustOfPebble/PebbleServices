package core.services.Weather;

import android.os.Bundle;

public interface WeatherUpdates {
    void push(Bundle UpdateSnapshot);
}
