package com.example.camtests;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PostsResultReceiver extends ResultReceiver{

	private PostListItemAdapter adapter;
	private List<Post> adapterData;
	private Tab tab;
	private PullToRefreshAttacher pullToRefreshAttacher;
	
	private Handler handler;
	
	public PostsResultReceiver(Handler handler){
		super(handler);
		this.handler = handler;
	}
	
	public void setAdapter(PostListItemAdapter adapter) {
		this.adapter = adapter;
	}

	public void setAdapterData(List<Post> adapterData) {
		this.adapterData = adapterData;
	}

	public void setTab(Tab tab) {
		this.tab = tab;
	}
	
	public void setPullToRefreshAttacher(PullToRefreshAttacher pullToRefreshAttacher){
		this.pullToRefreshAttacher = pullToRefreshAttacher;
	}
	
	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return pullToRefreshAttacher;
	}

	public Handler getHandler() {
		return handler;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData){
		int notifications= 0;
		PostsListParcelable postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
		List<Post> posts = null;
		if (postsListParcelable!=null)
			postsListParcelable.getPostsList();
		
		switch (resultCode){
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW:
				notifications = resultData.getInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, 0);
				if (tab==null)
					throw new RuntimeException("VIEW should NOT be null! Maybe you forgot to set it??");
				if (notifications>0){
					updateTabText(notifications);
				}
				break;
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC:
				if (adapter==null)
					throw new RuntimeException("adapter should NOT be null! Maybe you forgot to set it??");
				if (adapterData==null)
					throw new RuntimeException("adapterDATA should NOT be null! Maybe you forgot to set it??");
				postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
				posts = postsListParcelable.getPostsList();
				refreshAdapter(posts);
//				Log.d("<<<<<<<<<<<<<<<PostsResultReceiver.onReceiveResult>>>>>>>>>>>>>>>", "Posts: "+posts.size());
				break;
			/*case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER:
				throw new RuntimeException("NOT implemented YET");*/
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER:
				if (tab==null)
					throw new RuntimeException("VIEW should NOT be null! Maybe you forgot to set it??");
				if (adapter==null)
					throw new RuntimeException("adapter should NOT be null! Maybe you forgot to set it??");
				if (adapterData==null)
					throw new RuntimeException("adapterDATA should NOT be null! Maybe you forgot to set it??");
				
				notifications = resultData.getInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, 0);
				postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
				posts = postsListParcelable.getPostsList();
				
				updateTabText(notifications);
				Log.d("¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤", notifications+"");
				refreshAdapter(posts);
				break;
		}
		Log.d("###################################", "OnReceive Called");
		if (pullToRefreshAttacher!=null /*&& pullToRefreshAttacher.isRefreshing()*/){
			Log.d("###################################", "pullToRefreshAttacher NOT null");
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					pullToRefreshAttacher.setRefreshComplete();
				}
			});
		}
	}
	
	private void refreshAdapter(List<Post> newPosts){
		if (newPosts!=null && newPosts.size()>0){
			if (Helpers.renewList(adapterData, newPosts)){
				this.handler.post(new Runnable() {
					
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		}
	}
	
	private void updateTabText(final int notifications){
		if (tab!=null){
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					switch(tab.getPosition()){
					case 2:
						tab.setText("Nearby: "+notifications);
						break;
					case 3:
						tab.setText("Notifications: "+notifications);
						break;
					}
				}
			});
		}
	}
	
	
}
