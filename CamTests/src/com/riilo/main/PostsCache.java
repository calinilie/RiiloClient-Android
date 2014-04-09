package com.riilo.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.android.gms.maps.GoogleMap;
import com.riilo.interfaces.IPostsListener;
import com.riilo.interfaces.UIListener;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

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
	
	
	//conversation_only = will add a post to cache only (and if only) 
	//					the cache does NOT contain a posts from the same conversation id. 
	//					used in notifications cache
	/**
	 * 
	 * @author calin
	 *conversation_only = will add a post to cache only (and if only) 
	 * 						the cache does NOT contain a posts from the same conversation. 
	 *						used in notifications cache
	 *<br /><br />
	 *conversation_and_newer = will add a post to a cache which already contains a post from
	 *							the same conversation, only (and if only) the post in question is newer 
	 *							that the one in the cache, used in latest and nearby
	 */
	public static enum CacheMode {conversation_only, conversation_and_newer}; 
	
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
	
	public synchronized List<Post> addConversationPostsToCache(List<Post> newPosts, long conversationId){
		List<Post> conversation = this.getPostsByConversationId(conversationId);
		List<Post> retVal = new ArrayList<Post>();
		
		if (newPosts==null)
			return retVal;
		
		for(Post p : newPosts){
			addPost(p);
			if (!conversation.contains(p))
				retVal.add(p);
		}
		
		return retVal;
	}
	
	public synchronized List<Post> getLatestPosts(){
		return this.latestPostsCacheList;
	}

 	public synchronized List<Post> addNewPostsToLatest(List<Post> newPosts){
 		return this.addPostsListToCacheList(newPosts, latestPostsCacheList, CacheMode.conversation_and_newer);
 	}
 	
	public synchronized List<Post> getLatestPosts(
			IPostsListener latestPostsListener,
			boolean forcedUpdate){
		startService_getLatest(latestPostsListener, forcedUpdate);
		return latestPostsCacheList;
	}
	
	public synchronized List<Post> getPostsByConversationId(
			long conversationId,
			IPostsListener postsListener){
		List<Post> retVal = getPostsByConversationId(conversationId);
		startService_getConversationByPostId(conversationId, postsListener);
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
	
	public synchronized List<Post> getNotifications(
			String userId, 
			IPostsListener postsListener,
			boolean forcedUpdate){
			startService_getNotifications(userId, postsListener, forcedUpdate);
		return this.notifications;
	}
	
	public synchronized List<Post> getNotifications(){
		return this.notifications;
	}
	
	public synchronized void addPost(Post p){
		this.posts.put((int) p.getId(), p);
	}
	
	public synchronized List<Post> addPostsToNotifications(List<Post> notifications){
		return this.addPostsListToCacheList(notifications, this.notifications, CacheMode.conversation_only);
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
			IPostsListener listener, 
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
		return this.addPostsListToCacheList(newPosts, this.nearbyPostsCacheList, CacheMode.conversation_and_newer);
	}
	
	/**
	 * 
	 * @param newPosts
	 * @param targetCacheList
	 * @return
	 */
	private synchronized List<Post> addPostsListToCacheList(List<Post> newPosts, List<Post> targetCacheList, CacheMode cacheMode){
		List<Post> retVal = new ArrayList<Post>();
		
		if (newPosts == null)
			return retVal;
		
		Collections.sort(nearbyPostsCacheList, Collections.reverseOrder());
		Collections.sort(newPosts, Collections.reverseOrder());
		
		for(Post p : newPosts){
			if (this.addPostToCacheList(p, targetCacheList, cacheMode)){
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
	private synchronized boolean addPostToCacheList(Post currentPost, List<Post> targetCacheList, CacheMode cacheMode){
		boolean addNearby = true;
		addPost(currentPost);//add to global posts cache
		
		//if we already have the currentPost in list, then skip it
		if (!targetCacheList.contains(currentPost)){
			
			for_loop: 
			for (Post post : targetCacheList){
				
				switch(cacheMode){
				case conversation_only:
					if (currentPost.getConversationId() == post.getConversationId()){
						addNearby = false;
						break for_loop;
					}
					break;
				case conversation_and_newer:
					if (currentPost.getConversationId() == post.getConversationId() && !currentPost.isNewer(post)){
						addNearby = false;
						break for_loop;
					}
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
	
	private void startService_getConversationByPostId(
			long conversationId, 
			IPostsListener postsListener){
		Intent intent = new Intent(this.context, WorkerService.class);
        intent.putExtra(StringKeys.CONVERSATION_FROM_CONVERSATION_ID, conversationId);
        intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID);
        
        Handler handler = new Handler();
        PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
        resultReceiver.setPostsListener(postsListener);
        intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
        
        this.context.startService(intent);
        
        postsListener.startedRetrievingPosts();
	}
	
	private void startService_getNotifications(
			String userId, 
			IPostsListener postsListener,
			boolean forcedUpdate){
		if (isRequestAllowed(timestamp_NotificationsCall, forcedUpdate)){
			Intent intent = new Intent(this.context, WorkerService.class);
			intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_NOTIFICATIONS);
			intent.putExtra(StringKeys.NOTIFICATIONS_USER_ID, userId);
			
			Handler handler = new Handler();
			PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
			resultReceiver.setPostsListener(postsListener);
			intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
	
			this.context.startService(intent);
			
			postsListener.startedRetrievingPosts();
		}
	}
	
	private void startService_getNearby(
			double latitude, 
			double longitude, 
			IPostsListener listener,
			boolean forceUpdate){
		if (isRequestAllowed(this.timestamp_Nearby, forceUpdate)){
			Intent intent = new Intent(this.context, WorkerService.class);
			intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_NEARBY_POSTS);
			intent.putExtra(StringKeys.NEARBY_POSTS_LATITUDE, latitude);
			intent.putExtra(StringKeys.NEARBY_POSTS_LONGITUDE, longitude);
			
			Handler handler = new Handler();
			PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
			resultReceiver.setPostsListener(listener);
			intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
			this.context.startService(intent);
			
			listener.startedRetrievingPosts();
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
			IPostsListener listener, 
			boolean forcedUpdate){
		if (isRequestAllowed(this.timestamp_PostsCall, forcedUpdate)){
			
			Intent intent = new Intent(context, WorkerService.class);
	        intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_LATEST_POSTS);
	        
	        Handler handler = new Handler();
	        PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
	        resultReceiver.setPostsListener(listener);
	        intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
	        context.startService(intent);
	        
	        listener.startedRetrievingPosts();
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
