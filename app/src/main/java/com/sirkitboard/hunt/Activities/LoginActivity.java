package com.sirkitboard.hunt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.sirkitboard.hunt.R;

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

		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				loginResult.getAccessToken();
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
