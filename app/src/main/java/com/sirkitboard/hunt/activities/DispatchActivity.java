package com.sirkitboard.hunt.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.login.LoginManager;
import com.sirkitboard.hunt.activities.LoginActivity;
import com.sirkitboard.hunt.activities.MainActivity;

public class DispatchActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_dispatch);
		LoginManager.getInstance().logOut();

		SharedPreferences prefs = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		Intent intent;
		if(prefs.getBoolean("isLoggedIn", false)) {
			intent = new Intent(getApplicationContext(), MainActivity.class);
		} else {
			intent = new Intent(getApplicationContext(), LoginActivity.class);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

}
