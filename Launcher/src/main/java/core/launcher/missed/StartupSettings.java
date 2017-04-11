package core.launcher.missed;

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

import java.security.Permission;

import lib.service.ServiceAccess;
import lib.service.ServiceEvents;

public class StartupSettings extends Activity implements  ServiceEvents,ServiceConnection {

    private String LogTag = this.getClass().getSimpleName();

    private TextView CallsCounter = null;
    private TextView MessagesCounter = null;
    private ServiceAccess EventsService = null;
    private PermissionHelper Permissions = new PermissionHelper();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Instance of used HMI objects
        setContentView(R.layout.startup_settings);
        CallsCounter = (TextView) findViewById(R.id.CallsCount);
        MessagesCounter = (TextView) findViewById(R.id.MessagesCount);

        // Checking permissions
        Permissions.Append(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        Permissions.Append(Manifest.permission.RECEIVE_SMS);
        Permissions.Append(Manifest.permission.READ_PHONE_STATE);

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
        Intent ServiceStarter = new Intent(this, MissedEventsProvider.class);
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);
    }

     /************************************************************************
     * Handler Callback implementation to manage update from Sensor service
     * **********************************************************************/
    @Override
    public void CallsCount(int Count) { CallsCounter.setText(String.valueOf(Count)); }

    @Override
    public void MessagesCount(int Count) { MessagesCounter.setText(String.valueOf(Count)); }

    /************************************************************************
     * Managing requested permissions at runtime
     * **********************************************************************/
    private boolean CheckPermission(String RequestedPermission) {
        if (this.checkSelfPermission(RequestedPermission) != PackageManager.PERMISSION_GRANTED)  return false;
        return true;
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

        if (PermissionsGranted) StartComponents();
        else finish();
    }

    /************************************************************************
     * Managing connection to Service
     * **********************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        EventsService = (ServiceAccess)service;
        Log.d(LogTag, "Connected to Missed Events Service");
        EventsService.RegisterListener(this);
        EventsService.Query();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        EventsService = null;
        Log.d(LogTag, "Disconnected from Missed Events Service");
    }

}

