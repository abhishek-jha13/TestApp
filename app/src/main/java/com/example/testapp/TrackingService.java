package com.example.testapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.example.testapp.models.UserLocation;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.IntentFilter;
import android.util.Log;
import android.Manifest;
import android.location.Location;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.app.PendingIntent;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.Map;

public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        loginToFirebase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(!MainActivity.isUserExitingTracking){
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, RestartTrackingService.class);
            this.sendBroadcast(broadcastIntent);
        }

    }

//Create the persistent notification//

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder;
        int foregroundId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getNotificationChannel(notificationManager);
            // Create the persistent notification//
            builder = new Notification.Builder(this, channelId);
            foregroundId = 110;
        }else {
            // Create the persistent notification//
            builder = new Notification.Builder(this);
            foregroundId = 1;
        }
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))

//Make this notification ongoing so it can’t be dismissed by the user//

                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.tracking_enabled);
        startForeground(foregroundId, builder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String getNotificationChannel(NotificationManager notificationManager) {
        String channelId = "channelid";
        String channelName = getResources().getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//Unregister the BroadcastReceiver when the notification is tapped//

            unregisterReceiver(stopReceiver);

//Stop the Service//

            MainActivity.isUserExitingTracking = true;
            stopSelf();
        }
    };

    private void loginToFirebase() {

        requestLocationUpdates();


    }

//Initiate the request to track the device's location//

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

//Specify how often your app should request the device’s location//

        request.setInterval(60000);

//Specify fastest interval

        request.setFastestInterval(10000);

//Specify Max Wait Time

        request.setMaxWaitTime(180000);

//Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = getString(R.string.firebase_path);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

//If the app currently has access to the location permission...//

        if (permission == PackageManager.PERMISSION_GRANTED) {

//...then request location updates//

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

//Get a reference to the database, so your app can perform read and write operations//

                    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Location location = locationResult.getLastLocation();
                    UserLocation userLocation = new UserLocation(userUid, location.getTime(), location.getLatitude(), location.getLongitude(),
                                                                 location.getAltitude(), location.getSpeed(), location.getAccuracy());
                    Map<String, Object> userLocationMap = userLocation.toMap();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path + "_" + userUid)
                                                                          .child(String.valueOf(userLocation.getTime()));
                    if (location != null) {

//Save the location data to the database//

                        ref.setValue(userLocationMap);
                    }
                }
            }, null);
        }
    }
}