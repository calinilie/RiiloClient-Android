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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PostsLatestFragment 
		extends Fragment 
		implements OnItemClickListener, ILocationListener, OnRefreshListener, OnClickListener, AnimationListener{
	
    private MainActivity activity;
    private View view;
    
    
	PostListItemAdapter adapter;
 	List<Post> adapterData = new ArrayList<Post>();
 	ListView postsListView;
 	
 	View tutorialSwipe;
 	View tutorialSwipeContainer;
 	ImageButton buttonCloseTutorialSwipe;
 	
 	
 	private PullToRefreshLayout pullToRefreshLayout;

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
 		
 		return view;
 	}
 	
 	@Override
	public void onStart(){
		super.onStart();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_list_view_item_layout, adapterData, activity.deviceId, true);
		}
		postsListView.setAdapter(adapter);
		
		List<Post> latestPosts = PostsCache.getInstance(activity).getLatestPosts(adapter, adapterData, this.pullToRefreshLayout);
		if (Helpers.renewList(adapterData, latestPosts)){
			adapter.notifyDataSetChanged();
		}
 	}
 	
 	protected void setupWidgetsViewElements() {
		postsListView = (ListView)view.findViewById(R.id.posts_listView);
		postsListView.setOnItemClickListener(this);
		
		if (Facade.getInstance(activity).wasTutorialSwipeRun()==false){
			tutorialSwipeContainer = view.findViewById(R.id.tutorial_swipe_container);
			tutorialSwipeContainer.setVisibility(View.VISIBLE);
			
			tutorialSwipe = view.findViewById(R.id.tutorial_swipe);
			
			buttonCloseTutorialSwipe = (ImageButton)view.findViewById(R.id.button_close_tutorial_swipe);
			buttonCloseTutorialSwipe.setOnClickListener(this);
		}
	}
 	
 	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		
		activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_latest, post.getConversationId());
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
	}
 	
 	@Override
   	public void onClick(View v) {
 		if (v.getId() == R.id.button_close_tutorial_swipe){ 
 			//dialog slideOut
 			Animation slideOut = AnimationUtils.loadAnimation(activity.getApplicationContext(),
	                R.anim.slide_out_bottom);
 			slideOut.setAnimationListener(this);
 			if (tutorialSwipeContainer.getVisibility()==View.VISIBLE){
				tutorialSwipe.startAnimation(slideOut);
				tutorialSwipe.requestLayout();
				tutorialSwipe.setVisibility(View.GONE);
	   	   	 }
 			
 			Facade.getInstance(activity).updateTutorialSwipeRun();
 		}
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
				this.pullToRefreshLayout, 
				true);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		//fade out
		Animation fadeOut = AnimationUtils.loadAnimation(activity.getApplicationContext(),
                R.anim.fade_out);
		tutorialSwipeContainer.startAnimation(fadeOut);
		tutorialSwipeContainer.requestLayout();	   	   		 
		tutorialSwipeContainer.setVisibility(View.GONE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}

}
