package core.launcher.application;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import core.services.PhoneEvents.PhoneEventsProvider;
import core.services.PhoneEvents.PhoneEventsAccess;
import core.services.PhoneEvents.PhoneEventsUpdates;
import core.services.Weather.WeatherAccess;
import core.services.Weather.WeatherProvider;
import core.services.Weather.WeatherUpdates;

public class StartupSettings extends Activity implements PhoneEventsUpdates, WeatherUpdates,ServiceConnection {

    private String LogTag = this.getClass().getSimpleName();

    private TextView CallsCounter = null;
    private TextView MessagesCounter = null;

    private PhoneEventsAccess PhoneEventsService = null;
    private WeatherAccess WeatherService = null;

    private PermissionHelper Permissions = new PermissionHelper();
    private boolean PermissionsChecked = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Instance of used HMI objects
        setContentView(R.layout.startup_settings);
        CallsCounter = (TextView) findViewById(R.id.CallsCount);
        MessagesCounter = (TextView) findViewById(R.id.MessagesCount);

        // Checking permissions
        Permissions.Append(Manifest.permission.RECEIVE_SMS);
        Permissions.Append(Manifest.permission.READ_PHONE_STATE);
        Permissions.Append(Manifest.permission.INTERNET);
        Permissions.Append(Manifest.permission.ACCESS_NETWORK_STATE);
        Permissions.Append(Manifest.permission.ACCESS_FINE_LOCATION);
        Permissions.Append(Manifest.permission.ACCESS_COARSE_LOCATION);

        String Requested = Permissions.Selected();
        while (Requested != null) {
            if (CheckPermission(Permissions.Selected())) Permissions.setGranted();
            Permissions.Next();
            Requested = Permissions.Selected();
        }
        String[] NotGrantedPermissions = Permissions.NotGranted();
        if (NotGrantedPermissions.length > 0) requestPermissions(NotGrantedPermissions,0);
        else StartServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionsChecked) StartServices();
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
        PermissionsChecked = true;
        Intent ServiceStarter;

        // Start Service
        ServiceStarter = new Intent(this, PhoneEventsProvider.class);
        Log.d(LogTag, "Requesting Service ["+ PhoneEventsProvider.class.getSimpleName() +"] to start...");
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);

        ServiceStarter = new Intent(this, WeatherProvider.class);
        Log.d(LogTag, "Requesting Service ["+ WeatherProvider.class.getSimpleName() +"] to start...");
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

        Boolean PermissionsGranted = true;
        for(int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                PermissionsGranted = false;
                Log.d(LogTag, "Permission:"+permissions[i]+" is not granted !");
            }
        }

        if (PermissionsGranted) StartServices();
        else finish();
    }

    /************************************************************************
     * Managing connection to Service
     * **********************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LogTag, "Connected to " + name.getClassName() + " Service");

        // Connection from push Service
        if (PhoneEventsProvider.class.getName().equals(name.getClassName())) {
            PhoneEventsService = (PhoneEventsAccess) service;
            PhoneEventsService.RegisterListener(this);
            PhoneEventsService.query();
        }

        // Connection from push Service
        if (WeatherProvider.class.getName().equals(name.getClassName())) {
            WeatherService = (WeatherAccess) service;
            WeatherService.RegisterListener(this);
            WeatherService.query();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LogTag, "Disconnected from " + name.getClassName()  + " Service");

        // Disconnection from push Service
        if (PhoneEventsProvider.class.getName().equals(name.getClassName())) {
            PhoneEventsService = null;
        }

        // Disconnection from push Service
        if (WeatherProvider.class.getName().equals(name.getClassName())) {
            WeatherService = null;
        }
    }

    /************************************************************************
     * Callback implementation to manage update from push Services
     * **********************************************************************/
    @Override
    public void push(Bundle UpdateSnapshot) {
        for (String key : UpdateSnapshot.keySet()) {

            // Managing data from push Service
            if (key.equals(ServicesKeys.CallsID)) CallsCounter.setText(String.valueOf(UpdateSnapshot.getInt(key)));
            if (key.equals(ServicesKeys.MessagesID)) MessagesCounter.setText(String.valueOf(UpdateSnapshot.getInt(key)));


            // Managing data from Weather Service
//            if (key.equals(ServicesKeys.WeatherID))
//            if (key.equals(ServicesKeys.TemperatureID))
        }
    }

}

