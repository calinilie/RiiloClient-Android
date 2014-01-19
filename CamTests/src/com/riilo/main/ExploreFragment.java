package com.riilo.main;

import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.riilo.interfaces.ILocationListener;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ExploreFragment 
		extends Fragment
		implements ILocationListener, OnCameraChangeListener{

	private static final String TAG = "<<<<<<<<ExploreFragment>>>>>>>>";
	
	private GoogleMap map;
	private View view;
	private MapView mapView;
	private MainActivity activity;
	
	private boolean mapCameraAnimationRun = false; 
	private Timer timer;
	
	@Override
	public void onAttach(Activity activity){
 		this.activity = (MainActivity)activity;
 		super.onAttach(activity);
 	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
//        super.initLocationClient(LocationRequest.PRIORITY_LOW_POWER, 4000, 2000); //TODO
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.explore_layout, container, false);
    	
    	mapView =(MapView)view.findViewById(R.id.map);
    	mapView.onCreate(savedInstanceState);
    	mapView.onResume();
    	
    	try {
            MapsInitializer.initialize(activity);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    	
    	return view;
    }
	
	@Override
    public void onStart() {
    	super.onStart();
    	setUpMapIfNeeded();
    	setupWidgetsViewElements();
    }
	
	@Override
	public void onLocationChanged(Location location) {
		if (!mapCameraAnimationRun){
			animateMapCamera(new LatLng(location.getLatitude(), location.getLongitude()), 6);
			double[] latLng = Helpers.setReqFrom_Latitude_and_Longitude(location, null);
			PostsCache.getInstance(activity).getPostsOnMap(map, new Handler());//getAtLocationPosts(latLng[0], latLng[1], map, new Handler());
		}		
	}
	
	//Redirect fragment lifecycle to mapView===================================================================================================================
	
	@Override
	public void onResume() {
        super.onResume();
        if(mapView!=null){
        	mapView.onResume();	
        }
    }
   
    @Override
    public void onPause() {
        super.onPause();
        if (null != mapView)
        	mapView.onPause();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mapView)
        	mapView.onSaveInstanceState(outState);
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mapView)
        	mapView.onLowMemory();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mapView)
        	mapView.onDestroy();
    }
    
    //=========================================================================================================================================================
    
	private void setupWidgetsViewElements() {
		mapCameraAnimationRun = false;
		if (timer==null)
			timer = new Timer();
	}

	private void setUpMapIfNeeded() {
		map = mapView.getMap();
		if (map!=null){
			map.setMyLocationEnabled(true);
			map.setOnCameraChangeListener(this);
		}
	}
	
	private void animateMapCamera(LatLng location, float zoom){
		CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(location.latitude, location.longitude), zoom);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
		map.animateCamera(update);
		mapCameraAnimationRun = true;
		
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		timer.cancel();
		timer.purge();
		timer = new Timer();
		final CameraPosition camPos = position;
		final Handler handler = new Handler();
		Log.d(TAG, position.zoom+"");
		LatLng farLeftVisibleEdge = map.getProjection().getVisibleRegion().farLeft;
		final double distance = Helpers.distanceFrom(camPos.target.latitude, camPos.target.longitude, farLeftVisibleEdge.latitude, farLeftVisibleEdge.longitude);
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (camPos.zoom>=7.5){
					PostsCache.getInstance(activity).getAtLocationPosts(camPos.target.latitude, camPos.target.longitude, distance, map, handler);
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(activity, "loading ", Toast.LENGTH_SHORT).show();
						}
					});
				}
				else{
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(activity, "you have to zoom in a little more", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}, 2000);
//			if (isRequestAllowed()){
//				Toast.makeText(activity, "loading ", Toast.LENGTH_LONG).show();
//				PostsCache.getInstance(activity).getAtLocationPosts(position.target.latitude, position.target.longitude, map);
//			}
	
	}
	
	
	private long timeStarted;
	private boolean isRequestAllowed(){
		if (System.nanoTime() - timeStarted >= 1000000000){
			timeStarted = System.nanoTime();
			Log.d(TAG, timeStarted+" true");
			return true;
		}
		Log.d(TAG, timeStarted+" false");
		return false;
	}

}
