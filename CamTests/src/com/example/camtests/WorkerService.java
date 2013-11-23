package com.example.camtests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

public class WorkerService extends IntentService{

//	public static int id = 0;
	protected String deviceId="";
	private PostsCache postsCache; 
	
	public WorkerService() {
		super("WorkerIntentService");
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		deviceId = Secure.getString(this.getContentResolver(),
            Secure.ANDROID_ID);
		postsCache = PostsCache.getInstance(this);
		
		Log.d(">>>>>>>>>>>worker intent<<<<<<<<<<<", "onCreate Called ");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		Log.d(">>>>>>>>>>>worker intent<<<<<<<<<<<", "onStartCommand Called");
		return Service.START_NOT_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(">>>>>>>>>>>worker intent<<<<<<<<<<<", "onHandleIntent Called ");
		Bundle resultData = null;
		ResultReceiver resultReceiver = null;
		int resultReceiverType = 0;
		long conversationId = 0;
		String userId = null;
		int intentType = intent.getIntExtra(StringKeys.WS_INTENT_TYPE, -1);
		switch (intentType){
		case StringKeys.WS_INTENT_POST:
			Post post = new Post(intent.getBundleExtra(StringKeys.POST_BUNDLE));
//			Log.d(">>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<", post.getConversationId()+"");
			PostInsertedDTO result = uploadPost(post);
			long newPostId = result.getPostId();
			conversationId = result.getConversationId();
			if (result.isSuccess()){
				post.setId(newPostId);
				post.setConversationId(conversationId);
				savePostLocally(post);
			}
			
//			Log.d("<<<<<<<<<Workerservice.uploadPost 1111 >>>>>>>>>", "No of posts in cahce: "+postsCache.getPostsAsList().size());
			postsCache.addPost(post);
//			Log.d("<<<<<<<<<Workerservice.uploadPost 2222 >>>>>>>>>", "No of posts in cahce: "+postsCache.getPostsAsList().size());
//			Log.d("************************************", post.toString());
			Log.d("************************************", "WS_INTENT_POST");
			break;
		case StringKeys.WS_INTENT_GET_LATEST_POSTS:
			Log.d("************************************", "WS_INTENT_GET_LATEST_POSTS");
			resultReceiver = intent.getParcelableExtra(StringKeys.POST_LIST_RESULT_RECEIVER);
			List<Post> latestPosts = getLatestPosts(0, 50);
			if (latestPosts==null){
				Log.d("<<<<<<<#####<<<<<"+WorkerService.class.toString()+">>>>>########>>>>>>>", "getLatestposts(start, limit) FAILED");
			}
//			Log.d(">>>>>>>>>>>worker intent<<<<<<<<<<<", "getLatestsPosts() finished");
			resultReceiverType = intent.getIntExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC);
			if (latestPosts!=null){
//				Log.d(">>>>>>>>>>>worker intent<<<<<<<<<<<", "getLatestsPosts != null");
				resultData = new Bundle();
				resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(latestPosts));
				resultReceiver.send(resultReceiverType, resultData);
			}
//			Log.d(">>>>>>>>>>>worker intent<<<<<<<<<<<", "getLatestPosts == NULL");
			break;
		case StringKeys.WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID:
			resultReceiver = intent.getParcelableExtra(StringKeys.POST_LIST_RESULT_RECEIVER);
			resultReceiverType = intent.getIntExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_ASC);
			conversationId = intent.getLongExtra(StringKeys.CONVERSATION_FROM_CONVERSATION_ID, 0);
//			Log.d("WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID", conversationId +"");
			if (conversationId!=0){
				List<Post> postsInConversation = getConverstionByConversationId(conversationId);
				resultData = new Bundle();
				resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(postsInConversation));
				resultReceiver.send(resultReceiverType, resultData);
			}
			Log.d("************************************", "WS_INTENT_GET_CONVERSATION_FROM_CONVERSATION_ID");
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
			Log.d("************************************", "WS_INTENT_GET_NOTIFICATIONS");
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
			Log.d("************************************", "WS_INTENT_NOTIFICATIONS_SILENCE");
			break;
		case StringKeys.WS_INTENT_NEARBY_POSTS:
			double latitude = intent.getDoubleExtra(StringKeys.NEARBY_POSTS_LATITUDE, 0);
			double longitude = intent.getDoubleExtra(StringKeys.NEARBY_POSTS_LONGITUDE, 0);
			resultReceiver = intent.getParcelableExtra(StringKeys.POST_LIST_RESULT_RECEIVER);
			resultReceiverType = intent.getIntExtra(StringKeys.POST_RESULT_RECEIVER_TYPE, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW);
			if (latitude != 0 && longitude != 0){
				List<Post> nearbyPosts = getNearbyPosts(latitude, longitude);
				if (resultReceiver!=null){
					resultData = new Bundle();
					resultData.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(nearbyPosts));
					resultData.putInt(StringKeys.POST_RESULT_RECEIVER_NOTIFICATION_NUMBER, postsCache.getNearbyPosts().size());//TODO change to NEARBY_POSTS_NUMBER
					resultReceiver.send(resultReceiverType, resultData);
				}
			}
			Log.d("************************************", "WS_INTENT_NEARBY_POSTS");
			break;
		}
	}
	
	@Override
	public void onDestroy(){
		Log.d(">>>>>>>>>>>worker intent<<<<<<<<<<<", "onDestroy Called ");
	}
	
	private void savePostLocally(Post post){
		/*if (post.getUri()!=null){
			String originalUri = post.getUri();
			try{
				Uri resizedUri = BitmapHelper.resizeAndSaveImage(getResources(), post.getUri());
				post.setUri(resizedUri.toString());
			}
			catch(OutOfMemoryError err){
				Log.e("<<<<<<<<<Workerservice.savePostLocally>>>>>>>>>", err.getLocalizedMessage(), err);
				post.setUri(originalUri);
			}
		}*/
		Facade.getInstance(this).insertPost(post);
	}
	
	private PostInsertedDTO uploadPost(Post model){
		String postEndpoint = getResources().getString(R.string.endpoint_posts_upload);
		try{
			Log.d("originLoc", "posting json: "+model.toJson().toString());
			String jsonString = tryPostWithRetry(postEndpoint, model.toJson().toString());
			PostInsertedDTO retVal = jsonToPostInsertedDTO(jsonString);
			return retVal;
		}
		catch (Exception e) {
	        Log.e("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&", e.getLocalizedMessage(), e);
	    }
		return null;
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
	
	private List<Post> getLatestPosts(int start, int limit){
		//build endpoint
		String endpoint = getString(R.string.endpoint_latest_posts);
	    endpoint += String.format("%s/%s/", start, limit);
		
	    List<Post> retVal = getPostsWithRetry(endpoint);
//	    List<Post> toRemove = new ArrayList<Post>();
	    if (retVal!=null){
	    	Log.d("<<<<<<<<<Workerservice.getLatestPosts>>>>>>>>>", "retrieved no of posts: "+retVal.size());
	    	for (Post p:retVal){
//				Facade.getInstance(this).insertForeignPost(p, deviceId);
	    		savePostLocally(p);//TODO uncoment the one above!
				if (!postsCache.addPostToLatestPosts(p)){
//					toRemove.add(p);
				}
	    	}
//	    	retVal.removeAll(toRemove);
	    }
	    retVal = postsCache.getLatestPosts();
	    return retVal;
	}
	
	private List<Post> getNearbyPosts(double latitude, double longitude){
		String endpoint = getString(R.string.endpoint_nearby_posts);
		int distance = 0;//server side controlled distance
		endpoint = String.format("%s%s/%s/%s/", endpoint, latitude+"", longitude+"", distance+"");
		
		List<Post> retVal = getPostsWithRetry(endpoint);
		List<Post> postsToRemove = new ArrayList<Post>();//posts from conversation already present, only one post per conversation in List
		if (retVal!=null){
			Log.d("<<<<<<<<<Workerservice.getNearbyPosts>>>>>>>>>", "retrieved no of nearby posts: "+retVal.size());
			for (Post p:retVal){
				if (!postsCache.addPostAsNearbyPost(p)){
					postsToRemove.add(p);
				}
	    	}
			retVal.removeAll(postsToRemove);
		}
		return retVal;
	}
	
	private List<Post> getConverstionByConversationId(long conversationId){
		String endpoint = getString(R.string.endpoint_conversation);
		endpoint += conversationId;
		
		List<Post> retVal = getPostsWithRetry(endpoint);
		
		if (retVal!=null){
			Log.d("<<<<<<<<<Workerservice.getConverstionByPostId>>>>>>>>>", "retrieved no of posts in conversation: "+retVal.size());
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
			Log.d("<<<<<<<<<Workerservice.getNotificationsForUser>>>>>>>>>", "retrieved no of notifications: "+retVal.size());
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
//	      Header[] headers = response.getHeaders("X-Cache");
//	      if (headers.length>0)
//	    	  Log.d("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH", headers[0].getValue());
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
//			Log.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", array.length()+"");
			int length = array.length();
			for (int i=0; i<length; i++){
//				Log.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", array.getJSONObject(i).toString());
				Post p = new Post(array.getJSONObject(i));
				retVal.add(p);
//				Log.d("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", p.displayInList());
			}
		} catch (JSONException e) {
			Log.e("<<<<<<<<<Workerservice.jsonStringToPostsList>>>>>>>>>", e.getLocalizedMessage(), e);
			e.printStackTrace();
			Toast.makeText(this, getString(R.string.connection_error_no_attempt), Toast.LENGTH_LONG).show();
			return null;
		}
	    return retVal;
	}
	
	private PostInsertedDTO jsonToPostInsertedDTO(String jsonString){
		PostInsertedDTO retVal = new PostInsertedDTO();
		
		if (jsonString==null || jsonString.equals(StringKeys.POST_REQUEST_FAILED)){
			retVal.setSuccess(false);
			Toast.makeText(this, getString(R.string.upload_post_error), Toast.LENGTH_LONG).show();
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
	
	private String tryPostWithRetry(String endpoint, String jsonPost){
		String response = postJson(endpoint, jsonPost);
		if (response.equals(StringKeys.CONNECTION_TIMEDOUT)){
			response = postJson(endpoint, jsonPost);
		}
		else if (response.equals(StringKeys.POST_REQUEST_FAILED)){
			response = postJson(endpoint, jsonPost);
		}
		return response;
	}
	
	public String postJson(String endpoint, String jsonPost) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 3000);
		HttpConnectionParams.setSoTimeout(params, 10000);
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	    HttpClient httpclient = new DefaultHttpClient(params);
	    HttpPost httppost = new HttpPost(endpoint);

	    try {
	        httppost.setEntity(new StringEntity(jsonPost, "UTF-8"));
	        Log.d("posting post:>>>>>>>>>>>>>>>>", jsonPost);
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
}
