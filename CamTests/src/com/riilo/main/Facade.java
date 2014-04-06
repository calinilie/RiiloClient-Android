package com.riilo.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import android.util.SparseArray;

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
									Adapter.POSTS_CONVERSATION_ID,
									Adapter.POSTS_ALIAS,
									Adapter.POSTS_ACHIEVEMENT_ID
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
		initTutorialsMap();
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
		try{
			if (location!=null){
				values.clear();
				values.put(Adapter.LOCATION_HISTORY_LATITUDE, location.getLatitude());
				values.put(Adapter.LOCATION_HISTORY_LONGITUDE, location.getLongitude());
				values.put(Adapter.LOCATION_HISTORY_ACCURACY, location.getAccuracy());
				values.put(Adapter.LOCATION_HISTORY_DATE, Calendar.getInstance().getTimeInMillis());
				database.insert(Adapter.LOCATION_HISTORY_TABLE, null, values);
			}
		}
		catch(Exception e){
			
		}
		finally{
			close();
		}
	}
	
	public synchronized LocationHistory getLastKnownLocation(){
		LocationHistory retVal = new LocationHistory();
		Cursor cursor = null;
		try{
			open();
			String orderBy = Adapter.LOCATION_HISTORY_DATE+" desc";
			String limit = "1";
			cursor = database.query(Adapter.LOCATION_HISTORY_TABLE, locationHistoryColumns, null, null, null, null, orderBy, limit);
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
			if (cursor!=null)
				cursor.close();
			close();
		}
		return retVal;
	}

	public synchronized List<LocationHistory> getLocationHistory(){
		List<LocationHistory> retVal = new ArrayList<LocationHistory>();
		Cursor cursor = null;
		try{
			open();
			cursor = database.query(Adapter.OUTSIDE_LOCATION_HISTORY_TABLE, outsideLocationColumns, null, null, null, null, null, "3000");
			while (cursor.moveToNext()){
				double latitude = cursor.getDouble(1);
				double longitude = cursor.getDouble(2);
				LocationHistory location = new LocationHistory(latitude, longitude);
				location.setLocationHistoryId(cursor.getLong(0));
				retVal.add(location);
			}
		}
		catch(Exception e){}
		finally{
			if (cursor!=null)
				cursor.close();
			close();
		}
		return retVal;
	}
	
	private synchronized boolean doesLocationHistoryExist(long id){
		Cursor cursor = null;
		try{
			String[] whereArgs = {id+""};
			cursor = database.query(
								Adapter.OUTSIDE_LOCATION_HISTORY_TABLE, 
								outsideLcationHistoryId, 
								Adapter.OUTSIDE_LOCATION_HISTORY_ID+" = ?", 
								whereArgs , null, null, null);
			if (cursor.moveToNext())
				return true;
		}
		catch(Exception e){}
		finally{
			if (cursor!=null)
				cursor.close();
		}
		return false;
	}
	
	public synchronized void updateLastLocationSent(){
		open();
		try{
			values.clear();
			values.put(Adapter.LOCATION_HISTORY_IS_SENT, 1);
			database.update(Adapter.LOCATION_HISTORY_TABLE, values, null, null);
		}
		catch(Exception e){
			
		}
		finally{
			close();
		}
	}
	
	//========APP STORAGE METHODS=================================================================================
	
	public synchronized void upsert_AppStorage_GCMRegistrationId(String registrationId){
		open();
		try{
			if (doesAppStorageKeyExist(Adapter.APP_STORAGE_KEY_GCM_REG_ID)){
				this.updateAppStorageValue(Adapter.APP_STORAGE_KEY_GCM_REG_ID, registrationId);
			}
			else{
				this.insertAppStorageKeyValuePair(Adapter.APP_STORAGE_KEY_GCM_REG_ID, registrationId);
			}
		}
		catch(Exception e){
		
		}
		finally{
			close();
			this.appStorage_RegIdChanged();
		}
	}
	
	public synchronized void upsert_AppStorage_AppVersion(int appVersion){
		open();
		try{
			if (this.doesAppStorageKeyExist(Adapter.APP_STORAGE_KEY_APP_VERSION)){
				this.updateAppStorageValue(Adapter.APP_STORAGE_KEY_APP_VERSION, appVersion+"");
			}
			else {
				this.insertAppStorageKeyValuePair(Adapter.APP_STORAGE_KEY_APP_VERSION, appVersion+"");
			}
		}
		catch(Exception e){
			
		}
		finally{
			close();
		}
	}
	
 	public synchronized String getGCMRegistrationId(){
 		String retVal="";
 		open();
 		try{
	 		retVal = this.getStringAppStorageValue(Adapter.APP_STORAGE_KEY_GCM_REG_ID);
 		}
 		catch(Exception e){}
 		finally{close();}
		return retVal;
	}

	public synchronized int getRegisteredAppVersion(){
		open();
		try{
			return this.getIntAppStorageValue(Adapter.APP_STORAGE_KEY_APP_VERSION);
		}
		catch (Exception e) {}
		finally { close(); }
		return 0;
	}
	
	public synchronized boolean was_appStorage_RegIdSaved(){
		open();
		try{
			return this.getBooleanAppStorageValue(Adapter.APP_STORAGE_KEY_GCM_REG_ID_SAVED);
		}
		catch(Exception e){}
		finally{close();}
		return false;
	}
	
	public synchronized void appStorage_RegIdChanged(){
		this.upsert_AppStorage_wasRegIdSaved(false);
	}
	
	public synchronized void appStorage_RegIdSaved(){
		this.upsert_AppStorage_wasRegIdSaved(true);
	}
	
	private void upsert_AppStorage_wasRegIdSaved(boolean wasSaved){
		open();
		String value = wasSaved ? "1" : "0";
		try{
			if (this.doesAppStorageKeyExist(Adapter.APP_STORAGE_KEY_GCM_REG_ID_SAVED)){
				this.updateAppStorageValue(Adapter.APP_STORAGE_KEY_GCM_REG_ID_SAVED, value);
			}
			else{
				this.insertAppStorageKeyValuePair(Adapter.APP_STORAGE_KEY_GCM_REG_ID_SAVED, value);
			}
		}
		catch(Exception e){}
		finally {close();}
	}
	
	//========APP STORAGE HELPER METHODS=================================================================================
	
	private synchronized boolean doesAppStorageKeyExist(String keyName){
		boolean retVal = false;
		Cursor cursor = null;
		try{
			cursor = database.query(
					Adapter.APP_STORAGE_TABLE, 
					appStorageColumns, 
					Adapter.APP_STORAGE_KEY_COLUMN+" = ?",
					new String[] {keyName},
					null, null, null);
			if (cursor.moveToFirst())
				retVal = true;
		}
		catch(Exception e){
		
		}
		finally{
			if (cursor!=null)
				cursor.close();
		}
		return retVal;
	}
	
	private synchronized boolean getBooleanAppStorageValue(String keyName){
		boolean retVal = false;
		Cursor cursor = null;
		try{
			cursor = database.query(
					Adapter.APP_STORAGE_TABLE, 
					appStorageColumns, 
					Adapter.APP_STORAGE_KEY_COLUMN+" = ?", 
					new String[]{keyName}, 
					null, null, null);
			if (cursor.moveToFirst())
				retVal = cursor.getInt(1) == 1;
		}
		catch (Exception e){
			
		}
		finally{
			if (cursor!=null)
				cursor.close();
		}
		return retVal;
	}
	
	private synchronized String getStringAppStorageValue(String keyName){
		Cursor cursor = null;
		try{
			cursor = database.query(
					Adapter.APP_STORAGE_TABLE, 
					appStorageColumns, 
					Adapter.APP_STORAGE_KEY_COLUMN+" = ?", 
					new String[]{keyName}, 
					null, null, null);
			if (cursor.moveToFirst()){
				return cursor.getString(1);
			}
		}
		catch (Exception e){
			
		}
		finally{
			if (cursor!=null)
				cursor.close();
		}
		return null;
	}
	
	private synchronized int getIntAppStorageValue(String keyName){
		Cursor cursor = null;
		try{
			cursor = database.query(
					Adapter.APP_STORAGE_TABLE, 
					appStorageColumns, 
					Adapter.APP_STORAGE_KEY_COLUMN+" = ?", 
					new String[]{keyName}, 
					null, null, null);
			if (cursor.moveToFirst())
				return cursor.getInt(1);
		}
		catch (Exception e){
			
		}
		finally{
			if (cursor!=null)
				cursor.close();
		}
		return 0;
	}
	
	private synchronized void insertAppStorageKey(String keyName){
		this.insertAppStorageKeyValuePair(keyName, "0");
	}
	
	private synchronized void insertAppStorageKeyValuePair(String keyName, String value){
		values.clear();
		values.put(Adapter.APP_STORAGE_KEY_COLUMN, keyName);
		values.put(Adapter.APP_STORAGE_VALUE_COLUMN, value);
		database.insert(Adapter.APP_STORAGE_TABLE, null, values);
	}
	
	private synchronized void updateAppStorageValue(String keyName, String value){
		values.clear();
		values.put(Adapter.APP_STORAGE_VALUE_COLUMN, value);
		int count = database.update(
				Adapter.APP_STORAGE_TABLE, values, 
				Adapter.APP_STORAGE_KEY_COLUMN +" = ?", 
				new String[] {keyName});
	}
	
	private String getTutorialKeyName(int resourceId){
		if (this.tutorials.get(resourceId)==null)
			throw new RuntimeException("tutorial resource id: " + resourceId + " is not mapped to an appStorageKey. Please add your resource in the TUTORIALS sparse array");
		return tutorials.get(resourceId);
	}
	
	//========END APP STORAGE HELPER METHODS=================================================================================
	
	//========TUTORIAL METHODS=================================================================================

	private SparseArray<String> tutorials;
	
	private void initTutorialsMap(){
		//NOTE!!!!!
		//NEVER, NEVER change the value (the stirng on the right of each key/value pair); 
		//the layout resource name can be changed without a problem
		tutorials = new SparseArray<String>();
		tutorials.put(R.layout.tutorial_swipe_dialog, "tutorial_swipe");
		tutorials.put(R.layout.tutorial_location_history_dialog, "tutorial_location_history");
		tutorials.put(R.layout.tutorial_welcome_dialog, "tutorial_welcome");
		tutorials.put(R.layout.tutorial_navigate_to_write_post_dialog, "tutorial_navigate_to_write_post");
		tutorials.put(R.layout.tutorial_click_post_to_see_location_dialog, "tutorial_click_post_to_see_location");
		tutorials.put(R.layout.tutorial_conversation_dialog, "tutorial_conversation");
		tutorials.put(R.layout.tutorial_explore_dialog, "tutorial_explore");
		tutorials.put(R.layout.tutorial_first_post_congrats_dialog, "tutorial_first_post_congrats");
		tutorials.put(R.layout.tutorial_how_to_write_a_post_dialog, "tutorial_how_to_write_a_post");
		tutorials.put(R.layout.tutorial_latest_dialog, "tutorial_latest");
		tutorials.put(R.layout.tutorial_nearby_dialog, "tutorial_nearby");
		tutorials.put(R.layout.tutorial_notifications_dialog, "tutorial_notifications");
		
		
	}
	
	public synchronized boolean wasTutorialRun(int resourceId){
		return this.wereTutorialsRun(Arrays.asList(resourceId));
	}
	
	public synchronized boolean wereTutorialsRun(List<Integer> tutorialDialogResources){
		boolean retVal = true;
		open();
		try{
			for(Integer i: tutorialDialogResources){
				retVal &= this.getBooleanAppStorageValue(getTutorialKeyName(i)); 
			}
		}
		catch(Exception e){
			
		}
		finally{
			close();
		}
		return retVal;
	}
	
	public synchronized void updateTutorialRun(int resourceId){
		open();
		String tutorialKeyName = getTutorialKeyName(resourceId);
		try{
			if (this.doesAppStorageKeyExist(tutorialKeyName)){
				this.updateAppStorageValue(tutorialKeyName, "1");
			}
			else{
				this.insertAppStorageKeyValuePair(tutorialKeyName, "1");
			}
		}
		catch(Exception e){
			
		}
		finally{
			close();
		}
	}
	
	//========END TUTORIAL METHODS=================================================================================	
	
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
