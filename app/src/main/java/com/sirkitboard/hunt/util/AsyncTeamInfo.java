package com.sirkitboard.hunt.util;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by abalwani on 30/07/2016.
 */
public class AsyncTeamInfo extends AsyncTask<String, String, JSONArray> {

	private AsyncCallback callback;
	private static boolean isTaskRunning;

//	public AsyncTeamInfo(AsyncCallback callback) {
//		this.callback = callback;
//	}

	private static JSONArray teamData;
	private static ArrayList<AsyncCallback> requestQueue = new ArrayList<>();

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
//		callback.preExecute();
	}

	public static void getTeamDataAsync(AsyncCallback caller) {
		if(teamData == null) {
			requestQueue.add(caller);
			if(!isTaskRunning) {
				isTaskRunning = true;
				new AsyncTeamInfo().execute("");
			}

		} else {
			caller.asyncSuccess(teamData);
		}
	}



	private String readStream(InputStream in) {
		BufferedReader reader = null;
		StringBuffer response = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response.toString();
	}

	@Override
	protected JSONArray doInBackground(String... args) {
		JSONArray json = null;
		try {
			URL url = new URL("http://hunt-api.herokuapp.com/api/teams");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			try {
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				json = new JSONArray(readStream(in));
			} catch(JSONException e) {
				Log.e("ERRRRORRRRR", "idk");
				e.printStackTrace();
			} finally {
				urlConnection.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Getting JSON from URL
		return json;
	}

	@Override
	protected void onPostExecute(JSONArray json) {
		teamData = json;
		isTaskRunning = false;
		while(!requestQueue.isEmpty()) {
			AsyncCallback caller = requestQueue.remove(0);
			caller.asyncSuccess(json);
		}
	}
}
