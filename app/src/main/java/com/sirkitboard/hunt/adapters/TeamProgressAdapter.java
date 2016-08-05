package com.sirkitboard.hunt.adapters;

/**
 * Created by abalwani on 30/07/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sirkitboard.hunt.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TeamProgressAdapter extends ArrayAdapter {
	private Context context;
	LayoutInflater inflater;
	List<JSONObject> values;

	public TeamProgressAdapter(Context context, List<JSONObject> objects) {
		super(context, -1, objects);
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.context = context;
		this.values = objects;
	}

	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public JSONObject getItem(int position) {
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (convertView == null) {
			rowView = inflater.inflate(R.layout.progress_adapter, parent, false);
		}

		TextView teamName = (TextView) rowView.findViewById(R.id.teamName);
		TextView place = (TextView) rowView.findViewById(R.id.place);
		TextView numRecorded = (TextView) rowView.findViewById(R.id.numRecorded);
		TextView percentCompleted = (TextView) rowView.findViewById(R.id.percentCompleted);
		TextView numPoints = (TextView) rowView.findViewById(R.id.numPoints);

		SharedPreferences prefs = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);

		try {
			JSONObject item = getItem(position);
			teamName.setText(item.getString("name"));
			numPoints.setText(item.getString("points") + " Points");
			int numCompletedTasks = item.getJSONObject("experiences").getJSONArray("completed").length();
			numRecorded.setText(numCompletedTasks + " Check-ins");
			percentCompleted.setText(numCompletedTasks* 100 / prefs.getInt("numTasks", 10) + "% Complete");
			place.setText("" + (position+1));
		} catch (JSONException e) {
			e.printStackTrace();
		}


		return rowView;
	}
}
