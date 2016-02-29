package com.example.david.hvztracker;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Monitor the GPS location updates
 */
public class CurrentLocationManager implements LocationListener{
    TrackingActivity requester = null;
    LocationManager locationManager = null;

    public CurrentLocationManager(TrackingActivity trackingActivity){
        requester = trackingActivity;

        // setup to listen to the GPS
        locationManager = (LocationManager)requester.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 300, (float) 10.0, (LocationListener) this);
        }catch (SecurityException e){
            Log.e("CurrentLocationManager", "A security Exception occured\n");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("CurrentLocationManager", "Provider " + provider + " is enabled.");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("CurrentLocationManager", "Provider " + provider + " is disabled.");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("CurrentLocationManager", "Status of the provider changes to " + Integer.toString(status) + ".");
    }

    @Override
    public void onLocationChanged(Location location) {
        // Inform the rest of the app that the user moved
        requester.onLocationChanged(location);
    }
}
