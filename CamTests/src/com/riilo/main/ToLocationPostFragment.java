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
import android.util.Log;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.riilo.interfaces.IBackButtonListener;
import com.riilo.interfaces.ILocationListener;
import com.riilo.main.AnalyticsWrapper.EventLabel;

public class ToLocationPostFragment extends Fragment implements OnMapClickListener, OnClickListener, ILocationListener, IBackButtonListener, OnMarkerClickListener{
	
	private GoogleMap map;
	private ImageButton buttonPost;
	private ImageButton cancelButton;
	private EditText inputMessage;
	private View panelCreatePosts;
	
//	private boolean mapCameraAnimationRun = false; 
	private Post currentPost;
	private Marker marker;
	
	private View view;
	private MapView mapView;
	private BaseActivity activity;
	
	@Override
	public void onAttach(Activity activity){
 		this.activity = (BaseActivity)activity;
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
            MapsInitializer.initialize(getActivity());
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
    	newPostIfNeeded();
    	
    	LocationHistoryManager.getInstance(activity).getLocationHistory(map);
    	
//    	mapCameraAnimationRun = false;
    }
    
    /*@Override
	public void onResume() {
        super.onResume();
        if(mapView!=null){
        	mapView.onResume();	
        }
        //Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onResume()");
    }
   
    @Override
    public void onPause() {
        super.onPause();
        if (null != mapView)
        	mapView.onPause();
        //Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onPause()");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mapView)
        	mapView.onDestroy();
        //Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onDestroy()");
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mapView)
        	mapView.onSaveInstanceState(outState);
        //Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onSaveInstanceState()");
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mapView)
        	mapView.onLowMemory();
        //Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onLowMemory()");
    }*/
    
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
   				Toast.makeText(activity, getString(R.string.invalid_post_empty), Toast.LENGTH_LONG).show();
   			}
   			break;
   		case R.id.button_cancel:
   			hideReplyToPostPannel();
   			break;
   		}
   	}
    
   	@Override
    public void onLocationChanged(Location location) {
//    	if (!mapCameraAnimationRun){
//    		if (location.getAccuracy()<2000){
//	    		animateMapCamera(new LatLng(location.getLatitude(), location.getLongitude()));
//    		}
//    	}
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
	
	/*private void animateMapCamera(LatLng location){
		animateMapCamera(location, 10);
	}*/
	
	private void animateMapCamera(LatLng location, int zoom){
		CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(location.latitude, location.longitude), zoom);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
		map.animateCamera(update);
//		mapCameraAnimationRun = true;
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
	
	private void setUpMapIfNeeded() {
//		if (map==null){
			map = mapView.getMap();
	    	if (map!=null){
	    		map.setOnMapClickListener(this);
//	    		map.setMyLocationEnabled(true);
	    		map.setOnMarkerClickListener(this);
	    		addLocationHistoryMarkers();
	    	}
	    	else activity.showWarningDialog("Ouch, something went terribly wrong. Please keep calm and try again; if you still get this error your phone might not support Google Maps, and this app relies heavily on Google Maps.");
//		}
    }
	
	private void addLocationHistoryMarkers(){
		List<LocationHistory> locationhistory = new ArrayList<LocationHistory>();
		locationhistory = LocationHistoryManager.getInstance(activity).getLocationHistory();
//		for(LocationHistory loc : locationhistory){
//			map.addMarker(new MarkerOptions().position(loc.getLatLng())//.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plusone_standard_off_client))
//					);
//		}
		Helpers.addMarkersToMap(locationhistory, map);
	}
	
	@Override
	public boolean onBackPressed(){
		//Log.d(">>>>>>>>>>>>>>>>>>>>>>>>", "fragment.onBackPressed");
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
        
        inputMessage = (EditText)view.findViewById(R.id.editor_message);
        
        panelCreatePosts = view.findViewById(R.id.create_post_pannel);
	}

	
	@Override
	public boolean onMarkerClick(Marker marker) {
		activity.analytics.recordEvent_WritePost_MarkerClick();
		if (map.getCameraPosition().zoom<13){
			animateMapCamera(marker.getPosition(), 13);
		}
		else{
			createPost(marker.getPosition());
		}
		return true;
	}

}
