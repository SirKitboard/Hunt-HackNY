package com.sirkitboard.hunt.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sirkitboard.hunt.R;
import com.sirkitboard.hunt.util.HuntRestAPI;

import org.json.JSONArray;

import java.io.File;

import cz.msebera.android.httpclient.Header;

public class VideoPreview extends AppCompatActivity {
	String file;
	private ProgressDialog pDialog;
	SharedPreferences prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_preview);
		file = getIntent().getStringExtra("file");

		VideoView videoView = (VideoView) findViewById(R.id.videoPreview);
//		videoView.setRotation(90);
		getSupportActionBar().hide();

		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(true);
			}
		});
		prefs = getSharedPreferences("user", Context.MODE_PRIVATE);

		videoView.setVideoPath(file);
		videoView.start();
	}

	public void submitVideo(View view) {
		pDialog = new ProgressDialog(this);
//		pDialog.setMessage("Uploading Data ...");
		pDialog.setTitle("Uploading Data ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
		CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
				getApplicationContext(),    /* get the context for the application */
				"us-east-1:4a5ac310-6b03-4730-82d4-312f35f604bc",    /* Identity Pool ID */
				Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
		);
		AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

		TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
		final File fileO = new File(file);
		TransferObserver transferObserver = transferUtility.upload("hunt-api", fileO.getName(), fileO);

		transferObserver.setTransferListener(new TransferListener() {
			@Override
			public void onStateChanged(int id, TransferState state) {
				System.out.println(state);
				if(state == TransferState.COMPLETED) {
					pDialog.hide();
					RequestParams params = new RequestParams();
					params.add("filename", fileO.getName());
					params.add("experienceId", prefs.getString("experienceId", ""));
					HuntRestAPI.post("/teams/complete/"+prefs.getString("teamID", ""), params, new JsonHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
							System.out.println(response);
							Intent result = new Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri"));
							result.putExtra("success", true);
							setResult(Activity.RESULT_OK, result);
							finish();
						}

						@Override
						public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
							Toast.makeText(getApplicationContext(),"Upload Failed", Toast.LENGTH_SHORT).show();
						}

					});
				}
			}

			@Override
			public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
				System.out.println(bytesCurrent + " / " + bytesTotal);
				int percent = (int)((bytesCurrent * 100)/bytesTotal);
				pDialog.setMessage(percent + "%");
			}

			@Override
			public void onError(int id, Exception ex) {
				ex.printStackTrace();
			}
		});
	}
}
