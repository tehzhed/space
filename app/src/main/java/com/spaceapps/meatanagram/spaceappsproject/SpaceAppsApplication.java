package com.spaceapps.meatanagram.spaceappsproject;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

/**
 * Created by Simone on 4/11/2015.
 */
public class SpaceAppsApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();

        Parse.initialize(this, getString(R.string.parse_com_app_key), getString(R.string.parse_com_client_key));
        ParseFacebookUtils.initialize(getString(R.string.app_id));
    }
}
