package com.sirkitboard.hunt.activities;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.VideoView;

import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.sirkitboard.hunt.R;
import com.sirkitboard.hunt.util.AsyncCallback;
import com.sirkitboard.hunt.util.AsyncTeamInfo;
import com.sirkitboard.hunt.util.HuntTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class StoryPlayerActivity extends AppCompatActivity implements AsyncCallback{
	ArrayList<HuntTask> taskList;
	String teamID;
	LinearLayout loading;
	EMVideoView videoView;
	File mTmpFile;
	int currentlyPlaying = 0;
	boolean playing = false;
	int numCalled = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_player);
		getSupportActionBar().hide();
		teamID = getIntent().getStringExtra("team_id");
		loading = (LinearLayout) findViewById(R.id.loadingLayout);
		videoView = (EMVideoView) findViewById(R.id.storyPlayer);
		videoView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
//						videoView.setOnCompletionListener(null);
//						videoView.stopPlayback();
						currentlyPlaying++;
						playNext();
						return true;
				}
				return false;
			}
		});
		AsyncTeamInfo.getTeamDataAsync(this);
	}

	@Override
	public void preExecute() {

	}

	@Override
	public void asyncSuccess(JSONArray jsonArray) {
		numCalled++;
		System.out.println("NumCalled" + numCalled);
		try {
			taskList = new ArrayList<HuntTask>();
			for(int i=0; i<jsonArray.length();i++) {
				if(jsonArray.getJSONObject(i).getString("_id").equalsIgnoreCase(teamID)) {
					JSONArray experiences = jsonArray.getJSONObject(i).getJSONObject("experiences").getJSONArray("completed");
					for(int j=0;j<experiences.length();j++) {
						taskList.add(new HuntTask(experiences.getJSONObject(j)));
					}
				}
			}
			System.out.println(taskList.size());
			if(!playing) {
				if (taskList.size() > 0) {
					playing = true;
					playNext();
				} else {
					finish();
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void playNext() {
		System.out.println(currentlyPlaying);
		if(currentlyPlaying < taskList.size()) {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES)+"/HuntApp", taskList.get(currentlyPlaying).getFileName());
			System.out.println(file.getPath());
			System.out.println(file.exists());
			if(file.exists()) {
				videoView.setOnCompletionListener(null);
				loading.setVisibility(View.GONE);
				videoView.start();
				videoView.setVideoURI(Uri.parse(file.toURI().toString()));
				videoView.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion() {
						videoView.stopPlayback();
						currentlyPlaying++;
						playNext();
					}
				});
			} else {
				playNext();
			}
		} else {
			finish();
		}

	}

	@Override
	public void asyncSuccess(JSONObject jsonArray) {

	}

	@Override
	public void asyncFailure() {

	}

	@Override
	public void asyncCompleted() {

	}

	@Override
	public void onDestroy() {
		if (mTmpFile != null) {
			mTmpFile.delete();
		}
		super.onDestroy();
	}

	private void copyToTmpFile(String url) {
		File f = new File(url);
		try {
			mTmpFile = File.createTempFile("video", null);
			mTmpFile.deleteOnExit();

			FileInputStream is = new FileInputStream(f);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			FileOutputStream fos = new FileOutputStream(mTmpFile);
			fos.write(buffer);
			fos.close();
		} catch (Exception e) {
			videoView.setVideoURI(Uri.parse(url));
		}
	}
}
