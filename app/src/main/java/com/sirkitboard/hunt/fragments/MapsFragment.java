package com.sirkitboard.hunt.fragments;

import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.sirkitboard.hunt.Hunt;
import com.sirkitboard.hunt.R;
import com.sirkitboard.hunt.activities.MainActivity;

/**
 * Created by Adi on 7/29/2016.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback{
	private GoogleMap mMap;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
		setupMapIfNeeded();
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

}
