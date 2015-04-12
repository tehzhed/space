package com.spaceapps.meatanagram.spaceappsproject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Simone on 4/12/2015.
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationService";

    private static final long ONE_MIN = 1000 * 60;
    private static final long ONE_HOUR = ONE_MIN * 60;
    private static final long FLAG_IN_CACHE_MIN = ONE_HOUR * 2;
    private static final long INTERVAL = ONE_MIN * 5;
    private static final long FASTEST_INTERVAL = ONE_MIN * 3;

    private static LocationRequest locationRequest;
    private static GoogleApiClient googleApiClient;
    private static FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private Location location;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate()
    {
        super.onCreate();
        connectToGoogleAPI();
    }

    private void connectToGoogleAPI()
    {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "Being Destroyed");
        super.onDestroy();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        Log.d(TAG, "Connected to Google Api");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed " + connectionResult.toString());
    }

    static Location getLocation()
    {
        return fusedLocationProviderApi.getLastLocation(googleApiClient);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder
    {
        public LocationService getService()
        {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }
}
