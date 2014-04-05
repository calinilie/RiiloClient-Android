package com.riilo.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.android.gms.maps.GoogleMap;
import com.riilo.interfaces.ILatestPostsListener;
import com.riilo.interfaces.INearbyPostsListener;
import com.riilo.interfaces.UIListener;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Button;

public class PostsCache {

	private static final String TAG = "<<<<<<<<<<<<<<<<<PostsCache>>>>>>>>>>>>>>>>>";
	
	private Context context;
	private RequestTimestamp timestamp_PostsCall;	
	private RequestTimestamp timestamp_NotificationsCall;
	private RequestTimestamp timestamp_Nearby;
	
	//START: singleton=========================================
	private static PostsCache instance;
	private PostsCache(Context context){
		posts = new SparseArray<Post>();
		notifications = new ArrayList<Post>();
		nearbyPostsCacheList = new ArrayList<Post>();
		latestPostsCacheList = new ArrayList<Post>();
		explore_onMapPostGroups = new HashSet<Post>();
		explore_onMapPosts = new HashSet<Post>();
		
		this.context = context;
		
		this.timestamp_PostsCall = new RequestTimestamp();
		this.timestamp_NotificationsCall = new RequestTimestamp();
		this.timestamp_Nearby = new RequestTimestamp();
	}
	public static PostsCache getInstance(Context context){
		if (instance==null)
			instance = new PostsCache(context);
		return instance;
	}
	//END: singleton=========================================
	
	private SparseArray<Post> posts;
	private List<Post> notifications;
	private List<Post> nearbyPostsCacheList;
	private List<Post> latestPostsCacheList;
	private Set<Post> explore_onMapPostGroups;
	private Set<Post> explore_onMapPosts;
	
	public synchronized List<Post> getPostsAsList() {
		List<Post> retVal = new ArrayList<Post>();
		int length = this.posts.size();
		for(int i=0; i<length; i++){
			int key = this.posts.keyAt(i);
			retVal.add(this.posts.get(key));
		}
		Collections.sort(retVal, Collections.reverseOrder());
		return retVal;
	}
	
	public synchronized List<Post> getLatestPosts(){
		return this.latestPostsCacheList;
	}

 	public synchronized List<Post> addNewPostsToLatest(List<Post> newPosts){
 		return this.addPostsListToCacheList(newPosts, latestPostsCacheList);
 	}
 	
	public synchronized List<Post> getLatestPosts(
			ILatestPostsListener latestPostsListener,
			boolean forcedUpdate){
		startService_getLatest(latestPostsListener, forcedUpdate);
		return latestPostsCacheList;
	}
	
	public synchronized List<Post> getPostsByConversationId(
			long conversationId, 
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			PullToRefreshLayout pullToRefreshLayout){
		List<Post> retVal = getPostsByConversationId(conversationId);
		startService_getConversationByPostId(conversationId, adapter, adapterData, pullToRefreshLayout);
		return retVal;
	}
	
	public synchronized List<Post> getPostsByConversationId(long conversationId){
		List<Post> retVal = new ArrayList<Post>();
		int length = this.posts.size();
		for(int i=0; i<length; i++){
			int key = this.posts.keyAt(i);
			Post post = this.posts.get(key);
			if (conversationId == post.getConversationId())
				retVal.add(post);
		}
		return retVal;
	}
	
	//TODO
	public synchronized List<Post> getNotifications(
			String userId, 
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			SpinnerSectionItemAdapter spinnerAdapter, 
			SpinnerSection section,
			PullToRefreshLayout pullToRefreshLayout, 
			boolean forcedUpdate, 
			int postResultReceiverType){
		if (isRequestAllowed(timestamp_NotificationsCall, forcedUpdate)){
			startService_getNotifications(userId, adapter, adapterData, spinnerAdapter, section, pullToRefreshLayout, postResultReceiverType);
		}
		return this.notifications;
	}
	
	public synchronized List<Post> getNotifications(){
		return this.notifications;
	}
	
	public synchronized void addPost(Post p){
		this.posts.put((int) p.getId(), p);
	}
	
	/**
	 * 
	 * @param p
	 * @return <li><b>false</b> means a Post from the same conversation is already in cache, <b>DO NOT ADD</b> in data adapter</li>
	 */
	public synchronized boolean addPostAsNotification(Post p){
		boolean addNotification = true;
		addPost(p);
		for(Post post: notifications){
			if (p.getConversationId() == post.getConversationId()){
				addNotification=false;
				break;
			}
		}
		if (addNotification){
			notifications.add(p);
			return true;
		}
		return false;
	}
	
	/**
	 * This overload should be used to retrieve nearby posts (from server) and cache them ONLY, not present them on the UI
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public synchronized List<Post> getNearbyPosts(
			double latitude, 
			double longitude){
		return this.getNearbyPosts(latitude, longitude, null, false);
	}
	
	public synchronized List<Post> getNearbyPosts(
			double latitude, 
			double longitude, 
			INearbyPostsListener listener, 
			boolean forcedUpdate){
		startService_getNearby(latitude, longitude, listener, forcedUpdate);
		return nearbyPostsCacheList;
	}
	
	/**
	 * @return all posts in cache
	 */
	public List<Post> getNearbyPosts() {
		return nearbyPostsCacheList;
	}
	
	public synchronized void getPostsOnMap(
			double latitude, 
			double longitude,
			double distance,
			GoogleMap map, 
			Handler handler,
			UIListener uiListener){
		startService_getPostsOnMap(latitude, longitude, distance, map, handler, uiListener);
	}
	
	public synchronized void getPostGroupsOnMap(GoogleMap map, Handler handler, UIListener uiListener){
		startService_getPostGroupsOnMap(map, handler, uiListener);
	}
	
	public Set<Post> getExplore_onMapPosts() {
		return explore_onMapPosts;
	}
	
	public void clear_mapPosts(){
		this.explore_onMapPosts.clear();
		Helpers.mergeLists(this.explore_onMapPosts, this.explore_onMapPostGroups);
	}
	
	public Set<Post> getExplore_onMapPostGroups() {
		return explore_onMapPostGroups;
	}
	
	/**
	 * Adds all new posts to cache, and returns only the new posts, which have to be passed to the UI
	 * @param newPosts
	 * @return
	 */
	public synchronized List<Post> addPostsToNearby(List<Post> newPosts){
		return this.addPostsListToCacheList(newPosts, this.nearbyPostsCacheList);
	}
	
	/**
	 * 
	 * @param newPosts
	 * @param targetCacheList
	 * @return
	 */
	private synchronized List<Post> addPostsListToCacheList(List<Post> newPosts, List<Post> targetCacheList){
		List<Post> retVal = new ArrayList<Post>();
		
		if (newPosts == null)
			return retVal;
		
		Collections.sort(nearbyPostsCacheList, Collections.reverseOrder());
		Collections.sort(newPosts, Collections.reverseOrder());
		
		for(Post p : newPosts){
			if (this.addPostToCacheList(p, targetCacheList)){
				retVal.add(p);
			}
		}
		
		return retVal;
	}

	/**
	 * Adds a post to the to one of the cached lists posts nearby cache 
	 * @param currentPost
	 * @return
	 */
	private synchronized boolean addPostToCacheList(Post currentPost, List<Post> targetCacheList){
		boolean addNearby = true;
		addPost(currentPost);//add to global posts cache
		
		//if we already have the currentPost in list, then skip it
		if (!targetCacheList.contains(currentPost)){
			for (Post post : targetCacheList){
				//only add the currentPost from a particular conversation if it is newer the what the conversation already has 
				if (currentPost.getConversationId() == post.getConversationId() && !currentPost.isNewer(post)){
					addNearby = false;
					break;
				}
			}
			if (addNearby){
				targetCacheList.add(currentPost);
				return true;
			}
		}
		return false;
	}
	
	public synchronized boolean removeNotification(Post toRemove){
		List<Post> postsToBeRemoved = new ArrayList<Post>();
		for(Post p : notifications){
			if (p.getConversationId() == toRemove.getConversationId()){
				postsToBeRemoved.add(p);
			}
		}
		notifications.removeAll(postsToBeRemoved);
		return postsToBeRemoved.size()>0;
	}
	
	public synchronized Post getPost(int postId){
		return posts.get(postId);
	}
	
	public List<Post> renewPosts(List<Post> targetPosts){
		int length = targetPosts.size();
		for(int i=0; i<length; i++){
			int key = this.posts.keyAt(i);
			Post post = this.posts.get(key);
			if (!targetPosts.contains(post))
				targetPosts.add(post);
		}
		return targetPosts;
	}
	
	private void startService_getConversationByPostId(
			long conversationId, 
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			PullToRefreshLayout pullToRefreshLayout){
		Intent intent = new Intent(this.context, WorkerService.class);
        intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID);
        intent.putExtra(StringKeys.CONVERSATION_FROM_CONVERSATION_ID, conversationId);
        
        Handler handler = new Handler();
        intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_ASC);
        PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
        resultReceiver.setAdapter(adapter);
        resultReceiver.setAdapterData(adapterData);
        resultReceiver.setPullToRefreshAttacher(pullToRefreshLayout);
        intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
        
        this.context.startService(intent);
        
        startRereshAniamtion(handler, pullToRefreshLayout);
	}
	
	private void startService_getNotifications(
			String userId, 
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			SpinnerSectionItemAdapter spinnerAdapter, 
			SpinnerSection section,
			PullToRefreshLayout pullToRefreshLayout, 
			int postResultReceiverType){
		Intent intent = new Intent(this.context, WorkerService.class);
		intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_NOTIFICATIONS);
		intent.putExtra(StringKeys.NOTIFICATIONS_USER_ID, userId);
		
		Handler handler = new Handler();
		intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, postResultReceiverType);
		PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
		resultReceiver.setAdapter(adapter);
		resultReceiver.setAdapterData(adapterData);
		resultReceiver.setSpinnerAdapter(spinnerAdapter);
		resultReceiver.setSpinnerSection(section);
		resultReceiver.setPullToRefreshAttacher(pullToRefreshLayout);
		intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);

		this.context.startService(intent);
		
		startRereshAniamtion(handler, pullToRefreshLayout);
	}
	
	private void startService_getNearby(
			double latitude, 
			double longitude, 
			INearbyPostsListener listener,
			boolean forceUpdate){
		if (isRequestAllowed(this.timestamp_Nearby, forceUpdate)){
			Intent intent = new Intent(this.context, WorkerService.class);
			intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_NEARBY_POSTS);
			intent.putExtra(StringKeys.NEARBY_POSTS_LATITUDE, latitude);
			intent.putExtra(StringKeys.NEARBY_POSTS_LONGITUDE, longitude);
			
			Handler handler = new Handler();
			PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
			resultReceiver.setNearbyPostsListener(listener);
			intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
			this.context.startService(intent);
		}
	}
	
	private void startService_getPostsOnMap(
			double latitude, 
			double longitude, 
			double distance, 
			GoogleMap map, 
			Handler handler,
			UIListener uiListener){
		Intent intent =  new Intent(context, WorkerService.class);
		intent.putExtra(StringKeys.AT_LOCATION_POSTS_LATITUDE, latitude);
		intent.putExtra(StringKeys.AT_LOCATION_POSTS_LONGITUDE, longitude);
		intent.putExtra(StringKeys.AT_LOCATION_POSTS_DISTANCE, distance);
		intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_AT_LOCATION_POSTS);
		AtLocationPostsResultReceiver resultReceiver = new AtLocationPostsResultReceiver(handler, this, uiListener);
		resultReceiver.setMap(map);
		intent.putExtra(StringKeys.AT_LOCATON_POSTS_RESULT_RECEIVER, resultReceiver);
		context.startService(intent);
		uiListener.onLoadStart();
	}
	
	private void startService_getPostGroupsOnMap(GoogleMap map, Handler handler, UIListener uiListener){
		Intent intent = new Intent(context, WorkerService.class);
		intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_POST_GROUPS_ON_MAP);
		AtLocationPostsResultReceiver resultReceiver = new AtLocationPostsResultReceiver(handler, this, uiListener);
		resultReceiver.setMap(map);
		intent.putExtra(StringKeys.AT_LOCATON_POSTS_RESULT_RECEIVER, resultReceiver);
		context.startService(intent);
		uiListener.onLoadStart();
	}
	
	private void startService_getLatest(
			ILatestPostsListener listener, 
			boolean forcedUpdate){
		if (isRequestAllowed(this.timestamp_PostsCall, forcedUpdate)){
			
			Intent intent = new Intent(context, WorkerService.class);
	        intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_LATEST_POSTS);
	        
	        /*intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_LATEST_POSTS);*/
	        Handler handler = new Handler();
	        PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
	        resultReceiver.setLatestPostsListener(listener);
	        intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
	        context.startService(intent);
		}
	}
	
	private boolean isRequestAllowed(RequestTimestamp timestamp, boolean forceUpdate){
		if (forceUpdate){
			timestamp.setTimeStamp(Calendar.getInstance().getTime());
			return true;
		}
		else{
			if (timestamp.getTimeStamp()!=null){
				long difference = (Calendar.getInstance().getTimeInMillis()-timestamp.getTimeStamp().getTime()) / 1000;
				if (difference>60){
					timestamp.setTimeStamp(Calendar.getInstance().getTime());
					return true;
				}
			}
			else{
				timestamp.setTimeStamp(Calendar.getInstance().getTime());
				return true;
			}
		}
		return false;
	}
	
	private void startRereshAniamtion(Handler handler, final PullToRefreshLayout pullToRefreshLayout){
		if (pullToRefreshLayout!=null && !pullToRefreshLayout.isRefreshing()){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					pullToRefreshLayout.setRefreshing(true);
				}
			});
		}
	}
	
	public class RequestTimestamp{
		
		private Date timeStamp;
		
		public RequestTimestamp(){
			
		}
		
		public RequestTimestamp(Date date){
			this.timeStamp = date;
		}

		public Date getTimeStamp() {
			return timeStamp;
		}

		public void setTimeStamp(Date timeStamp) {
			this.timeStamp = timeStamp;
		}
	}


}
