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

import java.util.ArrayList;
import java.util.List;

public class TeamMembersAdapter extends ArrayAdapter {
	private Context context;
	LayoutInflater inflater;
	List<JSONObject> values;

	public TeamMembersAdapter(Context context, List<JSONObject> objects) {
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
			rowView = inflater.inflate(R.layout.adapter_team_member, parent, false);
		}

		TextView memberName = (TextView) rowView.findViewById(R.id.member_name);
		TextView memberLastActive = (TextView) rowView.findViewById(R.id.member_last_active);

		try {
			memberName.setText(getItem(position).getString("name"));
			memberLastActive.setText("2 mins ago");
		} catch (JSONException e) {
			e.printStackTrace();
		}


		return rowView;
	}
}
