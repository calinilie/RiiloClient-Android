package com.example.camtests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

public class Facade {

	private SQLiteDatabase database;
	private Adapter dataAdapter;
	private final static String[] postsTableCols = { 
									Adapter.POSTS_POST_ID,
									Adapter.POSTS_REPLYTO_POST_ID,
									Adapter.POSTS_INADDITIONTO_POST_ID,
									Adapter.POSTS_USER_ID,
									Adapter.POSTS_URI,
									Adapter.POSTS_LATITUDE, 
									Adapter.POSTS_LONGITUDE, 
									Adapter.POSTS_LOCATION_ACCURACY,
									Adapter.POSTS_DATE_CREATED,
									Adapter.POSTS_MESSAGE,
									Adapter.POSTS_USER_AT_LOCATION,
									Adapter.POSTS_CONVERSATION_ID
								};
	
	private static final String[] locationHistoryColumns = {
									Adapter.LOCATION_HISTORY_LATITUDE,
									Adapter.LOCATION_HISTORY_LONGITUDE,
									Adapter.LOCATION_HISTORY_ACCURACY,
									Adapter.LOCATION_HISTORY_DATE
								};
	
	private ContentValues values;

	private static Facade instance;
	private Facade(Context context) {
		dataAdapter = new Adapter(context);
		values = new ContentValues();
	}
	
	public static Facade getInstance(Context context){
		if (instance==null)
			instance = new Facade(context);
		return instance;
	}
	

	private void open() {
		database = dataAdapter.getWritableDatabase();
	}

	private void close() {
		dataAdapter.close();
	}
	
	public synchronized void insertPost(Post post){
		open();
		inserPost(post);
//		Log.d("******************************", post.toString());
		close();
	}
	
	public synchronized List<Post> getAllPosts(){
		open();
		List<Post> retVal = new ArrayList<Post>();
		String orderBy = Adapter.POSTS_POST_ID+" desc";
		Cursor cursor = database.query(Adapter.POSTS_TABLE, postsTableCols, null, null, null, null, orderBy, null);
		while (cursor.moveToNext()){
			Post post = new Post();
			post.setId(cursor.getInt(0));
			post.setRepliesToPostId(cursor.getInt(1));
			post.setInAdditionToPostId(cursor.getInt(2));
			post.setUserId(cursor.getString(3));
			post.setUri(cursor.getString(4));
			post.setLatitude(cursor.getDouble(5));
			post.setLongitude(cursor.getDouble(6));
			post.setAccuracy(cursor.getFloat(7));
			post.setDateCreated(new Date(cursor.getLong(8)));
			post.setMessage(cursor.getString(9));
			post.setUserAtLocation(cursor.getInt(10)==1 ? true : false);
			post.setConversationId(cursor.getInt(11));
			retVal.add(post);
//			Log.d("<<<<<<<<<<<<Facade.getAllPosts()>>>>>>>>>>>>", post.displayInList());
		}
//		if (retVal.size()>=1)
//			Log.d(">>>>>>>>>>>>>>>>>>>>>>>>>", retVal.get(retVal.size()-1).toString());
		close();
		Log.d("<<<<<<<<<<<<Facade.getAllPosts()>>>>>>>>>>>>", "posts in db: "+retVal.size());
		return retVal;
	}
	
	private synchronized boolean doesPostExist(long id){
		String selection = String.format("%s = ?", Adapter.POSTS_POST_ID);
		String[] selectionArgs = {id+""}; 
		Cursor cursor = database.query(Adapter.POSTS_TABLE, postsTableCols, selection, selectionArgs, null, null, null, null);
		if (cursor.moveToNext())
			return true;
		return false;
	}
	
	public synchronized void insertForeignPost(Post post, String deviceId){
		open();
		if (!deviceId.equalsIgnoreCase(post.getUserId())){
			if (!doesPostExist(post.getId())){
				Log.d(">>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<", "inserting post with id" + post.getId());
				insertPost(post);
			}
		}
		close();
	}
	
	private void inserPost(Post post){
		values.clear();
		if (post.getId()!=0)
			values.put(Adapter.POSTS_POST_ID, post.getId());
		if (post.getRepliesToPostId()!=0)
			values.put(Adapter.POSTS_REPLYTO_POST_ID, post.getRepliesToPostId());
		if (post.getInAdditionToPostId()!=0)
			values.put(Adapter.POSTS_INADDITIONTO_POST_ID, post.getInAdditionToPostId());
		values.put(Adapter.POSTS_USER_ID, post.getUserId());
		values.put(Adapter.POSTS_URI, post.getUri());
		values.put(Adapter.POSTS_DATE_CREATED, post.getDateCreated().getTime());
		values.put(Adapter.POSTS_LONGITUDE, post.getLongitude());
		values.put(Adapter.POSTS_LATITUDE, post.getLatitude());
		values.put(Adapter.POSTS_LOCATION_ACCURACY, post.getAccuracy());
		values.put(Adapter.POSTS_MESSAGE, post.getMessage());
		int userAtLocation = post.isUserAtLocation() ? 1 : 0;
		values.put(Adapter.POSTS_USER_AT_LOCATION, userAtLocation);
		values.put(Adapter.POSTS_CONVERSATION_ID, post.getConversationId());
		database.insert(Adapter.POSTS_TABLE, null, values);
	}
	
	public synchronized boolean insertLocationToHistoryIfNeeded(Location location, LocationHistory lastKnownLocation){
		if (location!=null && lastKnownLocation!=null){
				double distanceBetweenlocations = Helpers.distanceFrom(location.getLatitude(), 
																		location.getLongitude(), 
																		lastKnownLocation.getLatitude(), 
																		lastKnownLocation.getLongitude(), 
																		false);
				float accuracyDifference = Math.abs(location.getAccuracy()-lastKnownLocation.getAccuracy());
				if (distanceBetweenlocations>1){
					insertLocationToHistory(location);
					return true;
				}
				else if (accuracyDifference>100){
					insertLocationToHistory(location);
					return true;
				}
		}
		return false;
	}
	
	private synchronized void insertLocationToHistory(Location location){
		open();
		if (location!=null){
			values.clear();
			values.put(Adapter.LOCATION_HISTORY_LATITUDE, location.getLatitude());
			values.put(Adapter.LOCATION_HISTORY_LONGITUDE, location.getLongitude());
			values.put(Adapter.LOCATION_HISTORY_ACCURACY, location.getAccuracy());
			values.put(Adapter.LOCATION_HISTORY_DATE, Calendar.getInstance().getTimeInMillis());
			database.insert(Adapter.LOCATION_HISTORY_TABLE, null, values);
		}
		close();
	}
	
	public synchronized LocationHistory getLastKnownLocation(){
		open();
		LocationHistory retVal = new LocationHistory();
		String orderBy = Adapter.LOCATION_HISTORY_DATE+" desc";
		String limit = "1";
		Cursor cursor = database.query(Adapter.LOCATION_HISTORY_TABLE, locationHistoryColumns, null, null, null, null, orderBy, limit);
		while (cursor.moveToNext()){
			retVal.setLatitude(cursor.getDouble(0));
			retVal.setLongitude(cursor.getDouble(1));
			retVal.setAccuracy(cursor.getFloat(2));
			retVal.setDate(new Date(cursor.getLong(3)));
		}
//		if (retVal.getDate()!=null)
//			Log.d(">>>>>>>>>>>>>>>>>>>LAST LOCATION<<<<<<<<<<<<<<<<<<<<<<<", Helpers.dateToString(retVal.getDate()));
		close();
		return retVal;
		
	}
	
	/*public List<Post> getOwnPosts(String deviceId){
		open();
		List<Post> retVal = new ArrayList<Post>();
		String selection = String.format("%s = ?", Adapter.POSTS_USER_ID);
		String[] selectionArgs = {deviceId}; 
		Cursor cursor = database.query(Adapter.POSTS_TABLE, postsTableCols, selection, selectionArgs, null, null, null, null);
		while (cursor.moveToNext()){
			Post post = new Post();
			post.setId(cursor.getInt(0));
			post.setRepliesToPostId(cursor.getInt(1));
			post.setInAdditionToPostId(cursor.getInt(2));
			post.setUserId(cursor.getString(3));
			post.setUri(cursor.getString(4));
			post.setLatitude(cursor.getDouble(5));
			post.setLongitude(cursor.getDouble(6));
			post.setAccuracy(cursor.getFloat(7));
			post.setDateCreated(new Date(cursor.getLong(8)));
			post.setMessage(cursor.getString(9));
			post.setUserAtLocation(cursor.getInt(10)==1 ? true : false);
			retVal.add(post);
		}
		if (retVal.size()>=1)
			Log.d(">>>>>>>>>>>>>>>>>>>>>>>>>", retVal.get(retVal.size()-1).toString());
		close();
		return retVal;
	}*/

//	public int getCurrentHighScore(int levelId) {
//		Cursor cursor = database.query(Adapter.LEVELS_TABLE, level_cols, Adapter.LEVELS_ID + " = ?",
//				new String[]{levelId+""}, null, null, null);
//		int result = 0;
//		if (cursor.moveToFirst()) {
//			result = cursor.getInt(2);
//		}
//		cursor.close();
//		return result;
//	}
//
//	public DragonStats getDragonStats() {
//		Cursor cursor = database.query(Adapter.DRAGON_STATS_TABLE, stats_cols,
//				null, null, null, null, null);
//		DragonStats ds = null;
//		if (cursor.moveToFirst()) {
//			float MAX_HP = cursor.getFloat(0);
//			float hp = cursor.getFloat(1);
////			float hp = MAX_HP;
//			float MAX_STAMINA = cursor.getFloat(2);
//			 float stamina=cursor.getFloat(3);
////			float stamina = MAX_STAMINA;
//			float dmg = cursor.getFloat(4);
//			int XP = cursor.getInt(5);
//			int lvl = cursor.getInt(6);
//			int unusedSkillPoints = cursor.getInt(7);
//			float staminDropRate = cursor.getFloat(8);
//			// maxhp hp max_stamina stamin_drop| sta dmg XP lvl -
//			ds = new DragonStats(MAX_HP, hp, MAX_STAMINA, staminDropRate,
//					stamina, dmg, XP, lvl, unusedSkillPoints);
//		}
//		cursor.close();
//		return ds;
//	}
//
//	public boolean getLevelPreviouslyPassed(int levelId) {
//		String selection = Adapter.LEVELS_ID + " = ?";
//		String[] selectionArgs = new String[] { levelId + "" };
//		Cursor cursor = database.query(
//				Adapter.LEVELS_TABLE, 
//				level_cols,
//				selection, 
//				selectionArgs, 
//				null, null, null);
//		if (cursor.moveToFirst()) {
//			if (cursor.getInt(1) == 1)
//				return true;
//		}
//		return false;
//	}
//
//	public DragonBag getBag() {
//		Cursor cursor = database.query(Adapter.BAG_TABLE, bag_col, null, null,
//				null, null, null);
//		cursor.moveToFirst();
//		int spq = cursor.getInt(1);
//		cursor.moveToNext();
//		int hpq = cursor.getInt(1);
//		cursor.moveToNext();
//		int dpq = cursor.getInt(1);
//		cursor.moveToNext();
//		int gcq = cursor.getInt(1);
//		cursor.moveToNext();
//		int scq = cursor.getInt(1);
//		DragonBag dragonBag=new DragonBag(spq, hpq, dpq, gcq, scq);
//		
//		return dragonBag;
//	}

}
