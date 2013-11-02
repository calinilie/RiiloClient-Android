package com.example.camtests;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PostsResultReceiver extends ResultReceiver{

	private PostListItemAdapter adapter;
	private List<Post> adapterData;
	private View view;
	
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

	public void setView(View view) {
		this.view = view;
	}
	
	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData){
		switch (resultCode){
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW:
				int notifications = resultData.getInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, 0);
				if (view==null)
					throw new RuntimeException("VIEW should NOT be null! Maybe you forgot to set it??");
				if (notifications>0){
					updateButton(notifications);
				}
				break;
			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC:
				if (adapter==null)
					throw new RuntimeException("adapter should NOT be null! Maybe you forgot to set it??");
				if (adapterData==null)
					throw new RuntimeException("adapterDATA should NOT be null! Maybe you forgot to set it??");
				PostsListParcelable postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
				List<Post> posts = postsListParcelable.getPostsList();
				refreshAdapter(posts);
//				Log.d("<<<<<<<<<<<<<<<PostsResultReceiver.onReceiveResult>>>>>>>>>>>>>>>", "Posts: "+posts.size());
				break;
//			case StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW_AND_ADAPTER:
//				if (view==null)
//					throw new RuntimeException("VIEW should NOT be null! Maybe you forgot to set it??");
//				if (adapter==null)
//					throw new RuntimeException("adapter should NOT be null! Maybe you forgot to set it??");
//				if (adapterData==null)
//					throw new RuntimeException("adapterDATA should NOT be null! Maybe you forgot to set it??");
//				updateButton(posts);
//				refreshAdapter(posts);
//				break;
		}
	}
	
	private void refreshAdapter(List<Post> newPosts){
		if (Helpers.renewList(adapterData, newPosts)){
			this.handler.post(new Runnable() {
				
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}
	}
	
	private void updateButton(final int notifications){
		if ((view!=null) && (view instanceof Button)){
			this.handler.post(new Runnable() {
				
				@Override
				public void run() {
					Button button = (Button) view;
					button.setText("Feed" + String.format(" (%s)", notifications));
				}
			});
		}
	}
	
	
}
