package com.riilo.main;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.Marker;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class Post implements Comparable<Post>, Serializable{

	private final static String TAG = "<<<<<<<Post>>>>>>>";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private long repliesToPostId;
	private long conversationId;
	private int inAdditionToPostId;
	private String userId;
	private String uri;
	private double latitude;
	private double longitude;
	private double originLatitude;
	private double originLongitude;
	private float accuracy;
	private Date dateCreated;
	private String message;
	private boolean userAtLocation;
	private List<Integer> conversation;
	private double distanceFromCurLoc = -1;
	private Marker marker;
	private int priority;
	private int achievementId;
	private boolean isAnouncement;
	private String alias;
	
	public Post() {
	}

	public Post(String message, String uri, double lantitude, double longitude,
			float accuracy, boolean userAtLocation, Date dateCreated) {
		this.message = message;
		this.uri = uri;
		this.latitude = lantitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		this.userAtLocation = userAtLocation;
		this.dateCreated = dateCreated;
	}
	
	public Post(Bundle bundle){
		if (bundle == null)
			throw new RuntimeException("bundle is null");
		
		this.message = bundle.getString(StringKeys.POST_MESSAGE);
		this.uri = bundle.getString(StringKeys.POST_PIC_URI);
		this.latitude = bundle.getDouble(StringKeys.POST_LATITUDE);
		this.longitude = bundle.getDouble(StringKeys.POST_LONGITUDE);
		this.accuracy = bundle.getFloat(StringKeys.POST_ACCURACY, -1);
		this.userAtLocation = bundle.getBoolean(StringKeys.POST_USER_AT_LOCATION, false);
		this.dateCreated = Helpers.stringToDate(bundle.getString(StringKeys.POST_DATE_CREATED));
		this.userId = bundle.getString(StringKeys.POST_USER_ID);
		this.id = bundle.getLong(StringKeys.POST_ID);
		this.repliesToPostId = bundle.getLong(StringKeys.POST_REPLIES_TO_POSTID);
		this.conversationId = bundle.getLong(StringKeys.POST_CONVERSATION_ID);
		this.originLatitude = bundle.getDouble(StringKeys.POST_ORIGIN_LATITUDE);
		this.originLongitude = bundle.getDouble(StringKeys.POST_ORIGIN_LONGITUDE);
		this.priority = bundle.getInt(StringKeys.POST_PRIORITY);
    	this.achievementId = bundle.getInt(StringKeys.POST_ACHIEVEMENT);
    	this.isAnouncement = bundle.getBoolean(StringKeys.POST_ISANOUNCEMENT);
    	this.alias = bundle.getString(StringKeys.POST_ALIAS);
	}
	
	public Post (JSONObject jsonObject) throws JSONException{
		this.id = jsonObject.getLong("postId");
		this.message = jsonObject.getString("message");
		this.latitude = jsonObject.getDouble("latitude");
		this.longitude = jsonObject.getDouble("longitude");
		this.accuracy = (float) jsonObject.getDouble("accuracy");
//		this.uri = jsonObject.getString("pictureUri");//TODO !!!
		this.dateCreated = Helpers.stringToDate(jsonObject.getString("createdDateAsString"));
		this.userAtLocation = jsonObject.getBoolean("userAtLocation");
		this.userId = jsonObject.getString("userId");
		this.repliesToPostId = jsonObject.getInt("repliesToPostId");
		this.inAdditionToPostId = jsonObject.getInt("inAdditionToPostId");
		this.conversationId = jsonObject.getLong("conversationId");
		this.priority = jsonObject.getInt("priority");
		this.achievementId = jsonObject.getInt("achievementId");
		this.isAnouncement = jsonObject.getBoolean("anouncement");
		if (!jsonObject.isNull("alias"))
			this.alias = jsonObject.getString("alias");
	}
	
	public Bundle toBundle(){
    	Bundle bundle = new Bundle();
    	bundle.putString(StringKeys.POST_MESSAGE, message);
    	bundle.putString(StringKeys.POST_PIC_URI, uri);
    	bundle.putDouble(StringKeys.POST_LATITUDE, latitude);
    	bundle.putDouble(StringKeys.POST_LONGITUDE, longitude);
    	bundle.putDouble(StringKeys.POST_ORIGIN_LATITUDE, originLatitude);
    	bundle.putDouble(StringKeys.POST_ORIGIN_LONGITUDE, originLongitude);
    	bundle.putFloat(StringKeys.POST_ACCURACY, accuracy);
    	bundle.putBoolean(StringKeys.POST_USER_AT_LOCATION, userAtLocation);
    	bundle.putString(StringKeys.POST_DATE_CREATED, Helpers.dateToString(dateCreated));
    	bundle.putString(StringKeys.POST_USER_ID, userId);
    	bundle.putLong(StringKeys.POST_ID, id);
    	bundle.putLong(StringKeys.POST_REPLIES_TO_POSTID, repliesToPostId);
    	bundle.putLong(StringKeys.POST_CONVERSATION_ID, this.conversationId);
    	bundle.putInt(StringKeys.POST_PRIORITY, this.priority);
    	bundle.putInt(StringKeys.POST_ACHIEVEMENT, this.achievementId);
    	bundle.putBoolean(StringKeys.POST_ISANOUNCEMENT, this.isAnouncement);
    	bundle.putString(StringKeys.POST_ALIAS, this.alias);
    	return bundle;
	}

	public JSONObject toJson() throws JSONException {
		JSONObject retVal = new JSONObject();
		retVal.put("message", this.message);
		retVal.put("latitude", this.latitude);
		retVal.put("longitude", this.longitude);
		retVal.put("accuracy", this.accuracy);
		retVal.put("originLatitude", this.originLatitude);
		retVal.put("originLongitude", this.originLongitude);
		retVal.put("isUserAtLocation", this.userAtLocation);
		retVal.put("userId", this.userId);
		retVal.put("postId", this.id);
		retVal.put("repliesToPostId", this.repliesToPostId);
		retVal.put("conversationId", this.conversationId);
		
//		retVal.put("createdDateAsString", Helpers.dateToString(dateCreated));//TODO review!
//		retVal.put("pictureUri", this.uri);//TODO
		
		
		return retVal;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double lantitude) {
		this.latitude = lantitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isUserAtLocation() {
		return userAtLocation;
	}

	public void setUserAtLocation(Location currentLocation, double lat, double longitude){
		if (currentLocation!=null && lat!=0 && longitude!=0){
			this.originLatitude = currentLocation.getLatitude();
			this.originLongitude = currentLocation.getLongitude();
			double distanceFromCurrentLocation = Helpers.distanceInKmFrom(lat, longitude, currentLocation.getLatitude(), currentLocation.getLongitude()); 
			if (distanceFromCurrentLocation<2){
				setUserAtLocation(true);
			}
			else{
				setUserAtLocation(false);
			}
		}
	}
	
	public void setUserAtLocation(boolean userAtLocation) {
		this.userAtLocation = userAtLocation;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getRepliesToPostId() {
		return repliesToPostId;
	}

	public void setRepliesToPostId(long replyToPostId) {
		this.repliesToPostId = replyToPostId;
	}

	public int getInAdditionToPostId() {
		return inAdditionToPostId;
	}

	public void setInAdditionToPostId(int inAdditionToPostId) {
		this.inAdditionToPostId = inAdditionToPostId;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getDateAsString(){
		if (dateCreated == null) return "no date available, tell us about this in the comments";
		return Helpers.dateToString(dateCreated);
	}

	public long getConversationId() {
		return this.conversationId;
	}

	public void setConversationId(long conversationId) {
		this.conversationId = conversationId;
	}

	@Override
	public String toString() {
		 return
		 String.format("ID: %s, message: %s, lat: %s, long: %s, accuracy: %s, USER AT LOC: %s, date: %s, repliesToPostId: %s, conversationId: %s",
		 id+"", message, latitude+"", longitude+"", accuracy+"", userAtLocation+"",
		 Helpers.dateToString(dateCreated), repliesToPostId+"", conversationId+"");
	}

	public String displayInList(){
		return String.format("id: %s", id+"");
	}

	@Override
	public int compareTo(Post another) {
		if (this.priority > another.priority)
			return 1;
		else if (this.priority < another.priority)
			return -1;
		
		return this.id > another.getId() ? 1 : this.id<another.getId() ? -1 : 0;
	}
	
	public boolean isNewer(Post another){
		return this.compareTo(another)==1;
	}
	
	public List<Integer> getConversation() {
		return conversation;
	}

	public void setConversation(List<Integer> conversation) {
		this.conversation = conversation;
	}

	@Override
	public boolean equals(Object another){
		if (another == null){
			return false;
		}
		if (!(another instanceof Post)){
			return false;
		}
		
		return this.id == ((Post)another).getId();
	}
	
	@Override
	public int hashCode() {
		long hash = 1;
		hash = hash * 17 + id;
		return (hash+"").hashCode();
	}
	
	public double getDistanceFromCurLoc(){
		return distanceFromCurLoc;
	}

	public double getDistanceFromCurLoc(Location location) {
		if (location!=null){
			if (distanceFromCurLoc==-1)
				distanceFromCurLoc = distanceFrom(
											this.latitude, 
											this.longitude, 
											location.getLatitude(), 
											location.getLongitude());
			return distanceFromCurLoc;
		}
		return -1;
	}
	
	public void setDistanceFromCurLoc(Location location){
		if (location!=null){
			distanceFromCurLoc = distanceFrom(
					this.latitude, 
					this.longitude, 
					location.getLatitude(), 
					location.getLongitude());
		}
	}
	
	public void setDistanceFromLastKnownLocation(LocationHistory location){
		if (location!=null){
			distanceFromCurLoc = distanceFrom(
										this.latitude, 
										this.longitude, 
										location.getLatitude(), 
										location.getLongitude());
		}
	}
	
	public void setDistanceFromLocation(double lat, double log){
		distanceFromCurLoc = distanceFrom(lat, log, this.latitude, this.longitude);
	}
	
	private double distanceFrom(double lat1, double lng1, double lat2, double lng2) {
		return Helpers.distanceFrom(lat1, lng1, lat2, lng2);
	}

	public Marker getMarker() {
		return marker;
		
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getAchievementId() {
		return achievementId;
	}

	public void setAchievementId(int achievementId) {
		this.achievementId = achievementId;
	}

	public boolean isAnouncement() {
		return isAnouncement;
	}

	public void setAnouncement(boolean isAnouncement) {
		this.isAnouncement = isAnouncement;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	
	public static Comparator<Post> PostIdComparator = new Comparator<Post>() {
		
		@Override
		public int compare(Post lhs, Post rhs) {
			return lhs.id > rhs.id ? 1 : lhs.id<rhs.id ? -1 : 0;
		}
	};
	
}
