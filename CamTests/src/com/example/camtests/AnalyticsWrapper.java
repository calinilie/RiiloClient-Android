package com.example.camtests;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class AnalyticsWrapper {

	private enum ScreenName { Write_Post, Latest_Posts, Nearby_Posts, Notifications, Conversation}; 
	
	private EasyTracker tracker;
	private static AnalyticsWrapper instance;
	private AnalyticsWrapper(Activity context){
		tracker = EasyTracker.getInstance(context);
	}
	
	public static AnalyticsWrapper getInstance(Activity context){
		if (instance==null)
			instance = new AnalyticsWrapper(context);
		return instance;
	}
	
	public void startTracker(Activity activity){
		tracker.activityStart(activity);
	}
	
	public void stopTracker(Activity activity){
		tracker.activityStop(activity);
	}
	
	public void record_TabSelect_AsScreenHit(int position){
        switch(position){
    	case 0:
    		record_WritePost_ScreenHit();
    		break;
    	case 1:
    		record_LatestPosts_ScreenHit();
    		break;
    	case 2:
    		record_NearbyPosts_ScreenHit();
    		break;
    	case 3:
    		record_Notifications_ScreenHit();
    		break;
        }
	}
	
	public void record_WritePost_ScreenHit(){
		Log.d("ANALYTICS", "record_WritePost_ScreenHit");
		recordScreenHit(ScreenName.Write_Post);
	}
	
	public void record_LatestPosts_ScreenHit(){
		Log.d("ANALYTICS", "record_LatestPosts_ScreenHit");
		recordScreenHit(ScreenName.Latest_Posts);
	}
	
	public void record_NearbyPosts_ScreenHit(){
		Log.d("ANALYTICS", "record_NearbyPosts_ScreenHit");
		recordScreenHit(ScreenName.Nearby_Posts);
	}
	
	public void record_Notifications_ScreenHit(){
		Log.d("ANALYTICS", "record_Notifications_Screen");
		recordScreenHit(ScreenName.Notifications);
	}
	
	public void record_Conversation_ScreenHit(){
		Log.d("ANALYTICS", "record_Conversation_ScreenHit");
		recordScreenHit(ScreenName.Conversation);
	}
	
	private void recordScreenHit(ScreenName screen){
		tracker.set(Fields.SCREEN_NAME, screen.toString());
		
		tracker.send(MapBuilder
			    .createAppView()
			    .build()
			);
	}
	
}
