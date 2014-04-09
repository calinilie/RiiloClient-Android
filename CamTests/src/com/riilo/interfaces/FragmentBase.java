package com.riilo.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.riilo.main.Helpers;
import com.riilo.main.MainActivity;
import com.riilo.main.Post;
import com.riilo.main.PostListItemAdapter;
import com.riilo.main.PostViewActivity;
import com.riilo.main.StringKeys;
import com.riilo.main.AnalyticsWrapper.EventLabel;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class FragmentBase extends Fragment implements OnRefreshListener, IPostsListener, OnItemClickListener{

	protected MainActivity activity;
	
	private static final String TAG = "<<<<<<<<<<<<<<<<<FragmentBase>>>>>>>>>>>>>>>>>";
	
	private boolean isRequestInProgress;
	protected PullToRefreshLayout pullToRefreshLayout;
	protected PostListItemAdapter adapter;
 	protected List<Post> adapterData = new ArrayList<Post>();
	
 	@Override
 	public void onAttach(Activity activity){
 		this.activity = (MainActivity)activity;
 		super.onAttach(activity);
 	}
 	
	protected void requestInProgress(){
		isRequestInProgress = true;
	}
	
	protected void requestFinished(){
		isRequestInProgress = false;
		pullToRefreshLayout.setRefreshComplete();
	}
	
	public boolean isPTRRefreshing(){
		return this.pullToRefreshLayout.isRefreshing();
	}
	
	public void isSelected(boolean turnOnPullToRefresh){
		if (turnOnPullToRefresh && isRequestInProgress)
			pullToRefreshLayout.setRefreshing(true);
	}
	
	@Override
	public void retrievedPosts(List<Post> newPosts) {
		if (Helpers.renewList(adapterData, newPosts)){
			adapter.notifyDataSetChanged();
		}
		this.requestFinished();
	}

	@Override
	public void startedRetrievingPosts() {
		this.requestInProgress();
	}
	
	@Override
	public void onRefreshStarted(View view) {
		this.requestInProgress();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_CONVERSATION_ID, post.getConversationId());
		postViewIntent.putExtra(StringKeys.POST_ID, post.getId());
		startActivity(postViewIntent);
		activity.setAnimationType(StringKeys.ANIMATION_TYPE_SLIDE_IN_RIGHT);
	}
}
