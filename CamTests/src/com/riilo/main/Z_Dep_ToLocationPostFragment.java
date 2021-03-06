package com.riilo.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.riilo.main.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.riilo.interfaces.IBackButtonListener;
import com.riilo.interfaces.ILocationListener;
import com.riilo.main.AnalyticsWrapper.EventLabel;

public class Z_Dep_ToLocationPostFragment extends Fragment implements OnMapClickListener, OnClickListener, ILocationListener, IBackButtonListener, ClusterManager.OnClusterItemClickListener<LocationHistory>, ClusterManager.OnClusterClickListener<LocationHistory>{
	
	private GoogleMap map;
	private ClusterManager<LocationHistory> mapClusterManager;
	
	private ImageButton buttonPost;
	private ImageButton cancelButton;
	private EditText inputMessage;
	private View panelCreatePosts;
	private View tutorialMarker;
	private ImageButton closeTutorialMarker;
	
	private boolean mapCameraAnimationRun = false; 
	private Post currentPost;
	private Marker marker;
	
	private View view;
	private MapView mapView;
	private MainActivity activity;
	boolean skipNullCheck;
	
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
    	view = inflater.inflate(R.layout.write_post_layout, container, false);
    	
    	mapView =(MapView)view.findViewById(R.id.map);
    	mapView.onCreate(savedInstanceState);
    	mapView.onResume();
    	
    	try {
            MapsInitializer.initialize(activity);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    	
    	skipNullCheck = true;
    	
    	return view;
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    	setUpMapIfNeeded(skipNullCheck);
    	skipNullCheck = false;
    	
    	setupWidgetsViewElements();
    	newPostIfNeeded();
    	
    	LocationHistoryManager.getInstance(activity).getLocationHistory(mapClusterManager, null);
    	
    	mapCameraAnimationRun = false;
    }
    
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
    
   	@Override
   	public void onClick(View v) {
   		switch (v.getId()){
   		case R.id.button_post:
   			activity.analytics.recordEvent_WritePost_ButtonClick(EventLabel.button_post);
   			String message = inputMessage.getText().toString();
   			if (message!=null && !message.isEmpty()){
	   			currentPost.setMessage(message);
	   			currentPost.setUserAtLocation(activity.location, currentPost.getLatitude(), currentPost.getLongitude());
	   			currentPost.setDateCreated(Calendar.getInstance().getTime());
	   			currentPost.setUserId(activity.deviceId);
	
	       		Intent intentPost= new Intent(activity, WorkerService.class);
	       		intentPost.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_POST);
	       		intentPost.putExtra(StringKeys.POST_BUNDLE, currentPost.toBundle());
	           	activity.startService(intentPost);
	           	
	    		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
	    		postViewIntent.putExtra(StringKeys.POST_BUNDLE, currentPost.toBundle());
	    		startActivity(postViewIntent);
	    		
	    		inputMessage.setText("");
   			}
   			else{
   				Toast.makeText(activity, getString(R.string.error_post_empty), Toast.LENGTH_LONG).show();
   			}
   			break;
   		case R.id.button_cancel:
   			hideReplyToPostPannel();
   			break;
   		/*case R.id.button_close_tutorial_marker:
   			Animation slideOut = AnimationUtils.loadAnimation(activity.getApplicationContext(),
   	                R.anim.slide_out_bottom);
   	   	 
	   	   	 if (tutorialMarker.getVisibility()==View.VISIBLE){
	   	   		 tutorialMarker.startAnimation(slideOut);
	   	   		 tutorialMarker.requestLayout();
	   	   		 tutorialMarker.setVisibility(View.GONE);
	   	   	 }
	   	   	 
	   	   	 Facade.getInstance(activity).updateTutorialMarkerRun();
	   	   	 break;*/
   		}
   	}
    
   	@Override
    public void onLocationChanged(Location location) {
    	if (!mapCameraAnimationRun){
    		if (location.getAccuracy()<2000){
	    		animateMapCamera(new LatLng(location.getLatitude(), location.getLongitude()), 10);
    		}
    	}
    }

	@Override
	public void onMapClick(LatLng location) {
		activity.analytics.recordEvent_WritePost_MapClick();
		createPost(location);
		/*currentPost.setLatitude(location.latitude);
		currentPost.setLongitude(location.longitude);
		if (marker!=null)
			marker.remove();
		marker = map.addMarker(new MarkerOptions().position(location));*/
	}
	
	@Override
	public void onDestroyView(){
		LocationHistoryManager.getInstance(activity).locationHistoryMarkersRemoved();
		super.onDestroyView();
	}
	
	private void createPost(LatLng location){
		showCreatePostPanel();
		animateMapCamera(location, 15);
		currentPost.setLatitude(location.latitude);
		currentPost.setLongitude(location.longitude);
		if (marker!=null)
			marker.remove();
		marker = map.addMarker(new MarkerOptions().position(location));
	}
	
	private void animateMapCamera(LatLng location, float zoom){
		CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(location.latitude, location.longitude), zoom);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
		map.animateCamera(update);
		mapCameraAnimationRun = true;
	}
	
	private void hideReplyToPostPannel(){
   	 InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
   	 if (inputManager!=null)
   		 if (activity.getCurrentFocus()!=null)
		    	 inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
		                    InputMethodManager.HIDE_NOT_ALWAYS);
   	 
   	 Animation slideOut = AnimationUtils.loadAnimation(activity.getApplicationContext(),
                R.anim.slide_out_bottom);
   	 
   	 if (panelCreatePosts.getVisibility()==View.VISIBLE){
   		 panelCreatePosts.startAnimation(slideOut);
   		 panelCreatePosts.requestLayout();
   		 panelCreatePosts.setVisibility(View.GONE);
   	 	}
    }
	
	private void showCreatePostPanel(){
   	 Animation slideIn = AnimationUtils.loadAnimation(activity, 
   			 R.anim.slide_in_bottom);
   	 if (panelCreatePosts.getVisibility() == View.GONE){
   		 panelCreatePosts.setVisibility(View.VISIBLE);
   		 panelCreatePosts.requestLayout();
   		 panelCreatePosts.startAnimation(slideIn);
   	 	}
    }
	
	private void setUpMapIfNeeded(boolean skipNullCheck) {
		if (skipNullCheck || map==null){
			map = mapView.getMap();
			mapClusterManager = new ClusterManager<LocationHistory>(activity, map);
			mapClusterManager.setRenderer(new DefaultClusterRenderer<LocationHistory>(activity, map, mapClusterManager));
	    	if (map!=null){
	    		map.setOnMapClickListener(this);
	    		
	    		map.setOnCameraChangeListener(mapClusterManager);
	    		map.setOnMarkerClickListener(mapClusterManager);
//	    		map.setOnMarkerClickListener(this);
	    		
	    		mapClusterManager.setOnClusterClickListener(this);
	    		mapClusterManager.setOnClusterItemClickListener(this);
	    		
	    		map.setMyLocationEnabled(true);
	    		addLocationHistoryMarkers();
	    	}
	    	else activity.showWarningDialog(activity.getString(R.string.error_no_google_maps));
		}
    }
	
	private void addLocationHistoryMarkers(){
		List<LocationHistory> locationhistory = new ArrayList<LocationHistory>();
		locationhistory = LocationHistoryManager.getInstance(activity).getLocationHistory();
		Helpers.addMarkersToMap(locationhistory, mapClusterManager);
	}
	
	@Override
	public boolean onBackPressed(){
		if (panelCreatePosts.getVisibility()==View.VISIBLE){
			hideReplyToPostPannel();
			return false;
		}
		return true;
	}
	
	private void newPostIfNeeded(){
		if (currentPost==null){
			currentPost = new Post();
		}
	}
	
	protected void setupWidgetsViewElements() {
		buttonPost = ((ImageButton)view.findViewById(R.id.button_post));
        buttonPost.setOnClickListener(this);
        
        cancelButton = ((ImageButton)view.findViewById(R.id.button_cancel));
        cancelButton.setOnClickListener(this);
        
        /*if (!Facade.getInstance(activity).wasTutorialMarkerRun()){
		    tutorialMarker = view.findViewById(R.id.tutorial_marker);
		    if (tutorialMarker!=null)
		    	tutorialMarker.setVisibility(View.VISIBLE);
		    closeTutorialMarker = (ImageButton)view.findViewById(R.id.button_close_tutorial_marker);
		    closeTutorialMarker.setOnClickListener(this);
        }*/
        
        inputMessage = (EditText)view.findViewById(R.id.editor_message);
        panelCreatePosts = view.findViewById(R.id.create_post_pannel);
	}

	@Override
	public boolean onClusterItemClick(LocationHistory item) {
		activity.analytics.recordEvent_WritePost_MarkerClick();
		if (map.getCameraPosition().zoom<13){
			animateMapCamera(item.getPosition(), 13);
		}
		else{
			createPost(item.getPosition());
		}
		return true;
	}

	@Override
	public boolean onClusterClick(Cluster<LocationHistory> cluster) {
		animateMapCamera(cluster.getPosition(), map.getCameraPosition().zoom+2);
		return true;
	}

}
