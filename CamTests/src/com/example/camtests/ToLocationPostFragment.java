package com.example.camtests;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.camtests.AnalyticsWrapper.EventLabel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.riilo.interfaces.IBackButtonListener;
import com.riilo.interfaces.ILocationListener;

public class ToLocationPostFragment extends Fragment implements OnMapClickListener, OnClickListener, ILocationListener, IBackButtonListener{
	
	private GoogleMap map;
	private Button buttonPost;
	private Button buttonTakePicture;
	private Button buttonChooseFromGallery;
	private EditText inputMessage;
	private View panelCreatePosts;
	
	private boolean mapCameraAnimationRun = false; 
	private Post currentPost;
	private Marker marker;
	
	private View view;
	private MapView mapView;
	private BaseActivity activity;
	
	@Override
	public void onAttach(Activity activity){
 		this.activity = (BaseActivity)activity;
 		Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onAttach()");
 		super.onAttach(activity);
 	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
//        super.initLocationClient(LocationRequest.PRIORITY_LOW_POWER, 4000, 2000); //TODO
        Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onCreate()");
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//    	setRetainInstance(false);
    	view = inflater.inflate(R.layout.write_post_layout, container, false);
    	
    	mapView =(MapView)view.findViewById(R.id.map);
    	mapView.onCreate(savedInstanceState);
    	mapView.onResume();
    	
    	try {
            MapsInitializer.initialize(getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    	Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onCreateView()");
    	return view;
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	map = mapView.getMap();
    	map.setOnMapClickListener(this);
    	
    	setupWidgetsViewElements();
    	newPostIfNeeded();
    	mapCameraAnimationRun = false;
    	Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onStart()");
    }
    
    /*@Override
	public void onResume() {
        super.onResume();
        if(mapView!=null){
        	mapView.onResume();	
        }
        Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onResume()");
    }
   
    @Override
    public void onPause() {
        super.onPause();
        if (null != mapView)
        	mapView.onPause();
        Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onPause()");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mapView)
        	mapView.onDestroy();
        Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onDestroy()");
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mapView)
        	mapView.onSaveInstanceState(outState);
        Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onSaveInstanceState()");
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mapView)
        	mapView.onLowMemory();
        Log.d(">>>>>>>>>mapFragment<<<<<<<<<<", "onLowMemory()");
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
   		}
   	}
    
   	@Override
    public void onLocationChanged(Location location) {
    	if (!mapCameraAnimationRun){
    		if (location.getAccuracy()<2000){
	    		animateMapCamera(new LatLng(location.getLatitude(), location.getLongitude()));
    		}
    	}
    }

	@Override
	public void onMapClick(LatLng location) {
		activity.analytics.recordEvent_WritePost_MapClick();
		animatePanelCreatePosts();
		animateMapCamera(location, 15);
		currentPost.setLatitude(location.latitude);
		currentPost.setLongitude(location.longitude);
		if (marker!=null)
			marker.remove();
		marker = map.addMarker(new MarkerOptions().position(location));
	}
	
	private void animateMapCamera(LatLng location){
		animateMapCamera(location, 10);
	}
	
	private void animateMapCamera(LatLng location, int zoom){
		CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(location.latitude, location.longitude), zoom);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
		map.animateCamera(update);
		mapCameraAnimationRun = true;
	}
	
	private void animatePanelCreatePosts(){
		Animation slideIn = AnimationUtils.loadAnimation(activity,
                R.anim.slide_in_top);
		if (panelCreatePosts.getVisibility()==View.INVISIBLE || panelCreatePosts.getVisibility()==View.GONE){
			panelCreatePosts.setVisibility(View.VISIBLE);
			panelCreatePosts.requestLayout();
			panelCreatePosts.startAnimation(slideIn);
		}
	}
	
	/*private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = mapView.getMap();
            
            // Check if we were successful in obtaining the map.
//            if (map != null) {
//            	
//            }
        }
    }*/
	
	@Override
	public boolean onBackPressed(){
		Log.d(">>>>>>>>>>>>>>>>>>>>>>>>", "fragment.onBackPressed");
		if (panelCreatePosts.getVisibility()==View.VISIBLE){
			panelCreatePosts.setVisibility(View.GONE);
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
		buttonPost = ((Button)view.findViewById(R.id.button_post));
        buttonPost.setOnClickListener(this);
        
        buttonTakePicture = ((Button)view.findViewById(R.id.button_take_picture));
        buttonTakePicture.setOnClickListener(this);
        
        buttonChooseFromGallery = ((Button)view.findViewById(R.id.button_choose_from_gallery));
        buttonChooseFromGallery.setOnClickListener(this);
        
        inputMessage = (EditText)view.findViewById(R.id.editor_message);
        
        panelCreatePosts = view.findViewById(R.id.create_post_pannel);
	}

}
