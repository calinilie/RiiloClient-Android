package com.example.camtests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PostsNearbyFragment 
				extends Fragment 
				implements OnItemClickListener, ILocationListener{

	private boolean distancesComputed;
	
	// Update frequency in milliseconds
    private static final int UPDATE_INTERVAL = 2000;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = 1000;
	
    private BaseActivity activity;
    private View view;
    
    
	PostListItemAdapter adapter;
 	List<Post> adapterData = new ArrayList<Post>();
 	ListView postsList;
 	
 	private LocationHistory lastKnownLocation;
 	
 	public void onAttach(Activity activity){
 		this.activity = (BaseActivity)activity;
 		super.onAttach(activity);
 	}
	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInsatnceState){
 		view = inflater.inflate(R.layout.posts_lists_layout, container, false); 
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
 	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_view_layout_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}*/
 	
 	@Override
	public void onStart(){
		super.onStart();
		setupWidgetsViewElements();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_list_view_item_layout, adapterData, activity.deviceId, true);
		}		
		postsList.setAdapter(adapter);
		
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
		postsList = (ListView)view.findViewById(R.id.own_feed_posts_list_view);
		postsList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		
//		long conversationId = post.getConversationId();
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", "location Changed!" + distancesComputed);
		if (!distancesComputed){
			lastKnownLocation = Facade.getInstance(activity).getLastKnownLocation();
			//compute distance to currentLocation
			if (location!=null){
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
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, lastKnownLocation);
			adapterData = PostsCache.getInstance(activity).getNearbyPosts(latLong[0], latLong[1], adapter, adapterData, null, false, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC);
			adapter.notifyDataSetChanged();
		}
	}
}
