package com.riilo.main;

import java.util.List;

import com.google.android.gms.maps.GoogleMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class AtLocationPostsResultReceiver extends ResultReceiver{
	
	private Handler handler;
	private GoogleMap map;	
	public void setMap(GoogleMap map) {
		this.map = map;
	}

	public AtLocationPostsResultReceiver(Handler handler) {
		super(handler);
		this.handler = handler;
	}
	
	@Override 
	protected void onReceiveResult(int resultCode, Bundle resultDate){
		PostsListParcelable postsListParcelable = resultDate.getParcelable(StringKeys.POST_LIST_PARCELABLE);
		if (postsListParcelable!=null){
			final List<Post> posts = postsListParcelable.getPostsList();
			/*switch(resultCode){
			case StringKeys.AT_LOCATION_POSTS_RESULT_RECEIVER_ADD_POST_GROUPS:
				break;
			case StringKeys.AT_LOCATION_POSTS_RESULT_RECEIVER_ADD_POSTS:
				break;
			}*/
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
