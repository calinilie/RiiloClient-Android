package com.riilo.main;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.riilo.main.AnalyticsWrapper.EventLabel;
import com.riilo.utils.TutorialFactory;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class ToLocationPostActivity extends BaseActivity implements OnClusterClickListener<LocationHistory>, OnClusterItemClickListener<LocationHistory>, OnClickListener, OnMapClickListener{

	@SuppressWarnings("unused")
	private static final String TAG = "ToLocationPostActivity>>>>>>>>>>>>>>>>>>>>>>>>>>>";
	
	private ImageButton buttonPost;
	private ImageButton cancelButton;
	private EditText inputMessage;
	private View panelCreatePosts;
//	private View tutorialMarker;
//	private ImageButton closeTutorialMarker;
	private TutorialFactory tutorial;
	
	private GoogleMap map;
	private ClusterManager<LocationHistory> clusterManager;
	
	private boolean mapCameraAnimationRun = false;
	private Post currentPost;
	private Marker marker;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_post_layout);
		setupTutorials();
		initLocationClient(LocationRequest.PRIORITY_LOW_POWER, 2000, 1000);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		setupWidgetsViewElements();
		
		setUpMapIfNeeded();
		newPostIfNeeded();
		LocationHistoryManager.getInstance(this).getRemoteLocationHistory(clusterManager, map);
	}
	
	@Override
	public void onStop(){
		super.onStop();
	}
	
	private void setUpMapIfNeeded() {
	    if (map == null) {
	    	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
	                .getMap();
	        if (map != null) {
	        	clusterManager = new ClusterManager<LocationHistory>(this, map);
	        	clusterManager.setRenderer(new DefaultClusterRenderer<LocationHistory>(this, map, clusterManager));
	        	
	        	map.setOnCameraChangeListener(clusterManager);
	    		map.setOnMarkerClickListener(clusterManager);
	    		
	    		clusterManager.setOnClusterClickListener(this);
	    		clusterManager.setOnClusterItemClickListener(this);
	    		map.setOnMapClickListener(this);
	    		
	        	map.setMyLocationEnabled(true);
	        	//==============================
	        	map.clear();
	        	LocationHistoryManager.getInstance(this).locationHistoryMarkersRemoved();
	        	
	        	MapRenderer rederer = new MapRenderer();
	    		rederer.execute();
	        }
	    }
	}
	 
	@Override
	public void onLocationChanged(Location location){
		super.onLocationChanged(location);
		if (!mapCameraAnimationRun){
    		if (location.getAccuracy()<2000){
	    		animateMapCamera(new LatLng(location.getLatitude(), location.getLongitude()), 5);
    		}
    	}
	}
	
	@Override
   	public void onClick(View v) {
   		switch (v.getId()){
   		case R.id.button_post:
   			analytics.recordEvent_WritePost_ButtonClick(EventLabel.button_post);
   			String message = inputMessage.getText().toString();
   			if (message!=null && !message.isEmpty()){
	   			currentPost.setMessage(message);
	   			currentPost.setUserAtLocation(location, currentPost.getLatitude(), currentPost.getLongitude());
	   			currentPost.setDateCreated(Calendar.getInstance().getTime());
	   			currentPost.setUserId(deviceId);
	
	       		Intent intentPost= new Intent(this, WorkerService.class);
	       		intentPost.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_POST);
	       		intentPost.putExtra(StringKeys.POST_BUNDLE, currentPost.toBundle());
	           	startService(intentPost);
	           	
	    		Intent postViewIntent = new Intent(this, PostViewActivity.class);
	    		postViewIntent.putExtra(StringKeys.POST_BUNDLE, currentPost.toBundle());
	    		startActivity(postViewIntent);
	    		
	    		inputMessage.setText("");
   			}
   			else{
   				Toast.makeText(this, getString(R.string.error_post_empty), Toast.LENGTH_LONG).show();
   			}
   			break;
   		case R.id.button_cancel:
   			analytics.recordEvent_WritePost_ButtonClick(EventLabel.button_cancel);
   			hideReplyToPostPannel();
   			break;
   		}
   	}
	
	private void showCreatePostPanel(){
		Animation slideIn = AnimationUtils.loadAnimation(this, 
	   			 R.anim.slide_in_bottom);
	   	if (panelCreatePosts.getVisibility() == View.GONE){
		   	panelCreatePosts.setVisibility(View.VISIBLE);
		   	panelCreatePosts.requestLayout();
		   	panelCreatePosts.startAnimation(slideIn);
	   	}
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
	
	@Override
	public boolean onClusterItemClick(LocationHistory item) {
		analytics.recordEvent_WritePost_MarkerClick();
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
		analytics.recordEvent_WritePost_MapCLusterClick();
		animateMapCamera(cluster.getPosition(), map.getCameraPosition().zoom+2);
		return true;
	}
	
	private void animateMapCamera(LatLng location, float zoom){
		CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(location.latitude, location.longitude), zoom);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
		map.animateCamera(update);
		mapCameraAnimationRun = true;
	}
	
	@Override
	protected void setupWidgetsViewElements() {
		buttonPost = ((ImageButton)findViewById(R.id.button_post));
        buttonPost.setOnClickListener(this);
        
        cancelButton = ((ImageButton)findViewById(R.id.button_cancel));
        cancelButton.setOnClickListener(this);
        
        /*if (!Facade.getInstance(this).wasTutorialMarkerRun()){
		    tutorialMarker = findViewById(R.id.tutorial_marker);
		    if (tutorialMarker!=null)
		    	tutorialMarker.setVisibility(View.VISIBLE);
		    closeTutorialMarker = (ImageButton)findViewById(R.id.button_close_tutorial_marker);
		    closeTutorialMarker.setOnClickListener(this);
        }*/
        
        inputMessage = (EditText)findViewById(R.id.editor_message);
        panelCreatePosts = findViewById(R.id.create_post_pannel);
	}
	
	private void newPostIfNeeded(){
		if (currentPost==null){
			currentPost = new Post();
		}
	}
	
	private class MapRenderer extends AsyncTask<Void, Void, Void>{

		private List<LocationHistory> list;
		
		@Override
		protected Void doInBackground(Void... params) {
			list = LocationHistoryManager.getInstance(getApplicationContext()).getLocationHistory();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
			Helpers.addMarkersToMap(list, clusterManager);
		}
		
	}
	
	private void hideReplyToPostPannel(){
	   	 InputMethodManager inputManager = (InputMethodManager)
	                getSystemService(Context.INPUT_METHOD_SERVICE);
	   	 if (inputManager!=null)
	   		 if (getCurrentFocus()!=null)
			    	 inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
			                    InputMethodManager.HIDE_NOT_ALWAYS);
	   	 
	   	 Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(),
	                R.anim.slide_out_bottom);
	   	 
	   	 if (panelCreatePosts.getVisibility()==View.VISIBLE){
	   		 panelCreatePosts.startAnimation(slideOut);
	   		 panelCreatePosts.requestLayout();
	   		 panelCreatePosts.setVisibility(View.GONE);
   	 	}
    }

	@Override
	public void onMapClick(LatLng position) {
		analytics.recordEvent_WritePost_MapClick();
		createPost(position);
	}
	
	private void setupTutorials(){
		List<Integer> firstTutorial = Arrays.asList(R.layout.tutorial_how_to_write_a_post_dialog);
		List<Integer> secondTutorial = Arrays.asList(R.layout.tutorial_location_history_dialog);
		if (!Facade.getInstance(this).wereTutorialsRun(firstTutorial)){
			tutorial = new TutorialFactory(this, (ViewGroup) findViewById(R.id.write_post_layout), firstTutorial);
			tutorial.startTutorial(true);
		}
		else{
			tutorial = new TutorialFactory(this, (ViewGroup) findViewById(R.id.write_post_layout), secondTutorial);
			tutorial.startTutorial(true);
		}
	}
	
}
