package com.sirkitboard.hunt.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by abalwani on 31/07/2016.
 */
public class HuntTask {
	private int order;
	private String id;
	private double lat;
	private double lon;
	private String fileName;
	private Date dateCompleted;
	private String name;
	private String descr;
	private String clueDescr;
	private int points;
	private String title;

	public HuntTask(JSONObject object) throws JSONException{
		order = object.getInt("order");
		if(!object.isNull("filename")) {
			fileName = object.getString("filename");
			dateCompleted = null;
		} else {
			fileName = null;
			dateCompleted = null;
		}
		id = object.getString("_id");
		JSONObject location = object.getJSONObject("location");
		lat = location.getDouble("lat");
		lon = location.getDouble("lon");
		name = location.getString("name");
		clueDescr = location.getString("clueDescription");

		JSONObject task = object.getJSONObject("task");
		descr = task.getString("description");
		title = task.getString("title");
		points = task.getInt("points");

	}

	public int getOrder() {
		return order;
	}

	public String getId() {
		return id;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public String getFileName() {
		return fileName;
	}

	public Date getDateCompleted() {
		return dateCompleted;
	}

	public String getName() {
		return name;
	}

	public String getDescr() {
		return descr;
	}

	public String getClueDescr() {
		return clueDescr;
	}

	public int getPoints() {
		return points;
	}

	public String getTitle() {
		return title;
	}
}
