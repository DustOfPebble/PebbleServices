package core.launcher.application;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import core.services.PhoneEvents.Provider;
import core.services.PhoneEvents.Bind;
import core.services.PhoneEvents.Updates;
import core.services.PhoneEvents.Keys;
import core.services.Weather.CodesKeys;

public class StartupSettings extends Activity implements Updates, core.services.Weather.Updates,ServiceConnection, Runnable {

    private String LogTag = this.getClass().getSimpleName();

    private TextView CallsCounter = null;
    private TextView MessagesCounter = null;
    private ImageView WeatherIcon = null;
    private TextView Temperature = null;

    private Handler ViewUpdate = new Handler(Looper.getMainLooper());
    private Bundle UpdateContent = null;

    private Bind PhoneEventsService = null;
    private core.services.Weather.Bind WeatherService = null;

    private PermissionHelper Permissions = new PermissionHelper();
    private boolean PermissionsChecked = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Instance of used HMI objects
        setContentView(R.layout.services_states);
        CallsCounter = (TextView) findViewById(R.id.CallsCount);
        MessagesCounter = (TextView) findViewById(R.id.MessagesCount);
        WeatherIcon = (ImageView) findViewById(R.id.icon_weather);
        Temperature = (TextView) findViewById(R.id.TempValue);

        // Checking permissions
        Permissions.Append(Manifest.permission.RECEIVE_SMS);
        Permissions.Append(Manifest.permission.READ_PHONE_STATE);
        Permissions.Append(Manifest.permission.INTERNET);
        Permissions.Append(Manifest.permission.ACCESS_NETWORK_STATE);
        Permissions.Append(Manifest.permission.CHANGE_NETWORK_STATE);
        Permissions.Append(Manifest.permission.ACCESS_FINE_LOCATION);
        Permissions.Append(Manifest.permission.ACCESS_COARSE_LOCATION);
        Permissions.Append(Manifest.permission.WAKE_LOCK);

        String Requested = Permissions.Selected();
        while (Requested != null) {
            if (CheckPermission(Permissions.Selected())) Permissions.setGranted();
            Permissions.Next();
            Requested = Permissions.Selected();
        }
        String[] NotGrantedPermissions = Permissions.NotGranted();
        if (NotGrantedPermissions.length > 0) requestPermissions(NotGrantedPermissions,0);
        else PermissionsChecked = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        StartServices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (PhoneEventsService == null) return;
        if (WeatherService == null) return;
        unbindService(this);
        Log.d(LogTag, "Closing connection with Services ...");
    }

    private void StartServices(){
        if (!PermissionsChecked) return;

        Intent ServiceStarter;
        // Start Service
        ServiceStarter = new Intent(this, Provider.class);
        Log.d(LogTag, "Requesting Service ["+ Provider.class.getSimpleName() +"] to start...");
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);

        ServiceStarter = new Intent(this, core.services.Weather.Provider.class);
        Log.d(LogTag, "Requesting Service ["+ core.services.Weather.Provider.class.getSimpleName() +"] to start...");
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);
    }

     /************************************************************************
     * Managing requested permissions at runtime
     * **********************************************************************/
    private boolean CheckPermission(String RequestedPermission) {
        return this.checkSelfPermission(RequestedPermission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(LogTag, "Collecting Permissions results...");

        boolean PermissionsGranted = true;
        for(int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                PermissionsGranted = false;
                Log.d(LogTag, "Permission:"+permissions[i]+" is not granted !");
            }
        }

        if (!PermissionsGranted) finish();
        PermissionsChecked = true;
        StartServices();
    }

    /************************************************************************
     * Managing connection to Service
     * **********************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LogTag, "Connected to " + name.getClassName() + " Service");

        // Connection from push Service
        if (Provider.class.getName().equals(name.getClassName())) {
            PhoneEventsService = (Bind) service;
            PhoneEventsService.RegisterListener(this);
            PhoneEventsService.query();
        }

        // Connection from push Service
        if (core.services.Weather.Provider.class.getName().equals(name.getClassName())) {
            WeatherService = (core.services.Weather.Bind) service;
            WeatherService.RegisterListener(this);
            WeatherService.query();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LogTag, "Disconnected from " + name.getClassName()  + " Service");

        // Disconnection from push Service
        if (Provider.class.getName().equals(name.getClassName())) {
            PhoneEventsService = null;
        }

        // Disconnection from push Service
        if (core.services.Weather.Provider.class.getName().equals(name.getClassName())) {
            WeatherService = null;
        }
    }

    /************************************************************************
     * Callback implementation to manage update from push Services
     * **********************************************************************/
    @Override
    public void push(Bundle UpdateSnapshot) {
        UpdateContent =  UpdateSnapshot;
        ViewUpdate.post(this);
    }

    /************************************************************************
     * CallED by HMI main thread
     * **********************************************************************/
    @Override
    public void run() {
        for (String key : UpdateContent.keySet()) {

            // Managing data from push Service
            if (key.equals(Keys.CallsID)) CallsCounter.setText(String.valueOf(UpdateContent.getInt(key)));
            if (key.equals(Keys.MessagesID)) MessagesCounter.setText(String.valueOf(UpdateContent.getInt(key)));


            // Managing data from Weather Service
            if (key.equals(core.services.Weather.Keys.WeatherID)) {
                int WeatherID = UpdateContent.getInt(key);
                if (WeatherID == CodesKeys.SunnyID)  WeatherIcon.setImageResource(R.drawable.sunny);
                if (WeatherID == CodesKeys.CloudyID)  WeatherIcon.setImageResource(R.drawable.cloudy);
                if (WeatherID == CodesKeys.RainyID)  WeatherIcon.setImageResource(R.drawable.rainy);
                if (WeatherID == CodesKeys.SunnyRainyID)  WeatherIcon.setImageResource(R.drawable.sunny_rainy);
                if (WeatherID == CodesKeys.SunnyCloudyID)  WeatherIcon.setImageResource(R.drawable.sunny_cloudy);
                if (WeatherID == CodesKeys.StormyID)  WeatherIcon.setImageResource(R.drawable.stormy);
                if (WeatherID == CodesKeys.SnowyID)  WeatherIcon.setImageResource(R.drawable.snowy);
            }
            if (key.equals(core.services.Weather.Keys.TemperatureID)) Temperature.setText(String.valueOf(UpdateContent.getInt(key))+"°c");
        }
    }
}

