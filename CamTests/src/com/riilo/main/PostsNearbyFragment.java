package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


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
import android.view.Menu;
import android.view.MenuInflater;
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
	
	private static final String TAG = "<<<<<<<<<<<<<<PostsNearbyFragment>>>>>>>>>>>>>>";
	
    private MainActivity activity;
    private View view;
    private Button buttonRefresh;
    
    
	PostListItemAdapter adapter;
 	List<Post> adapterData = new ArrayList<Post>();
 	ListView postsListView;
 	
 	private LocationHistory lastKnownLocation;
 	
 	private PullToRefreshLayout pullToRefreshLayout;
 	
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
    	inflater.inflate(R.menu.main_activity_layout_menu, menu);
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
				
		activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_nearby, post.getConversationId());
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if (location!=null){
//			TODO
//			boolean refreshAdapter = false;
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, null);
			Helpers.renewList(adapterData,
								PostsCache
										.getInstance(activity)
										.getNearbyPosts(
												latLong[0],
												latLong[1],
												adapter,
												adapterData,
												activity.getSpinnerAdapter(),
												activity.getSpinnerAdapter().getItem(2),
												this.pullToRefreshLayout,
												buttonRefresh,
												false,
												StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER));
			for(Post p : adapterData){
				if (p.getDistanceFromCurLoc()==-1){
					p.setDistanceFromCurLoc(location);
				}
			}
//			TODO
//			if (refreshAdapter){
//				adapter.notifyDataSetChanged();
//			}
			if (adapterData.size()>0)
				buttonRefresh.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		activity.analytics.recordEvent_General_PullToRefresh(EventLabel.tab_nearby);
		Location location = ((BaseActivity) getActivity()).getLocation();
		if (location!=null){
			double[] latLong = Helpers.setReqFrom_Latitude_and_Longitude(location, lastKnownLocation);
			PostsCache
					.getInstance(activity)
					.getNearbyPosts(
							latLong[0],
							latLong[1],
							adapter,
							adapterData,
							activity.getSpinnerAdapter(),
							activity.getSpinnerAdapter().getItem(2),
							this.pullToRefreshLayout,
							buttonRefresh,
							true,
							StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER);
			adapter.notifyDataSetChanged();
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
			
			Helpers.renewList(adapterData, 
					PostsCache
					.getInstance(activity)
					.getNearbyPosts(
							latLong[0],
							latLong[1],
							adapter,
							adapterData,
							activity.getSpinnerAdapter(),
							activity.getSpinnerAdapter().getItem(2),
							pullToRefreshLayout,
							buttonRefresh,
							true,
							StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER));
			
			adapter.notifyDataSetChanged();
			
			break;
		}
	}

}
