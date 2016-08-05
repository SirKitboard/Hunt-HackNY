package com.sirkitboard.hunt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.sirkitboard.hunt.activities.LoginActivity;
import com.sirkitboard.hunt.activities.MainActivity;

/**
 * Created by abalwani on 29/07/2016.
 */
public class Hunt extends Application {

    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
