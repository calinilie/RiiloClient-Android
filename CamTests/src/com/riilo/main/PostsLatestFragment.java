package com.riilo.main;

import java.util.Arrays;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import com.riilo.main.R;
import com.riilo.interfaces.FragmentBase;
import com.riilo.interfaces.IPostsListener;
import com.riilo.interfaces.ILocationListener;
import com.riilo.main.AnalyticsWrapper.EventLabel;
import com.riilo.utils.TutorialFactory;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PostsLatestFragment 
		extends FragmentBase
		implements ILocationListener{
	
    private MainActivity activity;
    private View view;
    
 	ListView postsListView; 	
 	
 	private TutorialFactory tutorial;
 	
 	public void onAttach(Activity activity){
 		this.activity = (MainActivity)activity;
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
	public void onStart(){
		super.onStart();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_item_layout, adapterData, activity.deviceId, true);
		}
		postsListView.setAdapter(adapter);
		
		List<Post> latestPosts = PostsCache.getInstance(activity).getLatestPosts(this, false);
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
 		activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_latest);
		super.onItemClick(parentView, view, position, index);
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
		super.onRefreshStarted(view);
		activity.analytics.recordEvent_General_PullToRefresh(EventLabel.tab_latest);
		PostsCache.getInstance(getActivity())
			.getLatestPosts(this, true);
	}
	
	private void setupTutorials(){
		List<Integer> firstTutorial = Arrays.asList(R.layout.tutorial_swipe_dialog);
		List<Integer> secondTutorial = Arrays.asList(R.layout.tutorial_latest_dialog);
		if (!Facade.getInstance(activity).wereTutorialsRun(firstTutorial)){
			tutorial = new TutorialFactory(activity, (ViewGroup) view, firstTutorial);
			tutorial.startTutorial(true);
		}
		else{
			tutorial = new TutorialFactory(activity, (ViewGroup) view, secondTutorial);
			tutorial.startTutorial(true);
		}
	}
}
