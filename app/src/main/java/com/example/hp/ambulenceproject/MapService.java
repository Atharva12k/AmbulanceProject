package com.example.hp.ambulenceproject;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.maps.model.LatLng;

public class MapService extends Service {
    LocationManager locationManager;
    LocationListener locationListener;
    final static public String MAP_SERVICE_BROADCAST = MapService.class.getName() + "LocationBroadcast";
    private Intent updateintent;
    LocalBroadcastManager broadcaster;
    private static final String TAG = MapService.class.getName();
    //@androidx.annotation.Nullable
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        Log.d(TAG,"oncreate");
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Service.START_STICKY;
        }
        Log.d(TAG,"onstartcommand");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 100, locationListener);

        return Service.START_STICKY;
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }
        @Override
        public void onProviderEnabled(String s) { }
        @Override
        public void onProviderDisabled(String s) {}
        public void onLocationChanged(Location loc) {
            if(loc!=null) {
                float[] results = new float[10];
                if (loc != null) {
                        LatLng curr_latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
                        sendBroadcastMessage(curr_latlng);
                }
            }
        }
        public void sendBroadcastMessage(LatLng currLatLng)
        {
            updateintent=new Intent(MAP_SERVICE_BROADCAST);
            updateintent.putExtra("CURRENT_LATLNG",currLatLng);
            broadcaster.sendBroadcast(updateintent);
        }
    }
}
