package com.example.camtests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Button;

public class PostsCache {

	private Context context;
	private RequestTimestamp timestamp_PostsCall;	
	private RequestTimestamp timestamp_NotificationsCall;
	
	//START: singleton=========================================
	private static PostsCache instance;
	private PostsCache(Context context){
		posts = new SparseArray<Post>();
		notifications = new ArrayList<Post>();
		nearbyPosts = new ArrayList<Post>();
		latestPosts = new ArrayList<Post>();
		
		this.addPosts(Facade.getInstance(context).getAllPosts());
		this.context = context;
		
		this.timestamp_PostsCall = new RequestTimestamp();
		this.timestamp_NotificationsCall = new RequestTimestamp();
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
	
	public synchronized List<Post> getPostsAsList() {
		List<Post> retVal = new ArrayList<Post>();
		int length = this.posts.size();
		for(int i=0; i<length; i++){
			int key = this.posts.keyAt(i);
			retVal.add(this.posts.get(key));
//			Log.d("=======POST DEBUG======", posts.get(key).toString());
		}
		Collections.sort(retVal, Collections.reverseOrder());
		return retVal;
	}
	
	public synchronized boolean addPostToLatestPosts(Post currentPost){
		addPost(currentPost);
		boolean conversationFound = false;
		for(Post p : latestPosts){
//			Log.d("###########Cache", "post "+p.getId()+" in cahce");
			if(currentPost.getConversationId()==p.getConversationId()){
				conversationFound = true;
//				Log.d("###########Cache", "currentPost "+p.getId() + " in conv id " )
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
	
 	public synchronized List<Post> getLatestPosts(PostListItemAdapter adapter, List<Post> adapterData){
		Log.d("PostsCache", latestPosts.size()+"");
		return this.getLatestPosts(adapter, adapterData, false);
	}
	
	public synchronized List<Post> getLatestPosts(PostListItemAdapter adapter, List<Post> adapterData, boolean forcedUpdate){
		startService_getLatest(adapter, adapterData, forcedUpdate);
		return latestPosts;
	}
	
	public synchronized List<Post> getPostsByConversationId(long conversationId, PostListItemAdapter adapter, List<Post> adapterData){
		List<Post> retVal = getPostsByConversationId(conversationId);
//		Log.d("WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID", "postsCache "+conversationId);
		startService_getConversationByPostId(conversationId, adapter, adapterData);
//		Log.d("PostsCache.getPostsByConversationId()", retVal.size()+"");
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
	
	public synchronized List<Post> getNotifications(String userId, PostListItemAdapter adapter, List<Post> adapterData, Button button, boolean forcedUpdate, int postResultReceiverType){
		if (isRequestAllowed(timestamp_NotificationsCall, forcedUpdate)){
			startService_getNotifications(userId, adapter, adapterData, button, postResultReceiverType);
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
	
	public synchronized List<Post> getNearbyPosts(double latitude, double longitude, PostListItemAdapter adapter, List<Post> adapterData, Button button, boolean forcedUpdate, int postResultReceiverType){
		startService_getNearby(latitude, longitude, adapter, adapterData, button, postResultReceiverType);
		return nearbyPosts;
	}
	
	public synchronized List<Post> getNearbyPosts(){
		return this.nearbyPosts;
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
	
	private void startService_getConversationByPostId(long conversationId, PostListItemAdapter adapter, List<Post> adapterData){
		Intent intent = new Intent(this.context, WorkerService.class);
        intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID);
        intent.putExtra(StringKeys.CONVERSATION_FROM_CONVERSATION_ID, conversationId);
        
        intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER);
        PostsResultReceiver resultReceiver = new PostsResultReceiver(new Handler());
        resultReceiver.setAdapter(adapter);
        resultReceiver.setAdapterData(adapterData);
        intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
        
        this.context.startService(intent);
	}
	
	private void startService_getNotifications(String userId, PostListItemAdapter adapter, List<Post> adapterData, Button button, int postResultReceiverType){
		Intent intent = new Intent(this.context, WorkerService.class);
		intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_NOTIFICATIONS);
		intent.putExtra(StringKeys.NOTIFICATIONS_USER_ID, userId);
		
		intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, postResultReceiverType);
		PostsResultReceiver resultReceiver = new PostsResultReceiver(new Handler());
		resultReceiver.setAdapter(adapter);
		resultReceiver.setAdapterData(adapterData);
		resultReceiver.setView(button);
		intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);

		this.context.startService(intent);
	}
	
	private void startService_getNearby(double latitude, double longitude, PostListItemAdapter adapter, List<Post> adapterData, Button button, int postResultReceiverType){
		Intent intent = new Intent(this.context, WorkerService.class);
		intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_NEARBY_POSTS);
		intent.putExtra(StringKeys.NEARBY_POSTS_LATITUDE, latitude);
		intent.putExtra(StringKeys.NEARBY_POSTS_LONGITUDE, longitude);
		
		intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, postResultReceiverType);
		PostsResultReceiver resultReceiver = new PostsResultReceiver(new Handler());
		resultReceiver.setAdapter(adapter);
		resultReceiver.setAdapterData(adapterData);
		resultReceiver.setView(button);
		intent.putExtra(StringKeys.POST_LIST_RESULT_RECEIVER, resultReceiver);
		
		this.context.startService(intent);
	}
	
	private void startService_getLatest(PostListItemAdapter adapter, List<Post> adapterData, boolean forcedUpdate){
		if (isRequestAllowed(this.timestamp_PostsCall, forcedUpdate)){
			Intent intent = new Intent(context, WorkerService.class);
	        intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_LATEST_POSTS);
	        
	        intent.putExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC);
	        PostsResultReceiver resultReceiver = new PostsResultReceiver(new Handler());
	        resultReceiver.setAdapter(adapter);
	        resultReceiver.setAdapterData(adapterData);
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
//				Log.d("<<<<<<<<PostsCache>>>>>>>>", "diff: "+difference);
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
