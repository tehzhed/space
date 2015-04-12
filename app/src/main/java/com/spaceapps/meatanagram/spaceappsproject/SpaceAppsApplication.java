package com.spaceapps.meatanagram.spaceappsproject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

/**
 * Created by Simone on 4/11/2015.
 */
public class SpaceAppsApplication extends Application {

    private static final String TAG = "SpaceAppsApplication";

    static Context CONTEXT;

    @Override
    public void onCreate()
    {
        super.onCreate();

        CONTEXT = getApplicationContext();

        Parse.initialize(this, getString(R.string.parse_com_app_key), getString(R.string.parse_com_client_key));
        ParseFacebookUtils.initialize(getString(R.string.app_id));

        ParseObject.registerSubclass(Post.class);

        Intent locInt = new Intent(this, LocationService.class);
        Log.d(TAG, "Starting Location Service");
        startService(locInt);
    }

    public static Context getAppContext()
    {
        return CONTEXT;
    }
}
