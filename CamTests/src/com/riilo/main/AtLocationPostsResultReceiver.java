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
			/*TODO filter posts -
			 * same conversations, only 1 post should be in the map
			 * choose the newest from 2 conversations that are really close 
			 */
			final List<Post> posts = postsListParcelable.getPostsList();
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

}
