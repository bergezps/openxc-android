package com.openxc.remote.sources;

import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;

import android.content.Context;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.Looper;

import android.util.Log;

public class NativeLocationSource extends AbstractVehicleDataSource
        implements LocationListener, Runnable {
    private final static String TAG = "NativeLocationSource";
    private final static int NATIVE_GPS_UPDATE_INTERVAL = 5000;

    private LocationManager mLocationManager;

    public NativeLocationSource(Context context, SourceCallback callback) {
        super(context, callback);
        new Thread(this).start();
        mLocationManager = (LocationManager) getContext().getSystemService(
                    Context.LOCATION_SERVICE);
    }

    public void run() {
        Looper.myLooper().prepare();

        // try to grab a rough location from the network provider before
        // registering for GPS, which may take a while to initialize
        Location lastKnownLocation = mLocationManager
            .getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
        if(lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation);
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    NATIVE_GPS_UPDATE_INTERVAL, 0,
                    this);
            Log.d(TAG, "Requested GPS updates");
        } catch(IllegalArgumentException e) {
            Log.w(TAG, "GPS location provider is unavailable");
        }
        Looper.myLooper().loop();
    }

    public void stop() {
        Log.i(TAG, "Disabled native GPS passthrough");
        mLocationManager.removeUpdates(this);
    }

    public void onLocationChanged(final Location location) {
        handleMessage(Latitude.ID, location.getLatitude());
        handleMessage(Longitude.ID, location.getLongitude());
    }

    public void onStatusChanged(String provider, int status,
            Bundle extras) {}
    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}
}
