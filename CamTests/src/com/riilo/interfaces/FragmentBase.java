package com.riilo.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.riilo.main.Helpers;
import com.riilo.main.Post;
import com.riilo.main.PostListItemAdapter;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

public abstract class FragmentBase extends Fragment implements OnRefreshListener, IPostsListener{

	private static final String TAG = "<<<<<<<<<<<<<<<<<FragmentBase>>>>>>>>>>>>>>>>>";
	
	private boolean isRequestInProgress;
	protected PullToRefreshLayout pullToRefreshLayout;
	protected PostListItemAdapter adapter;
 	protected List<Post> adapterData = new ArrayList<Post>();
	
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
		Log.d(TAG, turnOnPullToRefresh +" " + isRequestInProgress);
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
}
