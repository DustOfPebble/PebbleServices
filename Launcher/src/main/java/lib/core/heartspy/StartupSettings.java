package lib.core.heartspy;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import lib.service.ServiceAccess;
import lib.service.ServiceState;
import lib.service.UpdateEvents;

public class StartupSettings extends Activity implements  View.OnClickListener,UpdateEvents,ServiceConnection {

    private String LogTag = this.getClass().getSimpleName();

    private WeatherIndicator VisualIndicator = null;
    private ServiceAccess SensorService = null;
    private PermissionCollection Permissions = new PermissionCollection();

    private int ServiceMode = ServiceState.Waiting;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Instance of used HMI objects
        setContentView(R.layout.startup_settings);
        VisualIndicator = (WeatherIndicator) findViewById(R.id.beat_indicator);
        VisualIndicator.setMode(ServiceMode);
        VisualIndicator.setHeartRate(0);
        VisualIndicator.setOnClickListener(this);

        // Checking permissions
        String Requested = Permissions.Selected();
        while (Requested != null) {
            if (CheckPermission(Permissions.Selected())) Permissions.setGranted();
            Permissions.Next();
            Requested = Permissions.Selected();
        }
        String[] NotGrantedPermissions = Permissions.NotGranted();
        if (NotGrantedPermissions.length > 0) requestPermissions(NotGrantedPermissions,0);
        else StartComponents();
    }

    private void StartComponents(){
        // Start Service
        Log.d(LogTag, "Requesting Service to start...");
        Intent ServiceStarter = new Intent(this, WeatherProvider.class);
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);
    }

    /************************************************************************
     * Handler Callback to manage Click from HMI
     * **********************************************************************/
    @Override
    public void onClick(View Widget) {
        if (Widget == null) return;
        if (Widget.getId() != VisualIndicator.getId()) return;
        Log.d(LogTag, "Managing Click request...");

        if (SensorService == null) return;
        if (ServiceMode == ServiceState.Waiting) SensorService.SearchSensor();
        else SensorService.Stop();
    }

    /************************************************************************
     * Handler Callback implementation to manage update from Sensor service
     * **********************************************************************/
    @Override
    public void Update(int Value) {
        VisualIndicator.setHeartRate(Value);
    }

    @Override
    public void StateChanged(int State) {
        ServiceMode = State;
        VisualIndicator.setMode(ServiceMode);
    }
    /************************************************************************
     * Managing requested permissions at runtime
     * **********************************************************************/
    private boolean CheckPermission(String RequestedPermission) {
        if (this.checkSelfPermission(RequestedPermission) != PackageManager.PERMISSION_GRANTED)  return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults == null) { Log.d(LogTag, "Granted Permissions is undefined"); return;}
        if (grantResults.length == 0) { Log.d(LogTag, "Granted Permissions is empty"); return;}

        Log.d(LogTag, "Collecting Permissions results...");
        Boolean PermissionsGranted = true;
        for(int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) PermissionsGranted = false;
        }

        if (PermissionsGranted) StartComponents();
        else finish();
    }

    /************************************************************************
     * Managing connection to Service
     * **********************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        SensorService = (ServiceAccess)service;
        Log.d(LogTag, "Connected to SensorProvider");
        SensorService.RegisterListener(this);
        SensorService.Query();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        SensorService = null;
        Log.d(LogTag, "Disconnected from SensorProvider");
        ServiceMode = ServiceState.Waiting;
        VisualIndicator.setMode(ServiceMode);
    }

}

