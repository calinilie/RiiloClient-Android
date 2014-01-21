package com.riilo.main;

import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.riilo.interfaces.UIListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class AtLocationPostsResultReceiver extends ResultReceiver{
	
	private Handler handler;
	private GoogleMap map;	
	public void setMap(GoogleMap map) {
		this.map = map;
	}
	private PostsCache postsCache;
	private UIListener uiListener;

	public AtLocationPostsResultReceiver(Handler handler, PostsCache postsCache, UIListener uiListener) {
		super(handler);
		this.handler = handler;
		this.postsCache = postsCache;
		this.uiListener = uiListener;
	}
	
	@Override 
	protected void onReceiveResult(int resultCode, Bundle resultDate){
		PostsListParcelable postsListParcelable = resultDate.getParcelable(StringKeys.POST_LIST_PARCELABLE);
		if (postsListParcelable!=null){
			List<Post> posts = postsListParcelable.getPostsList();
			switch(resultCode){
			case StringKeys.AT_LOCATION_POSTS_RESULT_RECEIVER_ADD_POST_GROUPS:
				posts = (List<Post>) Helpers.mergeLists(postsCache.getExplore_onMapPosts() , posts) ;
				posts = (List<Post>) Helpers.mergeLists(postsCache.getExplore_onMapPostGroups(), posts);
				uiListener.onLoadEnd(null);
				break;
			case StringKeys.AT_LOCATION_POSTS_RESULT_RECEIVER_ADD_POSTS:
				//TODO add to adapter data here!!
				uiListener.onLoadEnd(posts);
				//existent posts on the map have to be removed from the collection
				posts = (List<Post>) Helpers.mergeLists(postsCache.getExplore_onMapPosts(), posts);
				break;
			}
			addMarkersToMap(posts);
		}
		
	}
	
	private void addMarkersToMap(final List<Post> posts){
		if (posts!=null && !posts.isEmpty() && map!=null){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					Helpers.addPostsToMap(posts, map);
					
				}
			});
		}
	}

}
