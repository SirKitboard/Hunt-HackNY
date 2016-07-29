package com.sirkitboard.hunt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.sirkitboard.hunt.Activities.LoginActivity;
import com.sirkitboard.hunt.Activities.MainActivity;

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

        AccessToken currentToken = AccessToken.getCurrentAccessToken();
        Intent intent;
        if(currentToken == null || !currentToken.isExpired()) {
            // Go to Login Screen
            intent = new Intent(context, LoginActivity.class);
        } else {
            // TODO:Go to Main activity
            intent = new Intent(context, MainActivity.class);
        }
        startActivity(intent);
    }

    public static Context getContext() {
        return context;
    }
}
