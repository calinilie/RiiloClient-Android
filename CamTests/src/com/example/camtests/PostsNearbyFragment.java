package com.example.camtests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;

import com.example.camtests.AnalyticsWrapper.EventLabel;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PostsNearbyFragment 
				extends Fragment 
				implements OnItemClickListener, ILocationListener, OnRefreshListener{

	private boolean distancesComputed;
	
	// Update frequency in milliseconds
    private static final int UPDATE_INTERVAL = 2000;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = 1000;
	
    private MainActivity activity;
    private View view;
    
    
	PostListItemAdapter adapter;
 	List<Post> adapterData = new ArrayList<Post>();
 	ListView postsListView;
 	
 	private LocationHistory lastKnownLocation;
 	
 	private PullToRefreshAttacher pullToRefreshAttacher;
 	
 	public void onAttach(Activity activity){
 		this.activity = (MainActivity)activity;
 		super.onAttach(activity);
 	}
	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInsatnceState){
 		view = inflater.inflate(R.layout.posts_lists_layout, container, false); 
 		setupWidgetsViewElements();
 		
        pullToRefreshAttacher = activity
                .getPullToRefreshAttacher();
        pullToRefreshAttacher.addRefreshableView(postsListView, this);
 		
 		return view;
 	}
 	
 	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastKnownLocation = Facade.getInstance(activity).getLastKnownLocation();
//        activity.initLocationClient(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL, FASTEST_INTERVAL);//TODO
		
		if (savedInstanceState!=null && savedInstanceState.containsKey(StringKeys.POST_LIST_PARCELABLE)){
			PostsListParcelable parcelable = savedInstanceState.getParcelable(StringKeys.POST_LIST_PARCELABLE); 
			if (adapter!=null && Helpers.renewList(adapterData, parcelable.getPostsList())){
				adapter.notifyDataSetChanged();
			}
		}
 	}
 	
 	@Override
	public void onStart(){
		super.onStart();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_list_view_item_layout, adapterData, activity.deviceId, true);
		}		
		postsListView.setAdapter(adapter);
		
		List<Post> nearbyPosts = PostsCache.getInstance(activity).getNearbyPosts();
		if (Helpers.renewList(adapterData, nearbyPosts)){
			adapter.notifyDataSetChanged();
		}
 	}
 	
 	/*@Override
 	protected void onRestoreInstanceState (Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		
 	}*/
 	
	protected void setupWidgetsViewElements() {
		postsListView = (ListView)view.findViewById(R.id.posts_listView);
		postsListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		
//		long conversationId = post.getConversationId();
		
		activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_nearby, post.getConversationId());
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(activity, "Location Changed "+(location==null), Toast.LENGTH_SHORT).show();
		if (location!=null){
			boolean refreshAdapter = false;
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, null);
			adapterData = PostsCache
					.getInstance(activity)
					.getNearbyPosts(
							latLong[0],
							latLong[1],
							adapter,
							adapterData,
							activity.getTabs().get(2),
							pullToRefreshAttacher,
							false,
							StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER);
			for(Post p : adapterData){
				if (p.getDistanceFromCurLoc()==-1){
					refreshAdapter = true;
					p.setDistanceFromCurLoc(location, false);
				}
			}
			if (refreshAdapter){
				adapter.notifyDataSetChanged();
			}
		}		
			//TODO change logic in method above
		/*if (!distancesComputed){
			lastKnownLocation = Facade.getInstance(activity).getLastKnownLocation();
			//compute distance to currentLocation
			if (location!=null){
				double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, lastKnownLocation);
				adapterData = PostsCache.getInstance(activity).getNearbyPosts(latLong[0], latLong[1], adapter, adapterData, activity.getTabs().get(2) , pullToRefreshAttacher, false, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER);
				for(Post p : adapterData){
					p.setDistanceFromCurLoc(location, false);
				}
				distancesComputed = true;
			}
			//if no currentlocation, get location fromHistory
			else{
				for(Post p : adapterData){
					p.setDistanceFromLastKnownLocation(lastKnownLocation, false);
				}
			}
			if (Facade.getInstance(activity).insertLocationToHistoryIfNeeded(location, lastKnownLocation)){
				//TODO change logic in method above
			}
			adapter.notifyDataSetChanged();
		}*/
	}

	@Override
	public void onRefreshStarted(View view) {
		activity.analytics.recordEvent_General_PullToRefresh(EventLabel.tab_nearby);
		Location location = ((BaseActivity) getActivity()).getLocation();
		if (location!=null){
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, lastKnownLocation);
			adapterData = PostsCache
					.getInstance(activity)
					.getNearbyPosts(
							latLong[0],
							latLong[1],
							adapter,
							adapterData,
							activity.getTabs().get(2),
							pullToRefreshAttacher,
							true,
							StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER);
			adapter.notifyDataSetChanged();
		}
		else{
			pullToRefreshAttacher.setRefreshComplete();
		}
		
	}
}
