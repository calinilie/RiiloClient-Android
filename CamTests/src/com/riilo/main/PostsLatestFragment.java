package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import com.riilo.main.R;
import com.riilo.interfaces.ILocationListener;
import com.riilo.main.AnalyticsWrapper.EventLabel;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PostsLatestFragment 
		extends Fragment 
		implements OnItemClickListener, ILocationListener, OnRefreshListener{
	
    private BaseActivity activity;
    private View view;
    
    
	PostListItemAdapter adapter;
 	List<Post> adapterData = new ArrayList<Post>();
 	ListView postsListView;
 	
 	
 	private PullToRefreshAttacher pullToRefreshAttacher;

 	public void onAttach(Activity activity){
 		this.activity = (BaseActivity)activity;
 		super.onAttach(activity);
 	}
 	
 	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        activity.initLocationClient(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL, FASTEST_INTERVAL);//TODO
		
		if (savedInstanceState!=null && savedInstanceState.containsKey(StringKeys.POST_LIST_PARCELABLE)){
			PostsListParcelable parcelable = savedInstanceState.getParcelable(StringKeys.POST_LIST_PARCELABLE); 
			if (adapter!=null && Helpers.renewList(adapterData, parcelable.getPostsList())){
				adapter.notifyDataSetChanged();
			}
		}
 	}
	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInsatnceState){
 		view = inflater.inflate(R.layout.posts_lists_layout, container, false); 
 		setupWidgetsViewElements();

        // Now get the PullToRefresh attacher from the Activity. An exercise to the reader
        // is to create an implicit interface instead of casting to the concrete Activity
        pullToRefreshAttacher = ((MainActivity) getActivity())
                .getPullToRefreshAttacher();

        // Now set the ScrollView as the refreshable view, and the refresh listener (this)
        pullToRefreshAttacher.addRefreshableView(postsListView, this);
 		return view;
 	}
 	
 	@Override
	public void onStart(){
		super.onStart();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_list_view_item_layout, adapterData, activity.deviceId, true);
		}
		postsListView.setAdapter(adapter);
		
		List<Post> latestPosts = PostsCache.getInstance(activity).getLatestPosts(adapter, adapterData, pullToRefreshAttacher);
		if (Helpers.renewList(adapterData, latestPosts)){
			adapter.notifyDataSetChanged();
		}
 	}
 	
 	protected void setupWidgetsViewElements() {
		postsListView = (ListView)view.findViewById(R.id.posts_listView);
		postsListView.setOnItemClickListener(this);
	}
 	
 	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		
//		long conversationId = post.getConversationId();
		
		activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_latest, post.getConversationId());
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
	}
 	
 	
 	@Override
	public void onLocationChanged(Location location) {
		if (location!=null){
			boolean refreshAdapter = false;
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
		activity.analytics.recordEvent_General_PullToRefresh(EventLabel.tab_latest);
		PostsCache.getInstance(getActivity())
			.getLatestPosts(
				adapter, 
				adapterData, 
				pullToRefreshAttacher, 
				true);
	}

}
