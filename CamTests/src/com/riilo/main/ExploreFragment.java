package com.riilo.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.riilo.interfaces.ILocationListener;
import com.riilo.interfaces.UIListener;
import com.riilo.main.AnalyticsWrapper.EventLabel;
import com.riilo.utils.ExpandAnimation;
import com.riilo.utils.TutorialFactory;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ExploreFragment 
		extends Fragment
		implements ILocationListener, OnCameraChangeListener, OnMarkerClickListener, UIListener, OnItemClickListener, OnMapClickListener{

	private static final String TAG = "<<<<<<<<ExploreFragment>>>>>>>>";
	
	private GoogleMap map;
	private View view;
	private MapView mapView;
	private MainActivity activity;
	
	private PostsCache postsCache;
	
	private boolean mapCameraAnimationRun = false; 
	private Timer timer;
	private View progressBar;
	private ListView listView;
	private LinearLayout contentView;
	private LinearLayout mapWrapper;
	private View errorNoPosts;
	private View errorMoreZoom;
		
	private PostListItemAdapter adapter;
	private List<Post> adapterData;
	private long currentSelectedItem=-1;
	
	private TutorialFactory tutorial;
	boolean showItemClickTutorial;
	
	@Override
	public void onAttach(Activity activity){
 		this.activity = (MainActivity)activity;
 		super.onAttach(activity);
 	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postsCache = PostsCache.getInstance(activity);
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
    	
    	setupTutorials();
    	
    	Log.d(TAG, "onCreateView");
    	
    	return view;
    }
	
	@Override
    public void onStart() {
    	super.onStart();  	
    	setUpMapIfNeeded();
    	setupWidgetsViewElements();
    	PostsCache.getInstance(activity).getPostGroupsOnMap(map, new Handler(), this);
    	getPostsOnMap(map.getCameraPosition());
    }
	
	@Override
	public void onLocationChanged(Location location) {
		if (!mapCameraAnimationRun){
			animateMapCamera(new LatLng(location.getLatitude(), location.getLongitude()), 7.5f);
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
        postsCache.clear_mapPosts();
        map.clear();
        if (null != mapView)
        	mapView.onPause();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mapCameraAnimationRun", mapCameraAnimationRun);
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
		
		progressBar = view.findViewById(R.id.progressBar);
		listView = (ListView) view.findViewById(R.id.posts_list_view);
		adapterData = new ArrayList<Post>();
		adapter = new PostListItemAdapter(activity, R.layout.post_item_layout, adapterData, activity.deviceId, false);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		contentView = (LinearLayout) view.findViewById(R.id.content);
		mapWrapper = (LinearLayout) view.findViewById(R.id.map_wrapper);
		
		errorMoreZoom = view.findViewById(R.id.err_more_zoom);
		errorNoPosts = view.findViewById(R.id.err_no_posts);
	}

	private void setUpMapIfNeeded() {
		map = mapView.getMap();
		if (map!=null){
			map.setMyLocationEnabled(true);
			map.setOnCameraChangeListener(this);
			map.setOnMarkerClickListener(this);
			map.setOnMapClickListener(this);
			Helpers.addPostsToMap(postsCache.getExplore_onMapPostGroups(), map);
		}
	}
	
	private void animateMapCamera(LatLng location, float zoom){
		CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(location.latitude, location.longitude), zoom);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
		map.animateCamera(update);
		mapCameraAnimationRun = true;
		activity.analytics.recordEvent_Explore_AutoCameraChange();
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		activity.analytics.recordEvent_Explore_MapExplore();
		getPostsOnMap(position);
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		activity.analytics.recordEvent_Explore_PostClickExplore();
		animateMapCamera(marker.getPosition(), 7.7f);
		return true;
	}

	private void getPostsOnMap(CameraPosition position){
		timer.cancel();
		timer.purge();
		timer = new Timer();
		final CameraPosition camPos = position;
		final Handler handler = new Handler();
		resetPreviouslySelectedItem();
		errorNoPosts.setVisibility(View.GONE);
		if (camPos.zoom>=7.5){
			errorMoreZoom.setVisibility(View.GONE);
			timer.schedule(new TimerTask() {
		
			@Override
			public void run() {
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							LatLng farLeftVisibleEdge = map.getProjection().getVisibleRegion().farLeft;
							final double distance = Helpers.distanceFrom(camPos.target.latitude, camPos.target.longitude, farLeftVisibleEdge.latitude, farLeftVisibleEdge.longitude)*0.7;
							PostsCache.getInstance(activity).getPostsOnMap(camPos.target.latitude, camPos.target.longitude, distance, map, handler, ExploreFragment.this);
						}
					});
			}}, 1000);
		}
		else {
			errorMoreZoom.setVisibility(View.VISIBLE);
			resetList();
		}
	}
	
	@Override
	public void onLoadStart() {
		progressBar.setVisibility(View.VISIBLE);
		resetList();
		errorNoPosts.setVisibility(View.GONE);
	}

	@Override
	public void onLoadEnd(List<Post> posts, boolean isMapPostGroups) {
		progressBar.setVisibility(View.GONE);
		if (posts !=null && posts.size() > 0){
			adapterData.clear();
			if (Helpers.renewList(adapterData, posts))
				adapter.notifyDataSetChanged();
			
			listView.setVisibility(View.VISIBLE);
			shrinkMap();
			
			setupTutorialsOnLoadEnd();
		}
		else{
			if (!isMapPostGroups){
				resetList();
				errorNoPosts.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		if (currentSelectedItem==index){
			activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_explore);
			Intent postViewIntent = new Intent(activity, PostViewActivity.class);
			postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
			startActivity(postViewIntent);
		}
		else{
			selectItem(post, index);
		}
		
	}
	
	private void shrinkMap(){
		LinearLayout.LayoutParams mapWrapperLayoutParams = (android.widget.LinearLayout.LayoutParams) mapWrapper.getLayoutParams();
		LinearLayout.LayoutParams contentViewLayoutParams = (android.widget.LinearLayout.LayoutParams) contentView.getLayoutParams();
		
		if (mapWrapperLayoutParams.weight>contentViewLayoutParams.weight){
			ExpandAnimation mapWrapperAnimation = new ExpandAnimation((LinearLayout) mapWrapper, 0.8f, 0.5f);
			mapWrapperAnimation.setDuration(200);
			mapWrapper.startAnimation(mapWrapperAnimation);
			
			ExpandAnimation vontenetViewAnimation = new ExpandAnimation((LinearLayout) contentView, 0.2f, 0.5f);
			vontenetViewAnimation.setDuration(200);
			contentView.startAnimation(vontenetViewAnimation);
		}
	}
	
	private void growMap(){
		LinearLayout.LayoutParams mapWrapperLayoutParams = (android.widget.LinearLayout.LayoutParams) mapWrapper.getLayoutParams();
		LinearLayout.LayoutParams contentViewLayoutParams = (android.widget.LinearLayout.LayoutParams) contentView.getLayoutParams();
		
		if (mapWrapperLayoutParams.weight==contentViewLayoutParams.weight){
			ExpandAnimation mapWrapperAnimation = new ExpandAnimation((LinearLayout) mapWrapper, 0.5f, 0.8f);
			mapWrapperAnimation.setDuration(200);
			mapWrapper.startAnimation(mapWrapperAnimation);
			
			ExpandAnimation vontenetViewAnimation = new ExpandAnimation((LinearLayout) contentView, 0.5f, 0.2f);
			vontenetViewAnimation.setDuration(200);
			contentView.startAnimation(vontenetViewAnimation);
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		growMap();
	}
	
	private void resetList(){
		listView.setVisibility(View.GONE);
		currentSelectedItem = -1;
	}
	
	private void selectItem(Post currentPost, long index){
		resetPreviouslySelectedItem();
		currentSelectedItem = index;
		if (currentPost!=null)
			if (currentPost.getMarker()!=null)
				currentPost.getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_riilo));
	}
	
	private void resetPreviouslySelectedItem(){
		if (currentSelectedItem>=0){
			Post previouslySelectedPost = adapterData.get((int) currentSelectedItem);
			currentSelectedItem = -1;
			if (previouslySelectedPost!=null)
				if (previouslySelectedPost.getMarker()!=null)
					previouslySelectedPost.getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_history));
		}
	}

	private void setupTutorials(){
		List<Integer> firstTutorial = Arrays.asList(R.layout.tutorial_welcome_dialog, R.layout.tutorial_navigate_to_write_post_dialog);
		List<Integer> secondTutorial = Arrays.asList(R.layout.tutorial_explore_dialog);
		if (!Facade.getInstance(activity).wereTutorialsRun(firstTutorial)){
			tutorial = new TutorialFactory(activity, (ViewGroup) view, firstTutorial);
			tutorial.startTutorial(true);
		}
		else{
			tutorial = new TutorialFactory(activity, (ViewGroup) view, secondTutorial);
			tutorial.startTutorial(true);
		}
	}
	
	private void setupTutorialsOnLoadEnd(){
		List<Integer> dependencies = Arrays.asList(R.layout.tutorial_welcome_dialog, R.layout.tutorial_navigate_to_write_post_dialog, R.layout.tutorial_explore_dialog);
		List<Integer> clickItemTutorial = Arrays.asList(R.layout.tutorial_click_post_to_see_location_dialog);
		if (Facade.getInstance(activity).wereTutorialsRun(dependencies)){
			tutorial = new TutorialFactory(activity, (ViewGroup) view, clickItemTutorial);
			tutorial.startTutorial(true);
		}
	}
}
