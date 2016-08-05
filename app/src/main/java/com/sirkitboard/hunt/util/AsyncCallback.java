package com.sirkitboard.hunt.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Adi on 7/29/2016.
 */
public interface AsyncCallback {
	public void preExecute();

	public void asyncSuccess(JSONArray jsonArray);
	public void asyncSuccess(JSONObject jsonArray);

	public void asyncFailure();

	public void asyncCompleted();
}
