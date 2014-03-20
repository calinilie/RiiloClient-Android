package com.riilo.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.riilo.main.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class WorkerService extends IntentService{

//	public static int id = 0;
	protected String deviceId="";
	private PostsCache postsCache; 
	private LocationHistoryManager locationHistoryManager;
	private static final String TAG = "<<<<<<<<<WORKER SERVICE>>>>>>>>>"; 
	
	private Facade facade;
	
	public WorkerService() {
		super("WorkerIntentService");
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		deviceId = Secure.getString(this.getContentResolver(),
            Secure.ANDROID_ID);
		postsCache = PostsCache.getInstance(this);
		locationHistoryManager = LocationHistoryManager.getInstance(this);
		facade = Facade.getInstance(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		return Service.START_NOT_STICKY;
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
		GoogleCloudMessaging gcm = null;
		switch (intentType){
		case StringKeys.WS_INTENT_POST:
			Post post = new Post(intent.getBundleExtra(StringKeys.POST_BUNDLE));
			PostInsertedDTO result = uploadPost(post);
			long newPostId = result.getPostId();
			conversationId = result.getConversationId();
			if (result.isSuccess()){
				post.setId(newPostId);
				post.setConversationId(conversationId);
				savePostLocally(post);
			}
			postsCache.addPost(post);
			break;
		case StringKeys.WS_INTENT_GET_LATEST_POSTS:
			resultReceiver = intent.getParcelableExtra(StringKeys.POST_LIST_RESULT_RECEIVER);
			List<Post> latestPosts = getLatestPosts(0, 50);
			resultReceiverType = intent.getIntExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC);
			if (latestPosts!=null){
				resultData = new Bundle();
				resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(latestPosts));
				resultReceiver.send(resultReceiverType, resultData);
			}
			break;
		case StringKeys.WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID:
			resultReceiver = intent.getParcelableExtra(StringKeys.POST_LIST_RESULT_RECEIVER);
			resultReceiverType = intent.getIntExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_ASC);
			conversationId = intent.getLongExtra(StringKeys.CONVERSATION_FROM_CONVERSATION_ID, 0);
			if (conversationId!=0){
				List<Post> postsInConversation = getConverstionByConversationId(conversationId);
				resultData = new Bundle();
				resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(postsInConversation));
				resultReceiver.send(resultReceiverType, resultData);
			}
			break;
		case StringKeys.WS_INTENT_GET_NOTIFICATIONS:
			resultReceiver = intent.getParcelableExtra(StringKeys.POST_LIST_RESULT_RECEIVER);
			resultReceiverType = intent.getIntExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW);
			userId = intent.getStringExtra(StringKeys.NOTIFICATIONS_USER_ID);
			if (userId!=null){
				List<Post> notifications = getNotificationsForUser(userId);
				if (resultReceiver!=null){
					resultData = new Bundle();
					resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(notifications));
					resultData.putInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, postsCache.getNotifications().size());
					resultReceiver.send(resultReceiverType, resultData);
				}
			}
			break;
		case StringKeys.WS_INTENT_NOTIFICATIONS_SILENCE:
			conversationId = intent.getLongExtra(StringKeys.NOTIFICATION_SILENCE_CONVERSATION_ID, 0);
			userId = intent.getStringExtra(StringKeys.NOTIFICATION_SILENCE_USER_ID);
			if (conversationId>0 && userId!=null && !userId.isEmpty()){
				List<Post> postsToSilence = postsCache.getPostsByConversationId(conversationId);
				List<Long> postIds = new ArrayList<Long>();
				for(Post p : postsToSilence){
					postIds.add(p.getId());
				}
				silenceNotifications(postIds, userId);
			}
			break;
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
			double distance = intent.getDoubleExtra(StringKeys.AT_LOCATION_POSTS_DISTANCE, 100);
			resultReceiver = intent.getParcelableExtra(StringKeys.AT_LOCATON_POSTS_RESULT_RECEIVER);
			if (latitude!=0 && longitude!=0){
				List<Post> postsAtLocation = getAtLocationPosts(latitude, longitude, distance);
				if (resultReceiver!=null){
					resultData = new Bundle();
					resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(postsAtLocation));
					resultReceiver.send(StringKeys.AT_LOCATION_POSTS_RESULT_RECEIVER_ADD_POSTS, resultData);
				}
			}
			break;
			
		case StringKeys.WS_INTENT_GET_POST_GROUPS_ON_MAP:
			resultReceiver = intent.getParcelableExtra(StringKeys.AT_LOCATON_POSTS_RESULT_RECEIVER);
			List<Post> onMapPostGroups = getPostsOnMap();
			if (resultReceiver!=null){
				resultData = new Bundle();
				Helpers.mergeLists(postsCache.getExplore_onMapPosts() , onMapPostGroups) ;
				Helpers.mergeLists(postsCache.getExplore_onMapPostGroups(), onMapPostGroups);
				resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(onMapPostGroups));
				resultReceiver.send(StringKeys.AT_LOCATION_POSTS_RESULT_RECEIVER_ADD_POST_GROUPS, resultData);
			}
			break;
			
		case StringKeys.WS_INTENT_INSERT_LOCATION_HISTORY:
			LocationHistory location = facade.getLastKnownLocation();
			location.setUserId(deviceId);
			postLocationHistory(location);
			break;
		case StringKeys.WS_INTENT_GET_LOCATION_HISTORY:
			List<LocationHistory> list = getLocationHistory(); 
			resultReceiver = intent.getParcelableExtra(StringKeys.LOCATION_HISTORY_RESULT_RECEIVER);
			if (list!=null && !list.isEmpty()){
				locationHistoryManager.mergeLocationhistories(list);
//				Facade.getInstance(WorkerService.this).insertOutsideLocationHistory(list);
				resultData = new Bundle();
				resultData.putParcelable(StringKeys.LOCATION_HISTORY_PARCELABLE, new LocationHistoryParcelable(locationHistoryManager.getLocationHistory()));
				resultReceiver.send(123, resultData);
			}
			break;
		case StringKeys.WS_INTENT_REGISTER_FOR_GCM:
			gcm = GoogleCloudMessaging.getInstance(this);
			String regId = "";
			try {
				regId = gcm.register("271980103838");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!regId.isEmpty()){
				facade.upsert_AppStorage_GCMRegistrationId(regId);
				postDeviceSNSRegId(regId);
			}
			break;
		case StringKeys.WS_INTENT_PROCESS_GCM_MESSAGE:
			Bundle extras = intent.getExtras();
	        gcm = GoogleCloudMessaging.getInstance(this);
	        String messageType = gcm.getMessageType(intent);

	        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
	            if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//	                sendNotification("Send error: " + extras.toString());
	            	Log.e(TAG, "Send error: " + extras.toString());
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_DELETED.equals(messageType)) {
//	                sendNotification("Deleted messages on server: " + extras.toString());
	            	Log.e(TAG, "Send error: " + extras.toString());
	            // If it's a regular GCM message, do some work.
	            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	                // Post notification of received message.
	            	String notificationMessage = "";
	            	if (extras.containsKey("default"))
	            		notificationMessage = extras.getString("default");
	                sendNotification(notificationMessage);
	            }
	        }
	        // Release the wake lock provided by the WakefulBroadcastReceiver.
	        GcmBroadcastReceiver.completeWakefulIntent(intent);
			break;
		}
	}
	
	private void savePostLocally(Post post){
		facade.insertPost(post);
	}
	
	private PostInsertedDTO uploadPost(Post model){
		String postEndpoint = getResources().getString(R.string.endpoint_posts_upload);
		try{
			String jsonString = tryPostWithRetry(postEndpoint, model.toJson().toString());
			PostInsertedDTO retVal = jsonToPostInsertedDTO(jsonString);
			return retVal;
		}
		catch (Exception e) {
	        Log.e("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&", e.getLocalizedMessage(), e);
	    }
		return null;
	}

	private void postLocationHistory(LocationHistory locationHistory){
		if (locationHistory!=null && !locationHistory.isSent()){
			if (locationHistory.getLatitude()!= 0 || locationHistory.getLatitude()!=0){
				try {
					String json = locationHistory.toJson().toString();
					String endpoint = getString(R.string.endpoint_post_location);
					tryPostWithRetry(endpoint, json);
					facade.updateLastLocationSent();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void postDeviceSNSRegId(String registrationId){
		if (registrationId!=null && !registrationId.isEmpty()){
			try{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("deviceId", this.deviceId);
				jsonObject.put("regId", registrationId);
				jsonObject.put("platform", "android");
				String json = jsonObject.toString(); 
				String endpoint = getString(R.string.endpoint_device_insert);
				String result = tryPostWithRetry(endpoint, json);
				if (result.equals("true")){
					facade.appStorage_RegIdSaved();
				}
				else{
					facade.appStorage_RegIdChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<LocationHistory> getLocationHistory(){
		List<LocationHistory> retVal = new ArrayList<LocationHistory>();
		String endpoint = getString(R.string.endpoint_location_history);
		//get json with retry
		String json = getJson(endpoint);
		if (!isValidJsonResponse(json)){
			json = getJson(endpoint);
		}
		//check if there is something there
		if (!isValidJsonResponse(json))
			return null;
		//json -> objects
		try {
			JSONArray jsonArray = new JSONArray(json);
			int length = jsonArray.length();
			for (int i=0; i<length; i++){
				retVal.add(new LocationHistory(jsonArray.getJSONObject(i)));
			}
		} catch (JSONException e) {
			
		}
		return retVal;
	}
	
	private String silenceNotifications(List<Long> postIds, String userId){
		String endpoint = getResources().getString(R.string.endpoint_silence_notifications);
		JSONObject jsonObject = new JSONObject();
		try{
			jsonObject.put("userId", userId);
			JSONArray array = new JSONArray(postIds);
			return tryPostWithRetry(endpoint,
									"{"+String.format("\"userId\":\"%s\"", userId)+ ","+
									String.format("\"postIds\":%s", array.toString())+"}");
		}
		catch (Exception e) {
	        Log.e("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&", e.getLocalizedMessage(), e);
	    }
		return StringKeys.POST_REQUEST_FAILED;
	}
	
	private List<Post> getPostsOnMap(){
		String endpoint = getString(R.string.endpoint_posts_on_map);
		List<Post> retVal = getPostsWithRetry(endpoint);
		if (retVal!=null){
			//TODO add to cache
		}
		return retVal;
	}
	
	private List<Post> getLatestPosts(int start, int limit){
		//build endpoint
		String endpoint = getString(R.string.endpoint_latest_posts);
	    endpoint += String.format("%s/%s/", start, limit);
		
	    List<Post> retVal = getPostsWithRetry(endpoint);
//	    List<Post> toRemove = new ArrayList<Post>();
	    if (retVal!=null){
	    	for (Post p:retVal){
	    		savePostLocally(p);
				if (!postsCache.addPostToLatestPosts(p)){
//					toRemove.add(p);
				}
	    	}
//	    	retVal.removeAll(toRemove);
	    }
	    retVal = postsCache.getLatestPosts();
	    return retVal;
	}
	
	/*private List<Post> getNearbyPosts(double latitude, double longitude){
		return getNearbyPosts(latitude, longitude, 0);
	}*/
	
	private List<Post> getNearbyPosts(double latitude, double longitude){
		String endpoint = getString(R.string.endpoint_nearby_posts);
		int distance = 20;
		endpoint = String.format("%s%s/%s/%s/", endpoint, latitude+"", longitude+"", distance+"");
		List<Post> retVal = getPostsWithRetry(endpoint);
		List<Post> postsToRemove = new ArrayList<Post>();//posts from conversation already present, only one post per conversation in List
		if (retVal!=null){
			for (Post p:retVal){
				if (!postsCache.addPostAsNearbyPost(p)){
					postsToRemove.add(p);
				}
	    	}
			retVal.removeAll(postsToRemove);
		}
		return retVal;
	}
	
	private List<Post> getAtLocationPosts(double latitude, double longitude, double distance){
		String endpoint = getString(R.string.endpoint_nearby_posts);
		endpoint = String.format("%s%s/%s/%s/", endpoint, latitude+"", longitude+"", distance+"");
		List<Post> retVal = getPostsWithRetry(endpoint);
		return retVal;
	}
	
	private List<Post> getConverstionByConversationId(long conversationId){
		String endpoint = getString(R.string.endpoint_conversation);
		endpoint += conversationId;
		
		List<Post> retVal = getPostsWithRetry(endpoint);
		
		if (retVal!=null){
	    	for (Post p:retVal){
				postsCache.addPost(p);
	    	}
		}
		return retVal;
	}
	
	private List<Post> getNotificationsForUser(String userId){
		List<Post> retVal = null;
		String endpoint = getString(R.string.endpoint_notifications);
		endpoint += userId;
		
		retVal = getPostsWithRetry(endpoint);
		List<Post> postsToRemove = new ArrayList<Post>();//posts from conversation already present, only one post per conversation in List
		if (retVal!=null){
			for(Post p:retVal){
				if (!postsCache.addPostAsNotification(p)){
					postsToRemove.add(p);
				}
			}
			retVal.removeAll(postsToRemove);
		}		
		return retVal;
	}
	
	private List<Post> getPostsWithRetry(String endpoint){
		List<Post> retVal = null;
		String jsonString = getJson(endpoint);
	    if (!isValidJsonResponse(jsonString)){
	    	jsonString = getJson(endpoint);
	    }
	    retVal = jsonStringToPostsList(jsonString);
	    
	    return retVal;
	}
	
	private String getJson(String endpoint){
		StringBuilder builder = new StringBuilder();
		
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
		
		HttpClient client = new DefaultHttpClient(httpParameters);
	    HttpGet httpGet = new HttpGet(endpoint);
	    try {
	      HttpResponse response = client.execute(httpGet);
	      StatusLine statusLine = response.getStatusLine();
		  
	      int statusCode = statusLine.getStatusCode();
	      if (statusCode == 200) {
	        HttpEntity entity = response.getEntity();
	        InputStream content = entity.getContent();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	        String line;
	        while ((line = reader.readLine()) != null) {
	          builder.append(line);
	        }
	      } else {
	        Log.e(WorkerService.class.toString(), "Failed to download file");
	      }
	    }catch(SocketTimeoutException e){
	    	Log.e("<<<<<<<<<Workerservice.getJson>>>>>>>>>", e.getLocalizedMessage(), e);
	    	return StringKeys.CONNECTION_TIMEDOUT;
	    } catch (ClientProtocolException e) {
	    	Log.e("<<<<<<<<<Workerservice.getJson>>>>>>>>>", e.getLocalizedMessage(), e);
	      	e.printStackTrace();
	    } catch (IOException e) {
	    	Log.e("<<<<<<<<<Workerservice.getJson>>>>>>>>>", e.getLocalizedMessage(), e);
	    	e.printStackTrace();
	    }
	    return builder.toString();
	}
	
	private List<Post> jsonStringToPostsList(String jsonString){
	    List<Post> retVal = new ArrayList<Post>();
	    if (jsonString==null || jsonString.isEmpty()){
	    	Log.e("<<<<<<<<<Workerservice.jsonStringToPostsList>>>>>>>>>", "jsonString is empty or NULL!");
	    	return null;
	    }
	    try {
			JSONArray array =  new JSONArray(jsonString);
			int length = array.length();
			for (int i=0; i<length; i++){
				Post p = new Post(array.getJSONObject(i));
				retVal.add(p);
			}
		} catch (JSONException e) {
			Log.e("<<<<<<<<<Workerservice.jsonStringToPostsList>>>>>>>>>", e.getLocalizedMessage(), e);
			e.printStackTrace();
			Toast.makeText(this, getString(R.string.error_connection_no_attempt), Toast.LENGTH_LONG).show();
			return null;
		}
	    return retVal;
	}
	
	private PostInsertedDTO jsonToPostInsertedDTO(String jsonString){
		PostInsertedDTO retVal = new PostInsertedDTO();
		
		if (jsonString==null || jsonString.equals(StringKeys.POST_REQUEST_FAILED)){
			retVal.setSuccess(false);
			Toast.makeText(this, getString(R.string.error_upload_post), Toast.LENGTH_LONG).show();
			return retVal;
		}
		
		if (jsonString!=null && !jsonString.isEmpty()){
			try{
				JSONObject jsonObject = new JSONObject(jsonString);
				long postId = jsonObject.getLong("postId");
				long conversationId = jsonObject.getLong("conversationId");
				boolean success = jsonObject.getBoolean("success");
				retVal.setPostId(postId);
				retVal.setConversationId(conversationId);
				retVal.setSuccess(success);
			}
			catch(JSONException e){
				retVal.setSuccess(false);
			}
		}
		return retVal;
	}
	
	private String tryPostWithRetry(String endpoint, String json){
		String response = postJson(endpoint, json);
		if (response.equals(StringKeys.CONNECTION_TIMEDOUT)){
			response = postJson(endpoint, json);
		}
		else if (response.equals(StringKeys.POST_REQUEST_FAILED)){
			response = postJson(endpoint, json);
		}
		return response;
	}
	
	public String postJson(String endpoint, String json) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 3000);
		HttpConnectionParams.setSoTimeout(params, 10000);
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	    HttpClient httpclient = new DefaultHttpClient(params);
	    HttpPost httppost = new HttpPost(endpoint);

	    try {
	        httppost.setEntity(new StringEntity(json, "UTF-8"));
	        httppost.setHeader("Content-Type", "application/json; charset=utf-8");
	        HttpResponse response = httpclient.execute(httppost);
	        int responseCode = response.getStatusLine().getStatusCode();
	        if (responseCode == 200){
	        	HttpEntity entity = response.getEntity();
		        InputStream content = entity.getContent();
		        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		        String line = reader.readLine();
		        return line;//(int) Long.parseLong(line);
	        }
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	    catch (Exception e) {
			// TODO: handle exception
		}
	    return StringKeys.POST_REQUEST_FAILED;
	}
	
	private boolean isValidJsonResponse(String jsonString){
		if (jsonString==null || jsonString.isEmpty()){
			return false;
		}
		if (jsonString.equalsIgnoreCase(StringKeys.CONNECTION_TIMEDOUT)){
			return false;
		}
		return true;
	}
	
	private void sendNotification(String msg) {
		NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent startRiiloIntent = new Intent(this, MainActivity.class);
		startRiiloIntent.putExtra(StringKeys.SHOW_NOTIFICATIONS_TAB_FIRST, true);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, startRiiloIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.riilo_logo)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setAutoCancel(true)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText(msg)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }
}



