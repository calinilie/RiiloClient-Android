package com.riilo.main;

import android.app.Activity;
import android.provider.Settings.Secure;
//import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class AnalyticsWrapper {

	private enum ScreenName { Write_Post, Latest_Posts, Nearby_Posts, Notifications, Conversation, Explore};
	private enum EventCategory {use_write_post, use_conversation, use_general, use_tutorial, use_explore, exception};
	private enum EventAction {map_click, button_click, post_item_click, pull_to_refresh, tab_click, viewpager_swipe, map_marker_click, map_cluster_click, map_explore, auto_camera_change, postGroup_click_explore, at_location_posts_resultReceiver};
	public enum EventLabel{button_post, button_cancel, map, map_myLocation, reply_button, tab_latest, tab_nearby, tab_notifications, button_end_tutorial, tab_explore};
	
	private EasyTracker tracker;
	private static AnalyticsWrapper instance;
	private static String deviceId = "";
	private AnalyticsWrapper(Activity context){
		tracker = EasyTracker.getInstance(context);
		deviceId = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
	}
	
	public static AnalyticsWrapper getInstance(Activity context){
		if (instance==null)
			instance = new AnalyticsWrapper(context);
		return instance;
	}
	
	public void startTracker(Activity activity){
		if (isNotEmployeeDevice()){
			tracker.activityStart(activity);
		}
	}
	
	public void stopTracker(Activity activity){
		if (isNotEmployeeDevice())
			tracker.activityStop(activity);
	}
	
	/*====RECORD SCREEN HITS=======================================================================================================*/
	
	/*public void recordScreenHit_WritePost(){
		recordScreenHit(ScreenName.Write_Post);
	}
	
	public void recordScreenHit_LatestPosts(){
		recordScreenHit(ScreenName.Latest_Posts);
	}
	
	public void recordScreenHit_NearbyPosts(){
		recordScreenHit(ScreenName.Nearby_Posts);
	}
	
	public void recordScreenHit_Notifications(){
		recordScreenHit(ScreenName.Notifications);
	}
	
	public void recordScreenHit_Conversation(){
		recordScreenHit(ScreenName.Conversation);
	}
	
	private void recordScreenHit(ScreenName screen){
		if (isNotEmployeeDevice()){
			tracker.set(Fields.SCREEN_NAME, screen.toString());
			
			tracker.send(MapBuilder
				    .createAppView()
				    .build()
				);
		}
	}*/
	
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
	
	public void recordEvent_WritePost_MapCLusterClick(){
		recordEvent(EventCategory.use_write_post, EventAction.map_cluster_click);
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
	
	public void recordEvent_General_ItemClick(EventLabel label){
		recordEvent(EventCategory.use_general, EventAction.post_item_click, label);
	}
	
	public void recordEvent_General_ReplyButtonClicked(){
		recordEvent(EventCategory.use_general, EventAction.button_click, EventLabel.reply_button);
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
	
	//never used
	public void recordEvent_Exception_AtLocationResultReceiver_Caught(){
		recordEvent(EventCategory.exception, EventAction.at_location_posts_resultReceiver);
	}
	
	/**
	 * fired when user deliberately drags the map 
	 */
	public void recordEvent_Explore_MapExplore(){
		recordEvent(EventCategory.use_explore, EventAction.map_explore);
	}
	
	/**
	 * contains both EventAction.map_explore and EventAction.post_click_explore
	 */
	public void recordEvent_Explore_AutoCameraChange(){
		recordEvent(EventCategory.use_explore, EventAction.auto_camera_change);
	}
	
	/**
	 * fired when user clicks on post group
	 */
	public void recordEvent_Explore_PostClickExplore(){
		recordEvent(EventCategory.use_explore, EventAction.postGroup_click_explore);
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
		if (isNotEmployeeDevice())
			tracker.send(MapBuilder.createEvent(categoryAsString, actionAsString, labelAsStirng, value).build());
		
	}
	
	private static final String[] excludedDevices = {"7841715974043649", //Calin 
														"639832f623d58f2", //Mihai
														"7871b4c06d1c3fe2", //Galaxy Note
														"24a2e7d3ed700fc5", //Galaxy S2
														"dff238070c0034ca",  //genymotion galaxy nexus 4.3
														"4db0e579365f988" }; //genymotion galaxy S II 4.1.1
	
	private static boolean isNotEmployeeDevice(){
		for(String s : excludedDevices){
			if (s.equalsIgnoreCase(deviceId)){
				return false;
			}
		}
		
		return true;
	}


}
