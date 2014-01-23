package com.riilo.main;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class WorkerService_NearbyPosts extends IntentService{

	
	
	public WorkerService_NearbyPosts() {
		super("WorkerIntentService_NearbyPosts");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle resultData = null;
		ResultReceiver resultReceiver = null;
		int resultReceiverType = 0;
		long conversationId = 0;
		String userId = null;
		int intentType = intent.getIntExtra(StringKeys.WS_INTENT_TYPE, -1);
		double latitude = 0;
		double longitude = 0;
		/*switch(intentType){
			case StringKeys.WS_INTENT_NEARBY_POSTS:
				latitude = intent.getDoubleExtra(StringKeys.NEARBY_POSTS_LATITUDE, 0);
				longitude = intent.getDoubleExtra(StringKeys.NEARBY_POSTS_LONGITUDE, 0);
				resultReceiver = intent.getParcelableExtra(StringKeys.POST_LIST_RESULT_RECEIVER);
				resultReceiverType = intent.getIntExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW);
				if (latitude != 0 && longitude != 0){
					List<Post> nearbyPosts = getNearbyPosts(latitude, longitude);
					if (resultReceiver!=null){
						resultData = new Bundle();
						resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(nearbyPosts));
						resultData.putInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, postsCache.getNearbyPosts().size());
						resultReceiver.send(resultReceiverType, resultData);
					}
				}
				break;
			//TODO create AtLocationPostsIntentService
			case StringKeys.WS_INTENT_GET_AT_LOCATION_POSTS:
				latitude = intent.getDoubleExtra(StringKeys.AT_LOCATION_POSTS_LATITUDE, 0);
				longitude = intent.getDoubleExtra(StringKeys.AT_LOCATION_POSTS_LONGITUDE, 0);
				//TODO create AtLocationPostsResultReceiver
				resultReceiver = intent.getParcelableExtra(StringKeys.AT_LOCATON_POSTS_RESULT_RECEIVER);
				if (latitude!=0 && longitude!=0){
					List<Post> postsAtLocation = getLatestPosts(0, 2000);//getNearbyPosts(latitude, longitude, 3500);
					if (resultReceiver!=null){
						resultData = new Bundle();
						resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(postsAtLocation));
						resultReceiver.send(0, resultData);
					}
				}
				break;
		}*/
	}

}
