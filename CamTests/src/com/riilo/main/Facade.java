package com.riilo.main;

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

	private static final String TAG = "FACADE";
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
									Adapter.LOCATION_HISTORY_DATE,
									Adapter.LOCATION_HISTORY_IS_SENT
								};
	
	private static final String[] appStorageColumns = {Adapter.APP_STORAGE_KEY_COLUMN, Adapter.APP_STORAGE_VALUE_COLUMN};
	
	private static final String[] outsideLcationHistoryId = {Adapter.OUTSIDE_LOCATION_HISTORY_ID};
	private static final String[] outsideLocationColumns = {Adapter.OUTSIDE_LOCATION_HISTORY_ID, Adapter.OUTSIDE_LOCATION_HISTORY_LATITUDE, Adapter.OUTSIDE_LOCATION_HISTORY_LONGITUDE};
	
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
		if (!doesPostExist(post.getId())){
			inserPost(post);
		}
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
		}
		deleteOldPosts(retVal.size());
		close();
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
	
	private void deleteOldPosts(int totalCount){
		int oldPostsCount = totalCount - 300;
		if (oldPostsCount>0){
			String whereClause = String.format(" %s in (select %s from %s order by %s limit %s)", Adapter.POSTS_POST_ID, Adapter.POSTS_POST_ID, Adapter.POSTS_TABLE, Adapter.POSTS_POST_ID, oldPostsCount+"");
			database.delete(Adapter.POSTS_TABLE, whereClause, null);
		}
	}
	
	@Deprecated
	public synchronized void insertForeignPost(Post post, String deviceId){
		open();
		if (!deviceId.equalsIgnoreCase(post.getUserId())){
			if (!doesPostExist(post.getId())){
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
				double distanceBetweenlocations = Helpers.distanceInKmFrom(location.getLatitude(), 
																		location.getLongitude(), 
																		lastKnownLocation.getLatitude(), 
																		lastKnownLocation.getLongitude());
//				float accuracyDifference = Math.abs(location.getAccuracy()-lastKnownLocation.getAccuracy());
				if (distanceBetweenlocations>1){
					insertLocationToHistory(location);
					return true;
				}
//				else if (accuracyDifference>100){
//					insertLocationToHistory(location);
//					return true;
//				}
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
		LocationHistory retVal = new LocationHistory();
		try{
			open();
			String orderBy = Adapter.LOCATION_HISTORY_DATE+" desc";
			String limit = "1";
			Cursor cursor = database.query(Adapter.LOCATION_HISTORY_TABLE, locationHistoryColumns, null, null, null, null, orderBy, limit);
			while (cursor.moveToNext()){
				retVal.setLatitude(cursor.getDouble(0));
				retVal.setLongitude(cursor.getDouble(1));
				retVal.setAccuracy(cursor.getFloat(2));
				retVal.setDate(new Date(cursor.getLong(3)));
				retVal.setSent(cursor.getInt(4) == 1);
			}
		}
		catch(Exception e){
		}
		finally{
			close();
		}
		return retVal;
	}
	
	public synchronized void insertOutsideLocationHistory(List<LocationHistory> list){
		open();
		for(LocationHistory l:list){
			if (!doesLocationHistoryExist(l.getLocationHistoryId())){
				values.clear();
				values.put(Adapter.OUTSIDE_LOCATION_HISTORY_ID, l.getLocationHistoryId());
				values.put(Adapter.OUTSIDE_LOCATION_HISTORY_LATITUDE, l.getLatitude());
				values.put(Adapter.OUTSIDE_LOCATION_HISTORY_LONGITUDE, l.getLongitude());
				values.put(Adapter.OUTSIDE_LOCATION_HISTORY_DATE, Helpers.dateToString(l.getDate()));
				database.insert(Adapter.OUTSIDE_LOCATION_HISTORY_TABLE, null, values);
			}
		}
		close();
	}
	
	public synchronized List<LocationHistory> getLocationHistory(){
		List<LocationHistory> retVal = new ArrayList<LocationHistory>();
		try{
			open();
			Cursor cursor = database.query(Adapter.OUTSIDE_LOCATION_HISTORY_TABLE, outsideLocationColumns, null, null, null, null, null, "500");
			while (cursor.moveToNext()){
				LocationHistory location = new LocationHistory();
				location.setLocationHistoryId(cursor.getLong(0));
				location.setLatitude(cursor.getDouble(1));
				location.setLongitude(cursor.getDouble(2));
				retVal.add(location);
			}
		}
		catch(Exception e){}
		finally{
			close();
		}
		return retVal;
	}
	
	private synchronized boolean doesLocationHistoryExist(long id){
		try{
			String[] whereArgs = {id+""};
			Cursor cursor = database.query(
								Adapter.OUTSIDE_LOCATION_HISTORY_TABLE, 
								outsideLcationHistoryId, 
								Adapter.OUTSIDE_LOCATION_HISTORY_ID+" = ?", 
								whereArgs , null, null, null);
			if (cursor.moveToNext())
				return true;
		}
		catch(Exception e){}
		return false;
	}
	
	public synchronized boolean wasTutorialRun(){
		open();
		boolean retVal = false;
		try{
			String selection = String.format("%s = ?", Adapter.APP_STORAGE_KEY_COLUMN);
			String[] selectionArgs = {Adapter.APP_STORAGE_KEY_TUTORIAL_RUN}; 
			Cursor cursor = database.query(Adapter.APP_STORAGE_TABLE, appStorageColumns, selection, selectionArgs, null, null, null);
			if (cursor.moveToNext()){
				int value = cursor.getInt(1);
				retVal = value == 1;
			}
		}
		catch(Exception e)
		{
			//TODO add ga tracking
		}
		finally{
			close();
		}
		return retVal;
	}
	
	public synchronized void updateTutorialRun(){
		open();
		try{
			values.clear();
			values.put(Adapter.APP_STORAGE_VALUE_COLUMN, 1);
			String[] whereArgs = {Adapter.APP_STORAGE_KEY_TUTORIAL_RUN};
			database.update(Adapter.APP_STORAGE_TABLE, values, Adapter.APP_STORAGE_KEY_COLUMN+" = ?", whereArgs);
		}
		catch(Exception e){
			//TODO add ga tracking
		}
		finally{
			close();
		}
	}
	
	public synchronized boolean wasTutorialMarkerRun(){
		open();
		boolean retVal = false;
		try{
			String selection = String.format("%s = ?", Adapter.APP_STORAGE_KEY_COLUMN);
			String[] selectionArgs = {Adapter.APP_STORAGE_KEY_TUTORIAL_MARKER_RUN}; 
			Cursor cursor = database.query(Adapter.APP_STORAGE_TABLE, appStorageColumns, selection, selectionArgs, null, null, null);
			if (cursor.moveToNext()){
				int value = cursor.getInt(1);
				retVal = value == 1;
			}
		}
		catch(Exception e){
			//TODO add ga tracking
		}
		finally{
			close();
		}
		return retVal;
	}
	
	public synchronized void updateTutorialMarkerRun(){
		open();
		try{
			values.clear();
			values.put(Adapter.APP_STORAGE_VALUE_COLUMN, 1);
			String[] whereArgs = {Adapter.APP_STORAGE_KEY_TUTORIAL_MARKER_RUN};
			database.update(Adapter.APP_STORAGE_TABLE, values, Adapter.APP_STORAGE_KEY_COLUMN+" = ?", whereArgs);
		}
		catch(Exception e){
			//TODO add ga tracking
		}
		finally{
			close();
		}
	}
	
	public synchronized boolean wasTutorialSwipeRun(){
		open();
		boolean retVal = false;
		try{
			String selection = String.format("%s = ?", Adapter.APP_STORAGE_KEY_COLUMN);
			String[] selectionArgs = {Adapter.APP_STORAGE_KEY_TUTORIAL_SWIPE_RUN}; 
			Cursor cursor = database.query(Adapter.APP_STORAGE_TABLE, appStorageColumns, selection, selectionArgs, null, null, null);
			if (cursor.moveToNext()){
				int value = cursor.getInt(1);
				retVal = value == 1;
			}
		}
		catch(Exception e){
			//TODO add ga tracking
		}
		finally{
			close();
		}
		return retVal;
	}
	
	public synchronized void updateTutorialSwipeRun(){
		open();
		try{
			values.clear();
			values.put(Adapter.APP_STORAGE_VALUE_COLUMN, 1);
			String[] whereArgs = {Adapter.APP_STORAGE_KEY_TUTORIAL_SWIPE_RUN};
			database.update(Adapter.APP_STORAGE_TABLE, values, Adapter.APP_STORAGE_KEY_COLUMN+" = ?", whereArgs);
		}
		catch(Exception e){
			//TODO add ga tracking
		}
		finally{
			close();
		}
	}
	
	public synchronized void updateLastLocationSent(){
		open();
		values.clear();
		values.put(Adapter.LOCATION_HISTORY_IS_SENT, 1);
		database.update(Adapter.LOCATION_HISTORY_TABLE, values, null, null);
		close();
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

		close();
		return retVal;
	}*/

}
