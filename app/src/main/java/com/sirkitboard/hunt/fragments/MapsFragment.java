package com.sirkitboard.hunt.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sirkitboard.hunt.Hunt;
import com.sirkitboard.hunt.R;
import com.sirkitboard.hunt.activities.RecordActivity;
import com.sirkitboard.hunt.activities.VideoPreview;
import com.sirkitboard.hunt.util.AsyncCallback;
import com.sirkitboard.hunt.util.AsyncTeamInfo;
import com.sirkitboard.hunt.util.HuntRestAPI;
import com.sirkitboard.hunt.util.HuntTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Adi on 7/29/2016.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, AsyncCallback,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
	private GoogleMap mMap;
	private ArrayList<HuntTask> completed;
	private HuntTask nextTask;
	GoogleApiClient mGoogleApiClient;
	View rootView;
	SharedPreferences prefs;
	boolean nextVisible;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_maps, container, false);
		setupMapIfNeeded();
		prefs = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		HuntRestAPI.get("/hunts", null, new JsonHttpResponseHandler() {
			public void onSuccess(int statusCode, Header[] headers, JSONArray respone) {
				try {
					JSONObject response = respone.getJSONObject(0);
					SharedPreferences.Editor editor = prefs.edit();
					int count = response.getInt("taskCount");
					System.out.println(count);
					editor.putInt("numTasks", count);
					editor.apply();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(getContext())
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}
		if(prefs.getString("teamID", "").equalsIgnoreCase("")) {
			String userID = prefs.getString("currentUserID", "");
			System.out.println(userID);
			HuntRestAPI.get("/users/"+userID, null, new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
					try {
						String teamId = response.getString("teamId");
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("teamID", teamId);
						editor.commit();
						AsyncTeamInfo.getTeamDataAsync(MapsFragment.this);
					} catch (JSONException e) {
						Toast.makeText(getContext(), "Please wait until you're assigned a team", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String idk, Throwable error) {
					System.out.println("ERRRORRRR");
				}
			});
		} else {
			AsyncTeamInfo.getTeamDataAsync(this);
		}
		FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.launchDoExperience);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doExperience(v);
			}
		});
		return rootView;
	}

	public void setupMapIfNeeded() {
		if(mMap == null) {
			SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
		}
	}

	public boolean getLocationPermissionState() {
		return (ActivityCompat.checkSelfPermission(Hunt.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Hunt.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
	}



	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		if(getLocationPermissionState()) {
			mMap.setMyLocationEnabled(true);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void preExecute() {

	}

	@Override
	public void asyncSuccess(JSONArray jsonArray) {
		String teamID = prefs.getString("teamID", "");
		try {
			for(int i=0; i<jsonArray.length();i++) {
				if(jsonArray.getJSONObject(i).getString("_id").equalsIgnoreCase(teamID)) {
					JSONObject experiences = jsonArray.getJSONObject(i).getJSONObject("experiences");
					nextTask = new HuntTask(experiences.getJSONObject("next"));
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("experienceId", nextTask.getId());
					JSONArray completedTasks = experiences.getJSONArray("completed");
					completed = new ArrayList<>();
					for(int j=0;j<completedTasks.length();j++) {
						completed.add(new HuntTask(completedTasks.getJSONObject(j)));
					}
					Collections.sort(completed, new Comparator<HuntTask>() {
						@Override
						public int compare(HuntTask lhs, HuntTask rhs) {

							if(lhs.getOrder() > lhs.getOrder()) {
								return 1;
							} else if(lhs.getOrder() < lhs.getOrder()) {
								return -1;
							}
							return 0;
						}
					});
				}
			}
//			TextView taskTitle = (TextView) rootView.findViewById(R.id.hintTitle);
			TextView taskHint = (TextView) rootView.findViewById(R.id.hint);
			taskHint.setText(nextTask.getClueDescr());
//			(TextView) taskTitle = (TextView) rootView.findViewById(R.id.hintTitle);
//			renderMarkers();
		} catch (JSONException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void renderMarkers() {

		if(completed != null) {
			PolylineOptions rectOptions = new PolylineOptions();
			HuntTask prevTask = null;
			for(HuntTask task: completed) {
				mMap.addMarker(new MarkerOptions()
						.position(new LatLng(task.getLat(), task.getLon()))
						.title(task.getTitle())
//						.snippet(task.getDescr())
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_centered_58)));
				rectOptions.add(new LatLng(task.getLat(), task.getLon()));
			}
			rectOptions = rectOptions.color(getContext().getResources().getColor(R.color.strokeColor));
			mMap.addPolyline(rectOptions);
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

	public void reloadMaps() {
		mMap.clear();
		FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.launchDoExperience);
		button.setVisibility(View.GONE);
		TextView taskTitle = (TextView) rootView.findViewById(R.id.hintTitle);
		taskTitle.setVisibility(View.GONE);

		AsyncTeamInfo.getTeamDataAsync(this);
	}

	public void doExperience(View v) {
		Intent intent = new Intent(getContext(), RecordActivity.class);
		getActivity().startActivityForResult(intent, 101);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == 101) {
//			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(getContext(), VideoPreview.class);
				intent.putExtra("file", data.getExtras().getString("file_name"));
				startActivity(intent);
//			}
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) throws SecurityException{
		mMap.clear();
		renderMarkers();
		Location location = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient);

		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
		double lat = location.getLatitude();
		double lon = location.getLongitude();

		CameraPosition camPos = new CameraPosition.Builder()
				.target(new LatLng(lat, lon))
				.zoom(15)
				.bearing(location.getBearing())
				.tilt(0)
				.build();
		CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
		mMap.animateCamera(camUpd3);
	}

	@Override
	public void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	protected void stopLocationUpdates() {
		if(mGoogleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(
					mGoogleApiClient, this);
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		System.out.println(i);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		System.out.println(connectionResult);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(!prefs.getString("teamID", "").equalsIgnoreCase("")) {
			AsyncTeamInfo.getTeamDataAsync(this);
		}
	}

	public void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	public void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}


	@Override
	public void onLocationChanged(Location location) {
		mMap.clear();
		renderMarkers();
		if(nextTask != null) {
			Location nextTaskLocation = new Location("task");
			nextTaskLocation.setLongitude(nextTask.getLon());
			nextTaskLocation.setLatitude(nextTask.getLat());
			if(nextTaskLocation.distanceTo(location) < 100) {
				FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.launchDoExperience);
				button.setVisibility(View.VISIBLE);
				TextView taskTitle = (TextView) rootView.findViewById(R.id.hintTitle);
				taskTitle.setVisibility(View.VISIBLE);
				TextView taskHint = (TextView) rootView.findViewById(R.id.hint);
				taskTitle.setText(nextTask.getName());
				taskHint.setText(nextTask.getDescr());
				mMap.addMarker(new MarkerOptions()
						.position(new LatLng(nextTaskLocation.getLatitude(), nextTaskLocation.getLongitude()))
						.title(nextTask.getTitle())
//						.snippet(nextTask.getDescr())
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_centered_58)));

			}
		}
	}
}
