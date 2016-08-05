package com.sirkitboard.hunt.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sirkitboard.hunt.R;
import com.sirkitboard.hunt.util.HuntRestAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.execchain.MainClientExec;

public class LoginActivity extends AppCompatActivity {

	CallbackManager callbackManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		callbackManager = CallbackManager.Factory.create();

		LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

		assert loginButton != null;

		ProfileTracker profileTracker = new ProfileTracker() {
			@Override
			protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
				RequestParams params = new RequestParams();
				String name = Profile.getCurrentProfile().getName();
				params.add("name", name);
				HuntRestAPI.post("/users/create", params, new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						// If the response is JSONObject instead of expected JSONArray
						try {
							System.out.println(response);
							SharedPreferences prefs = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
							SharedPreferences.Editor prefEditor = prefs.edit();
							prefEditor.putString("currentUserID", response.getString("_id"));
							prefEditor.putBoolean("isLoggedIn", true);
							prefEditor.apply();
							Intent intent = new Intent(getApplicationContext(), MainActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//							intent.putExtra("EXIT", true);
							startActivity(intent);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
						// Pull out the first event on the public timeline
						System.out.println(response);
					}
				});
			}
		};

		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
//				loginResult.getAccessToken();
			}

			@Override
			public void onCancel() {
				Toast.makeText(getApplicationContext(), "Login Cancelled", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(FacebookException exception) {
				Toast.makeText(getApplicationContext(), "Login Error", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}
}
