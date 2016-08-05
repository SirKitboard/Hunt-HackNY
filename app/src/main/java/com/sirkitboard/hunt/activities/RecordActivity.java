package com.sirkitboard.hunt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sirkitboard.hunt.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.markushi.ui.CircleButton;

public class RecordActivity extends Activity implements SurfaceHolder.Callback {
	private MediaRecorder recorder;
	private SurfaceHolder surfaceHolder;
	private CamcorderProfile camcorderProfile;
	private Camera mCamera;
	boolean recording = false;
	boolean usecamera = true;
	boolean previewRunning = false;
	SurfaceView surfaceView;
	CircleButton btnStart;
	File root;
	File file;
	Boolean isSDPresent;
	SimpleDateFormat simpleDateFormat;
	String timeStamp;

	Timer timer;

	File saveFile;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_record);

		simpleDateFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
		timeStamp = simpleDateFormat.format(new Date());
		surfaceView = (SurfaceView) findViewById(R.id.recorder);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		btnStart = (CircleButton) findViewById(R.id.record_start);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		isSDPresent = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		mCamera = getCameraInstance();
		actionListener();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	/**
	 * Check if this device has a camera
	 */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d("CamEra", "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}


	private void actionListener() {

		btnStart.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						ViewGroup.LayoutParams params = v.getLayoutParams();
						params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 86, getResources().getDisplayMetrics());
						params.height =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 86, getResources().getDisplayMetrics());;
						v.setLayoutParams(params);
						if (!recording) {
							recording = true;
							startRecording();
						}
						return true;
					case MotionEvent.ACTION_UP:
						//
						stopRecording();
						return true;
				}
				return false;
			}
		});
	}

	public void stopRecording() {
		if (recording) {
			timer.cancel();
			recorder.stop();
			Intent result = new Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri"));
			result.putExtra("file_name", file.toString());
			setResult(Activity.RESULT_OK, result);
			// recorder.release();
			recording = false;
			System.out.println("STOPPED");
			finish();
		}
	}

	public void startRecording() {
		try {
			mCamera.unlock();
			recorder = new MediaRecorder();
			recorder.setPreviewDisplay(surfaceHolder.getSurface());
			recorder.setCamera(mCamera);
			recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
			file = getOutputMediaFile(MEDIA_TYPE_VIDEO);
			recorder.setOrientationHint(90);
			recorder.setOutputFile(file.toString());
			recorder.prepare();
			recorder.start();

			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if(recording) {
						stopRecording();
					}
				}
			}, 10000);
			System.out.println("RECORDING");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "HuntApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("HuntApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (surfaceHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d("CamEra", "Error starting camera preview: " + e.getMessage());
		}
	}
}