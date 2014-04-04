package com.riilo.main;

import java.util.List;

import com.riilo.interfaces.ILatestPostsListener;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PostsResultReceiver extends ResultReceiver{

	private ILatestPostsListener latestPostsListener;
	
	private PostListItemAdapter adapter;
	private List<Post> adapterData;
	private SpinnerSectionItemAdapter spinnerAdapter;
	private SpinnerSection section;
	private PullToRefreshLayout pullToRefreshLayout;
	private Button button;
	
	private Handler handler;
	
	public PostsResultReceiver(Handler handler){
		super(handler);
		this.handler = handler;
	}
	
	public void setLatestPostsListener(ILatestPostsListener latestPostsListener) {
		this.latestPostsListener = latestPostsListener;
	}
	
	public void setAdapter(PostListItemAdapter adapter) {
		this.adapter = adapter;
	}

	public void setAdapterData(List<Post> adapterData) {
		this.adapterData = adapterData;
	}

	public void setSpinnerAdapter(SpinnerSectionItemAdapter section) {
		this.spinnerAdapter = section;
	}
	
	public void setPullToRefreshAttacher(PullToRefreshLayout pullToRefreshLayout){
		this.pullToRefreshLayout = pullToRefreshLayout;
	}
	
	public Handler getHandler() {
		return handler;
	}
	
	public void setButton(Button button){
		this.button = button;
	}
	
	public void setSpinnerSection(SpinnerSection section){
		this.section = section;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData){
		int notifications= 0;
		PostsListParcelable postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
		List<Post> posts = null;
		/*if (postsListParcelable!=null)
			postsListParcelable.getPostsList();*/
		
		switch (resultCode){
			case StringKeys.POST_RESULT_RECEIVER_CODE_LATEST_POSTS:
				postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
				posts = postsListParcelable.getPostsList();
				if (posts!=null && posts.size()>=1)
					Log.d("result receviver", posts.get(0).toString());
				latestPostsListener.retrievedLatestPosts(posts);
				break;
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW:
				notifications = resultData.getInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, 0);
				if (spinnerAdapter==null)
					throw new RuntimeException("SPINNER adapter should NOT be null! Maybe you forgot to set it??");
				if (section==null){
					throw new RuntimeException("SECTION in spinner should NOT be null! Mayb you forgot to set it??");
				}
				if (notifications>0){
					updateSection(notifications);
				}
				break;
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC:
				if (adapter==null)
					throw new RuntimeException("adapter should NOT be null! Maybe you forgot to set it??");
				if (adapterData==null)
					throw new RuntimeException("adapterDATA should NOT be null! Maybe you forgot to set it??");
				postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
				posts = postsListParcelable.getPostsList();
				refreshPostsListAdapter(posts, true);
				break;
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_ASC:
				if (adapter==null)
					throw new RuntimeException("adapter should NOT be null! Maybe you forgot to set it??");
				if (adapterData==null)
					throw new RuntimeException("adapterDATA should NOT be null! Maybe you forgot to set it??");
				postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
				posts = postsListParcelable.getPostsList();
				refreshPostsListAdapter(posts, false);
				break;
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER:
				if (spinnerAdapter==null)
					throw new RuntimeException("SPINNER adapter should NOT be null! Maybe you forgot to set it??");
				if (adapter==null)
					throw new RuntimeException("adapter should NOT be null! Maybe you forgot to set it??");
				if (adapterData==null)
					throw new RuntimeException("adapterDATA should NOT be null! Maybe you forgot to set it??");
				
				notifications = resultData.getInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, 0);
				postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
				posts = postsListParcelable.getPostsList();
				
				updateSection(notifications);
				refreshPostsListAdapter(posts, true);
				break;
		}
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (pullToRefreshLayout!=null){
					pullToRefreshLayout.setRefreshComplete();
				}
				if (button!=null){
					button.setVisibility(View.GONE);
				}
					
			}
		});
	}
	
	private void refreshPostsListAdapter(List<Post> newPosts, boolean desc){
		if (newPosts!=null && newPosts.size()>0){
			if (Helpers.renewList(adapterData, newPosts, desc)){
				this.handler.post(new Runnable() {
					
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		}
	}
	
	private void updateSection(final int notifications){
		if (section!=null && spinnerAdapter!=null){
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					section.setNotifications(notifications);
					spinnerAdapter.notifyDataSetChanged();
				}
			});
		}
	}
	
	
}
