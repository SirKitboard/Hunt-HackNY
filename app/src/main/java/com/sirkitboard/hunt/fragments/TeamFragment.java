package com.sirkitboard.hunt.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sirkitboard.hunt.R;
import com.sirkitboard.hunt.adapters.TeamMembersAdapter;
import com.sirkitboard.hunt.util.AsyncCallback;
import com.sirkitboard.hunt.util.AsyncTeamInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abalwani on 30/07/2016.
 */
public class TeamFragment extends Fragment implements AsyncCallback{
	View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_teams, container, false);

		AsyncTeamInfo.getTeamDataAsync(this);
		return rootView;
	}

	@Override
	public void preExecute() {

	}

	public List<JSONObject> jsonArrayToObjectList(JSONArray array) throws JSONException {
		ArrayList<JSONObject> list = new ArrayList<>();
		for(int i=0;i<array.length();i++) {
			list.add(array.getJSONObject(i));
		}
		return list;
	}


	@Override
	public void asyncSuccess(JSONArray object) {
		try {
			TextView teamName = (TextView) rootView.findViewById(R.id.team_name);
			TextView teamPlace = (TextView) rootView.findViewById(R.id.team_place);

			TextView numPoints = (TextView) rootView.findViewById(R.id.numPoints);
			TextView percentCompleted = (TextView) rootView.findViewById(R.id.percentCompleted);
			TextView numRecorded = (TextView) rootView.findViewById(R.id.numRecorded);
			ListView memberList = (ListView) rootView.findViewById(R.id.teamMemberList);

//		System.out.println(object);
			JSONObject team = object.getJSONObject(0);
			teamName.setText(team.getString("name"));
			int points = team.getInt("points");
			int place = 1;
			for(int i=0;i<object.length();i++) {
				JSONObject teamTemp = object.getJSONObject(i);
				if(teamTemp.getInt("points") > points) {
					place++;
				}
			}
			if(place == 1) {
				teamPlace.setText("1st Place");
			} else if(place == 2) {
				teamPlace.setText("2nd Place");
			} else if(place == 3) {
				teamPlace.setText("3rd Place");
			} else {
				teamPlace.setText(place + "th Place");
			}

			List<JSONObject> members = jsonArrayToObjectList(team.getJSONArray("users"));
			memberList.setAdapter(new TeamMembersAdapter(getContext(), members));
			numPoints.setText(team.getString("points"));
			percentCompleted.setText("" + team.getJSONObject("experiences").getJSONArray("completed").length() * 10);
			numRecorded.setText("" + team.getJSONObject("experiences").getJSONArray("completed").length());
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getContext(), "JSON error", Toast.LENGTH_SHORT).show();
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
