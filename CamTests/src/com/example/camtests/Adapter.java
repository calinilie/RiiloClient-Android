package com.example.camtests;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Adapter extends SQLiteOpenHelper{

	private static final String DB_NAME="Prototypes39.db";
	private static final int DB_VERSION=1;
	
	public static final String PICTURES_TABLE="CamTestsPictures";
	//======================V5=======================================
	public static final String POSTS_TABLE = "CamTestsPosts";
	public static final String POSTS_MESSAGE = "Message";
	//======================V6=======================================
	public static final String POSTS_USER_AT_LOCATION = "IsUserAtLocation";
	//=============================================================
	public static final String POSTS_URI="Uri";
	public static final String POSTS_DATE_CREATED="DateCreated";
	public static final String POSTS_LONGITUDE="Longitude";
	public static final String POSTS_LATITUDE="Latitude";
	public static final String POSTS_LOCATION_ACCURACY="Accuracy";
	public static final String POSTS_POST_ID = "PostId";
	public static final String POSTS_REPLYTO_POST_ID = "ReplyToPostId";
	public static final String POSTS_CONVERSATION_ID = "ConversationId";
	public static final String POSTS_INADDITIONTO_POST_ID = "InAdditionToPostId";
	public static final String POSTS_USER_ID = "UserId";
	
	private static final String CREATE_POSTS_TABLE = String.format(
									"CREATE TABLE %s (%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s REAL, %s REAL, %s INTEGER, %s REAL)",
									POSTS_TABLE,
									POSTS_POST_ID,
									POSTS_REPLYTO_POST_ID,
									POSTS_CONVERSATION_ID,
									POSTS_INADDITIONTO_POST_ID,
									POSTS_USER_ID,
									POSTS_URI,
									POSTS_MESSAGE,
									POSTS_DATE_CREATED,
									POSTS_LONGITUDE,
									POSTS_LATITUDE,
									POSTS_USER_AT_LOCATION,
									POSTS_LOCATION_ACCURACY);
	
	public static final String APP_STORAGE_TABLE = "AppStorage";
	public static final String APP_STORAGE_KEY_COLUMN = "Key";
	public static final String APP_STORAGE_KEY_TUTORIAL_RUN = "TutorialRun";
	public static final String APP_STORAGE_VALUE_COLUMN = "Value";
	
	private static final String CREATE_APP_STORAGE_TABLE = String.format("CREATE TABLE %s (%s TEXT, %s TEXT)", APP_STORAGE_TABLE, APP_STORAGE_KEY_COLUMN, APP_STORAGE_VALUE_COLUMN);
	private static final String INSERT_APPSTORAGE_TUTORIAL_RUN = String.format("INSERT INTO %s (%s, %s) VALUES('%s', %s)", 
																	APP_STORAGE_TABLE, 
																	APP_STORAGE_KEY_COLUMN, 
																	APP_STORAGE_VALUE_COLUMN, 
																	APP_STORAGE_KEY_TUTORIAL_RUN, 
																	0);
	
	public static final String LOCATION_HISTORY_TABLE = "LocationHistory";
	public static final String LOCATION_HISTORY_DATE = "DateCreated";
	public static final String LOCATION_HISTORY_LATITUDE = "Latitutde";
	public static final String LOCATION_HISTORY_LONGITUDE = "Longitude";
	public static final String LOCATION_HISTORY_ACCURACY = "Accuracy";
	
	private static final String CREATE_LOCATION_HISTORY_TABLE = String.format("CREATE TABLE %s (%s REAL, %s REAL, %s REAL, %s TEXT)",
									LOCATION_HISTORY_TABLE,
									LOCATION_HISTORY_LATITUDE,
									LOCATION_HISTORY_LONGITUDE,
									LOCATION_HISTORY_ACCURACY,
									LOCATION_HISTORY_DATE);
	
	//======================V5=======================================
	private static final String ALTER_PICTURES_TO_POSTS_TABLE = String.format("ALTER TABLE %s RENAME TO %s", PICTURES_TABLE, POSTS_TABLE);
	private static final String ALTER_POSTS_ADD_MESSAGE_COLUMN = String.format("ALTER TABLE %s ADD COLUMN %s TEXT", POSTS_TABLE, POSTS_MESSAGE);
	//======================V6=======================================
	public static final String ALTER_POSTS_ADD_USER_AT_LOCATION_COLUMN = String.format("ALTER TABLE %s ADD COLUMN %s INTEGER", POSTS_TABLE, POSTS_USER_AT_LOCATION);
	//======================V7=======================================
	public static final String UPDATE_POST_USER_AT_LOCATION = String.format("UPDATE %s SET %s=1", POSTS_TABLE, POSTS_USER_AT_LOCATION);
	//===============================================================
	private final String[] createQueries = new String[] { CREATE_POSTS_TABLE, CREATE_LOCATION_HISTORY_TABLE, CREATE_APP_STORAGE_TABLE, INSERT_APPSTORAGE_TUTORIAL_RUN };
//	private final String[] queiresV5 = new String[] {ALTER_PICTURES_TO_POSTS_TABLE, ALTER_POSTS_ADD_MESSAGE_COLUMN};
//	private final String[] queriesV6 = new String[] {ALTER_POSTS_ADD_USER_AT_LOCATION_COLUMN};
//	private final String[] queriesV7 = new String[] {UPDATE_POST_USER_AT_LOCATION};
	
	public Adapter(Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("sqlite_db","on create called");
		try{
			runSqlQueries(db, createQueries);
		}
		catch (Exception e){
			Log.d("sqlite_db", e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", "called onUpgrade");
		/*if (newVersion == 5 && oldVersion==4){
			runSqlQueries(db, queiresV5);
		}
		if (newVersion==6){
			if (oldVersion==5)
				runSqlQueries(db, queriesV6);
			if (oldVersion==4){
				runSqlQueries(db, queiresV5);
				runSqlQueries(db, queriesV6);
			}
		}
		if (newVersion == 7){
			if (oldVersion== 6){
				runSqlQueries(db, queriesV7);
			}
			if (oldVersion==5){
				runSqlQueries(db, queriesV6);
				runSqlQueries(db, queriesV7);
			}
			if (oldVersion==4){
				runSqlQueries(db, queiresV5);
				runSqlQueries(db, queriesV6);
				runSqlQueries(db, queriesV7);
			}
		}
		
		
//		db.execSQL("DROP TABLE IF EXISTS "+Adapter.PICTURES_TABLE);
//		onCreate(db);*/
	}
	
	private void runSqlQueries(SQLiteDatabase db, String[] queries){
		for (String s:queries){
			Log.d("sqlite_db", queries.length +" "+ s);
			db.execSQL(s);
		}
	}

}
