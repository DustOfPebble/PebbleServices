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

import core.service.PhoneEvents.PhoneEventsProvider;
import core.service.PhoneEvents.PhoneEventsAccess;
import core.service.PhoneEvents.PhoneEventsUpdates;

public class StartupSettings extends Activity implements PhoneEventsUpdates,ServiceConnection {

    private String LogTag = this.getClass().getSimpleName();

    private TextView CallsCounter = null;
    private TextView MessagesCounter = null;
    private PhoneEventsAccess PhoneEventsService = null;


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
        Intent ServiceStarter = new Intent(this, PhoneEventsProvider.class);
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);
    }

    /************************************************************************
    * Handler Callback implementation to manage update from Services Events
    * **********************************************************************/

    @Override
    public void CallsCount(int Count) { CallsCounter.setText(String.valueOf(Count)); }

    @Override
    public void MessagesCount(int Count) { MessagesCounter.setText(String.valueOf(Count)); }

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

        if (PermissionsGranted) StartComponents();
        else finish();
    }

    /************************************************************************
     * Managing connection to Service
     * **********************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LogTag, "Connected to " + name + " Service");
        PhoneEventsService = (PhoneEventsAccess)service;
        PhoneEventsService.RegisterListener(this);
        PhoneEventsService.Query();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LogTag, "Disconnected from " + name + " Service");
        PhoneEventsService = null;
    }

}

