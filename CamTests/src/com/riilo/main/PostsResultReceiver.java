package com.riilo.main;

import java.util.List;

import com.riilo.interfaces.IPostsListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class PostsResultReceiver extends ResultReceiver{

	private IPostsListener postsListener;
	
	public PostsResultReceiver(Handler handler){
		super(handler);
	}
	
	public void setPostsListener(IPostsListener postsListener) {
		this.postsListener = postsListener;
	}
	
	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData){
		PostsListParcelable postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
		List<Post> posts = null;		
		postsListParcelable =  resultData.getParcelable(StringKeys.POST_LIST_PARCELABLE);
		posts = postsListParcelable.getPostsList();
		postsListener.retrievedPosts(posts);
				
	}
	
}
