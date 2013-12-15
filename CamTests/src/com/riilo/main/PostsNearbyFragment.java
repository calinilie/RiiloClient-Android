package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;

import com.riilo.main.R;
import com.riilo.interfaces.ILocationListener;
import com.riilo.main.AnalyticsWrapper.EventLabel;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PostsNearbyFragment 
				extends Fragment 
				implements OnItemClickListener, 
							ILocationListener, 
							OnRefreshListener,
							OnClickListener{
	
    private MainActivity activity;
    private View view;
    private Button buttonRefresh;
    
    
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
		
		if (adapterData==null || adapterData.size()==0){
			buttonRefresh.setVisibility(View.VISIBLE);
		}
 	}
 	
 	/*@Override
 	protected void onRestoreInstanceState (Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		
 	}*/
 	
	protected void setupWidgetsViewElements() {
		postsListView = (ListView)view.findViewById(R.id.posts_listView);
		postsListView.setOnItemClickListener(this);
		buttonRefresh = (Button)view.findViewById(R.id.button_refresh);
		buttonRefresh.setOnClickListener(this);
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
//		Log.d(">>>>>>>>>>>>>>>>>", (String) activity.getSpinner().getItem(2));
//		Toast.makeText(activity, "Location Changed "+(location==null), Toast.LENGTH_SHORT).show();
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
							activity.getSpinnerAdapter(),
							activity.getSpinnerAdapter().getItem(2),
							pullToRefreshAttacher,
							buttonRefresh,
							false,
							StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER);
			for(Post p : adapterData){
				if (p.getDistanceFromCurLoc()==-1){
					refreshAdapter = true;
					p.setDistanceFromCurLoc(location);
				}
			}
			if (refreshAdapter){
				adapter.notifyDataSetChanged();
			}
		}
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
							activity.getSpinnerAdapter(),
							activity.getSpinnerAdapter().getItem(2),
							pullToRefreshAttacher,
							buttonRefresh,
							true,
							StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER);
			adapter.notifyDataSetChanged();
		}
		else{
			pullToRefreshAttacher.setRefreshComplete();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.button_refresh:
			buttonRefresh.setVisibility(View.GONE);
			Location location = ((BaseActivity) getActivity()).getLocation();
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, lastKnownLocation);
			adapterData = PostsCache
					.getInstance(activity)
					.getNearbyPosts(
							latLong[0],
							latLong[1],
							adapter,
							adapterData,
							activity.getSpinnerAdapter(),
							activity.getSpinnerAdapter().getItem(2),
							pullToRefreshAttacher,
							buttonRefresh,
							true,
							StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER);
			adapter.notifyDataSetChanged();
			break;
		}
	}

}
