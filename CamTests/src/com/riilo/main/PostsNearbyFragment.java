package com.riilo.main;

import java.util.Arrays;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


import com.riilo.main.R;
import com.riilo.interfaces.FragmentBase;
import com.riilo.interfaces.ILocationListener;
import com.riilo.interfaces.IPostsListener;
import com.riilo.main.AnalyticsWrapper.EventLabel;
import com.riilo.utils.TutorialFactory;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PostsNearbyFragment 
				extends FragmentBase 
				implements OnItemClickListener, 
							ILocationListener, 
							OnClickListener{
	
	private static final String TAG = "<<<<<<<<<<<<<<PostsNearbyFragment>>>>>>>>>>>>>>";
	
    private MainActivity activity;
    private View view;
    private Button buttonRefresh;
    
 	ListView postsListView;
 	
 	private LocationHistory lastKnownLocation;
 	
 	private TutorialFactory tutorial;
 	
 	public PostsNearbyFragment(){
		super();
		Log.d("PostsNearbyFragment", "PostsNearbyFragment constructor");
	}
 	
 	public void onAttach(Activity activity){
 		this.activity = (MainActivity)activity;
 		super.onAttach(activity);
 	}
	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInsatnceState){
 		view = inflater.inflate(R.layout.posts_lists_layout, container, false); 
 		setupWidgetsViewElements();
 		
 		pullToRefreshLayout =  (PullToRefreshLayout) view.findViewById(R.id.ptr_layout_fragment);
 		ActionBarPullToRefresh.from(activity)
 			.options(Options.create().scrollDistance(.33f).build())
 			.allChildrenArePullable()
 			.listener(this)
 			.setup(pullToRefreshLayout);
 		
 		setupTutorials();
 		
 		return view;
 	}
 	
 	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastKnownLocation = Facade.getInstance(activity).getLastKnownLocation();
//        activity.initLocationClient(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL, FASTEST_INTERVAL);//TODO
 	}
 	
 	@Override
	public void onStart(){
		super.onStart();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_item_layout, adapterData, activity.deviceId, true);
		}		
		postsListView.setAdapter(adapter);
		
		List<Post> nearbyPosts = PostsCache.getInstance(activity).getNearbyPosts();
		if (Helpers.renewList(adapterData, nearbyPosts)){
			adapter.notifyDataSetChanged();
		}
		
		if (adapterData==null || adapterData.size()==0){
			buttonRefresh.setVisibility(View.VISIBLE);
		}
		
 	}
 	
	protected void setupWidgetsViewElements() {
		postsListView = (ListView)view.findViewById(R.id.posts_listView);
		postsListView.setOnItemClickListener(this);
		buttonRefresh = (Button)view.findViewById(R.id.button_refresh);
		buttonRefresh.setOnClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
				
		activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_nearby);
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
		activity.setAnimationType(StringKeys.ANIMATION_TYPE_SLIDE_IN_RIGHT);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if (location!=null){
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, null);
			PostsCache.getInstance(activity).getNearbyPosts(latLong[0], latLong[1], this, false);
			
			for(Post p : adapterData){
				if (p.getDistanceFromCurLoc()==-1){
					p.setDistanceFromCurLoc(location);
				}
			}
			
			if (adapterData.size()>0)
				buttonRefresh.setVisibility(View.GONE);
			
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		activity.analytics.recordEvent_General_PullToRefresh(EventLabel.tab_nearby);
		Location location = ((BaseActivity) getActivity()).getLocation();
		if (location!=null){
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, lastKnownLocation);
			PostsCache.getInstance(activity).getNearbyPosts(latLong[0], latLong[1], this, true);
		}
		else{
			this.pullToRefreshLayout.setRefreshComplete();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.button_refresh:
			buttonRefresh.setVisibility(View.GONE);
			Location location = ((BaseActivity) getActivity()).getLocation();
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, lastKnownLocation);
			PostsCache.getInstance(activity).getNearbyPosts(latLong[0],	latLong[1],	this, true);
			break;
		}
	}
	
	private void setupTutorials(){
		List<Integer> firstTutorial = Arrays.asList(R.layout.tutorial_nearby_dialog);
		tutorial = new TutorialFactory(activity, (ViewGroup) view, firstTutorial);
		tutorial.startTutorial(true);
	}

	@Override
	public void retrievedPosts(List<Post> newPosts){
		super.retrievedPosts(newPosts);
		buttonRefresh.setVisibility(View.GONE);
	}
}
