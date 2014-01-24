package com.riilo.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.android.gms.maps.GoogleMap;
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
		nearbyPosts = new ArrayList<Post>();
		latestPosts = new ArrayList<Post>();
		explore_onMapPostGroups = new HashSet<Post>();
		explore_onMapPosts = new HashSet<Post>();
		
		this.addPosts(Facade.getInstance(context).getAllPosts());
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
	private List<Post> nearbyPosts;
	private List<Post> latestPosts;
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
	
	public synchronized boolean addPostToLatestPosts(Post currentPost){
		addPost(currentPost);
		boolean conversationFound = false;
		for(Post p : latestPosts){
			if(currentPost.getConversationId()==p.getConversationId()){
				conversationFound = true;
				if (currentPost.isNewer(p)){
					latestPosts.add(currentPost);
					latestPosts.remove(p);
					return true;
				}
			}
		}
		if (!conversationFound)latestPosts.add(currentPost);
		return false;
	}
	
	public synchronized List<Post> getLatestPosts(){
		return this.latestPosts;
	}
	
 	public synchronized List<Post> getLatestPosts(
 				PostListItemAdapter adapter, 
 				List<Post> adapterData, 
 				PullToRefreshLayout pullToRefreshLayout){
		return this.getLatestPosts(adapter, adapterData, pullToRefreshLayout, false);
	}
	
 	//TODO
	public synchronized List<Post> getLatestPosts(
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			PullToRefreshLayout pullToRefreshLayout, 
			boolean forcedUpdate){
		startService_getLatest(adapter, adapterData, pullToRefreshLayout, forcedUpdate);
		return latestPosts;
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
	
	//TODO
	public synchronized List<Post> getNearbyPosts(
			double latitude, 
			double longitude, 
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			SpinnerSectionItemAdapter spinnerAdapter,
			SpinnerSection section,
			PullToRefreshLayout pullToRefreshLayout,
			Button button,
			boolean forcedUpdate, 
			int postResultReceiverType){
		startService_getNearby(latitude, longitude, adapter, adapterData, spinnerAdapter, section, pullToRefreshLayout, button, postResultReceiverType, forcedUpdate);
		return nearbyPosts;
	}
	
	public synchronized List<Post> getNearbyPosts(){
		return this.nearbyPosts;
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

	public synchronized boolean addPostAsNearbyPost(Post p){
		boolean addNearby = true;
		addPost(p);
		for (Post post : nearbyPosts){
			if (p.getConversationId() == post.getConversationId()){
				addNearby = false;
				break;
			}
		}
		if (addNearby){
			nearbyPosts.add(p);
			return true;
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
	
	public synchronized void addPosts(List<Post> posts){
		int lenght = posts.size();
		for(int i=lenght-1; i>=0; i--){
			Post p = posts.get(i);
			this.addPost(p);
			if (!Helpers.hasPostFromConversation(latestPosts, p)){
				latestPosts.add(p);
			}
		}
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
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			SpinnerSectionItemAdapter spinnerAdapter, 
			SpinnerSection section,
			PullToRefreshLayout pullToRefreshLayout,
			Button button,
			int postResultReceiverType,
			boolean forceUpdate){
		if (isRequestAllowed(this.timestamp_Nearby, forceUpdate)){
			Intent intent = new Intent(this.context, WorkerService.class);
			intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_NEARBY_POSTS);
			intent.putExtra(StringKeys.NEARBY_POSTS_LATITUDE, latitude);
			intent.putExtra(StringKeys.NEARBY_POSTS_LONGITUDE, longitude);
			
			Handler handler = new Handler();
			intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, postResultReceiverType);
			PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
			resultReceiver.setAdapter(adapter);
			resultReceiver.setAdapterData(adapterData);
			resultReceiver.setSpinnerAdapter(spinnerAdapter);
			resultReceiver.setSpinnerSection(section);
			resultReceiver.setPullToRefreshAttacher(pullToRefreshLayout);
			resultReceiver.setButton(button);
			intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
			
			this.context.startService(intent);
			
			startRereshAniamtion(handler, pullToRefreshLayout);
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
			PostListItemAdapter adapter, 
			List<Post> adapterData, 
			PullToRefreshLayout pullToRefreshLayout, 
			boolean forcedUpdate){
		if (isRequestAllowed(this.timestamp_PostsCall, forcedUpdate)){
			
			Intent intent = new Intent(context, WorkerService.class);
	        intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_LATEST_POSTS);
	        
	        intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC);
	        Handler handler = new Handler();
	        PostsResultReceiver resultReceiver = new PostsResultReceiver(handler);
	        resultReceiver.setAdapter(adapter);
	        resultReceiver.setAdapterData(adapterData);
	        resultReceiver.setPullToRefreshAttacher(pullToRefreshLayout);
	        intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
	        context.startService(intent);
	        
	        startRereshAniamtion(handler, pullToRefreshLayout);
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
