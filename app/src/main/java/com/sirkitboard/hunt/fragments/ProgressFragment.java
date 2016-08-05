package com.sirkitboard.hunt.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.facebook.login.LoginManager;
import com.sirkitboard.hunt.Hunt;
import com.sirkitboard.hunt.R;
import com.sirkitboard.hunt.activities.StoryPlayerActivity;
import com.sirkitboard.hunt.adapters.TeamProgressAdapter;
import com.sirkitboard.hunt.util.AsyncCallback;
import com.sirkitboard.hunt.util.AsyncTeamInfo;
import com.sirkitboard.hunt.util.HuntTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RunnableFuture;

/**
 * Created by abalwani on 31/07/2016.
 */
public class ProgressFragment extends Fragment implements AsyncCallback{
	View rootView;
	List<JSONObject> teams;
	ListView progressList;
	int runningDownloads = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_progress, container, false);
		Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
		toolbar.setTitle("Progress");
		progressList = ((ListView) rootView.findViewById(R.id.teamProgressList));
		AsyncTeamInfo.getTeamDataAsync(this);
		toolbar.inflateMenu(R.menu.menu_progress);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.logout:
						logout();
						return true;

					default:
						return false;
				}
			}
		});
		return rootView;
	}


	@Override
	public void preExecute() {

	}

	public void logout() {
		SharedPreferences prefs = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("currentUserID", "");
		editor.putBoolean("isLoggedIn", false);
		editor.putString("teamID", "");
		editor.apply();
		LoginManager.getInstance().logOut();
		getActivity().finish();
	}

	public void reloadData() {
		AsyncTeamInfo.getTeamDataAsync(this);
	}

	public List<JSONObject> jsonArrayToObjectList(JSONArray array) throws JSONException {
		ArrayList<JSONObject> list = new ArrayList<>();
		for(int i=0;i<array.length();i++) {
			list.add(array.getJSONObject(i));
		}
		return list;
	}

	@Override
	public void asyncSuccess(JSONArray jsonArray) {
		try {
			int numTeams = jsonArray.length();
			int numUsers = 0;
			int numClips = 0;
			for(int i=0;i<numTeams;i++) {
				JSONObject team = jsonArray.getJSONObject(i);
				numUsers += team.getJSONArray("users").length();
				numClips += team.getJSONObject("experiences").getJSONArray("completed").length();
			}

			((TextView) rootView.findViewById(R.id.numTeams)).setText("" + numTeams);
			((TextView) rootView.findViewById(R.id.numTotalRecorded)).setText("" + numClips);
			((TextView) rootView.findViewById(R.id.numPlayers)).setText("" + numUsers);


			teams = jsonArrayToObjectList(jsonArray);
			Collections.sort(teams, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject lhs, JSONObject rhs) {
					try {
						if(lhs.getInt("points") > rhs.getInt("points")) {
							return 1;
						} else if(lhs.getInt("points") < rhs.getInt("points")) {
							return -1;
						}
						return 0;
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return 0;
				}
			});

			progressList.setAdapter(new TeamProgressAdapter(getContext(), teams));
			progressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						String teamID = teams.get(position).getString("_id");
						Intent intent = new Intent(getContext(), StoryPlayerActivity.class);
						intent.putExtra("team_id", teamID);
						startActivity(intent);
					} catch (JSONException e) {
						Toast.makeText(getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
					}
				}
			});


			if(runningDownloads == 0) {
				downloadStories();
			}
		} catch (JSONException | NullPointerException e) {
			e.printStackTrace();
			Toast.makeText(getContext(), "API Error", Toast.LENGTH_SHORT).show();
		}
	}

	public void downloadStories() {
		CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
				getContext(),    /* get the context for the application */
				"us-east-1:"+getString(R.string.amazon_key),    /* Identity Pool ID */
				Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
		);
		AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

		final TransferUtility transferUtility = new TransferUtility(s3, getContext());
		try {
			for(JSONObject team: teams) {
				JSONArray experiences = team.getJSONObject("experiences").getJSONArray("completed");
				for(int i=0;i<experiences.length();i++) {
					HuntTask task = new HuntTask(experiences.getJSONObject(i));
					File file = new File(Environment.getExternalStoragePublicDirectory(
							Environment.DIRECTORY_PICTURES)+"/HuntApp", task.getFileName());
					if(!file.exists()) {
						runningDownloads++;
						TransferObserver observer = transferUtility.download("hunt-api", task.getFileName(), file);
						observer.setTransferListener(new TransferListener() {
							@Override
							public void onStateChanged(int id, TransferState state) {
								if (state == TransferState.COMPLETED || state == TransferState.FAILED) {
									runningDownloads--;
									if(runningDownloads == 0) {
										Toast.makeText(Hunt.getContext(), "Stories updated", Toast.LENGTH_SHORT).show();
									}
								}
							}

							@Override
							public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

							}

							@Override
							public void onError(int id, Exception ex) {
								ex.printStackTrace();
							}
						});
					}
				}
			}
			if(runningDownloads == 0) {
				Toast.makeText(getContext(), "Stories updated", Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
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
}
