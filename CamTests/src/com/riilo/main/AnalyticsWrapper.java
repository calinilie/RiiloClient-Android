package com.riilo.main;

import android.app.Activity;
//import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class AnalyticsWrapper {

	private enum ScreenName { Write_Post, Latest_Posts, Nearby_Posts, Notifications, Conversation};
	private enum EventCategory {use_write_post, use_conversation, use_general, use_tutorial};
	private enum EventAction {map_click, button_click, post_item_click, pull_to_refresh, tab_click, viewpager_swipe, map_marker_click};
	public enum EventLabel{button_post, button_cancel, map, map_myLocation, reply_button, tab_latest, tab_nearby, tab_notifications, button_end_tutorial};
	
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
	
	/*====RECORD SCREEN HITS=======================================================================================================*/
	public void recordScreenHit_TabSelect(int position){
        switch(position){
    	case 0:
    		recordScreenHit_WritePost();
    		break;
    	case 1:
    		recordScreenHit_LatestPosts();
    		break;
    	case 2:
    		recordScreenHit_NearbyPosts();
    		break;
    	case 3:
    		recordScreenHit_Notifications();
    		break;
        }
	}
	
	public void recordScreenHit_WritePost(){
		//Log.d("ANALYTICS", "record_WritePost_ScreenHit");
		recordScreenHit(ScreenName.Write_Post);
	}
	
	public void recordScreenHit_LatestPosts(){
		//Log.d("ANALYTICS", "record_LatestPosts_ScreenHit");
		recordScreenHit(ScreenName.Latest_Posts);
	}
	
	public void recordScreenHit_NearbyPosts(){
		//Log.d("ANALYTICS", "record_NearbyPosts_ScreenHit");
		recordScreenHit(ScreenName.Nearby_Posts);
	}
	
	public void recordScreenHit_Notifications(){
		//Log.d("ANALYTICS", "record_Notifications_Screen");
		recordScreenHit(ScreenName.Notifications);
	}
	
	public void recordScreenHit_Conversation(){
		//Log.d("ANALYTICS", "record_Conversation_ScreenHit");
		recordScreenHit(ScreenName.Conversation);
	}
	
	private void recordScreenHit(ScreenName screen){
		tracker.set(Fields.SCREEN_NAME, screen.toString());
		
		tracker.send(MapBuilder
			    .createAppView()
			    .build()
			);
	}
	
	/*=====RECORD EVENTS======================================================================================================*/
	public void recordEvent_WritePost_MapClick(){
		recordEvent(EventCategory.use_write_post, EventAction.map_click);
	}
	
	public void recordEvent_WritePost_ButtonClick(EventLabel label){
		recordEvent(EventCategory.use_write_post, EventAction.button_click, label);
	}
	
	public void recordEvent_WritePost_MarkerClick(){
		recordEvent(EventCategory.use_write_post, EventAction.map_marker_click);
	}
	
	public void recordEvent_Conversation_MapClick(EventLabel label){
		recordEvent(EventCategory.use_conversation, EventAction.map_click, label);
	}
	
	public void recordEvent_Conversation_ButtonClick(EventLabel label){
		recordEvent(EventCategory.use_conversation, EventAction.button_click, label);
	}
	
	public void recordEvent_Conversation_ItemClick(Long value){
		recordEvent(EventCategory.use_conversation, EventAction.post_item_click, null, value);
	}
	
	public void recordEvent_General_ItemClick(EventLabel label, Long value){
		recordEvent(EventCategory.use_general, EventAction.post_item_click, label, value);
	}
	
	public void recordEvent_General_PullToRefresh(EventLabel label){
		recordEvent(EventCategory.use_general, EventAction.pull_to_refresh, label);
	}
	
	public void recordEvent_General_TabClick(){
		recordEvent(EventCategory.use_general, EventAction.tab_click);
	}
	
	public void recordEvent_General_ViewPagerSwipe(){
		recordEvent(EventCategory.use_general, EventAction.viewpager_swipe);
	}

	public void recordEvent_Tutorial_EndButtonClick(){
		recordEvent(EventCategory.use_tutorial, EventAction.button_click, EventLabel.button_end_tutorial);
	}
	
	
	private void recordEvent(EventCategory category, EventAction action){
		recordEvent(category, action, null, null);
	}
	
	private void recordEvent(EventCategory category, EventAction action, EventLabel label){
		this.recordEvent(category, action, label, null);
	}
	
	private void recordEvent(EventCategory category, EventAction action, EventLabel label, Long value){
		String categoryAsString = category.toString();
		String actionAsString = action.toString();
		String labelAsStirng = "";
		if (label!=null){
			labelAsStirng = label.toString();
		}
		tracker.send(MapBuilder.createEvent(categoryAsString, actionAsString, labelAsStirng, value).build());
		
		//Log.d("ANALYTICS", categoryAsString+" "+actionAsString+" "+labelAsStirng+" "+value);
	}


}
