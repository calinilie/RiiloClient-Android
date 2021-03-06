package com.riilo.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Adapter extends SQLiteOpenHelper{

	private static final String DB_NAME="riilo5.db";
	private static final int DB_VERSION=1;

	public static final String POSTS_TABLE = "CamTestsPosts";
	public static final String POSTS_MESSAGE = "Message";
	public static final String POSTS_USER_AT_LOCATION = "IsUserAtLocation";
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
	public static final String POSTS_ALIAS = "Alias";
	public static final String POSTS_ACHIEVEMENT_ID = "AchievementId";
	
	private static final String CREATE_POSTS_TABLE = String.format(
									"CREATE TABLE %s (%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s REAL, %s REAL, %s INTEGER, %s REAL, %s TEXT, %s INTEGER)",
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
									POSTS_LOCATION_ACCURACY,
									POSTS_ALIAS,
									POSTS_ACHIEVEMENT_ID);
	
	public static final String APP_STORAGE_TABLE = "AppStorage";
	public static final String APP_STORAGE_KEY_COLUMN = "Key";
	public static final String APP_STORAGE_VALUE_COLUMN = "Value";
	
	public static final String APP_STORAGE_KEY_GCM_REG_ID = "GCMRegId";
	public static final String APP_STORAGE_KEY_GCM_REG_ID_SAVED = "GCMRegIdSaved";
	public static final String APP_STORAGE_KEY_APP_VERSION = "AppVersion";
	
	private static final String CREATE_APP_STORAGE_TABLE = String.format("CREATE TABLE %s (%s TEXT, %s TEXT)", APP_STORAGE_TABLE, APP_STORAGE_KEY_COLUMN, APP_STORAGE_VALUE_COLUMN);
	
	
	public static final String LOCATION_HISTORY_TABLE = "LocationHistory";
	public static final String LOCATION_HISTORY_DATE = "DateCreated";
	public static final String LOCATION_HISTORY_LATITUDE = "Latitutde";
	public static final String LOCATION_HISTORY_LONGITUDE = "Longitude";
	public static final String LOCATION_HISTORY_ACCURACY = "Accuracy";
	public static final String LOCATION_HISTORY_IS_SENT = "IsSent";
	
	private static final String CREATE_LOCATION_HISTORY_TABLE = String.format("CREATE TABLE %s (%s REAL, %s REAL, %s REAL, %s TEXT, %s INTEGER)",
									LOCATION_HISTORY_TABLE,
									LOCATION_HISTORY_LATITUDE,
									LOCATION_HISTORY_LONGITUDE,
									LOCATION_HISTORY_ACCURACY,
									LOCATION_HISTORY_DATE,
									LOCATION_HISTORY_IS_SENT);
	
	public static final String OUTSIDE_LOCATION_HISTORY_TABLE = "OutsideLocationHistory";
	public static final String OUTSIDE_LOCATION_HISTORY_ID = "LocationHistoryId";
	public static final String OUTSIDE_LOCATION_HISTORY_LATITUDE = "Latitude";
	public static final String OUTSIDE_LOCATION_HISTORY_LONGITUDE = "Longitude";
	public static final String OUTSIDE_LOCATION_HISTORY_DATE = "CreatedDate";
	
	private static final String CREATE_OUTSIDE_LOCATION_HISTORY_TABLE = String.format("CREATE TABLE %s (%s INTEGER, %s REAL, %s REAL, %s TEXT)",
								OUTSIDE_LOCATION_HISTORY_TABLE,
								OUTSIDE_LOCATION_HISTORY_ID,
								OUTSIDE_LOCATION_HISTORY_LATITUDE,
								OUTSIDE_LOCATION_HISTORY_LONGITUDE,
								OUTSIDE_LOCATION_HISTORY_DATE);
	
	//=============V2================================================================================================================================
	public static final  String APP_STORAGE_KEY_TUTORIAL_SWIPE_RUN = "TutorialSwipeRun";
	/*private static final String INSERT_APPSTORAGE_SWIPTE_TUTORIAL_RUN = String.format("INSERT INTO %s (%s, %s) VALUES ('%s', %s)", 
																		APP_STORAGE_TABLE,
																		APP_STORAGE_KEY_COLUMN,
																		APP_STORAGE_VALUE_COLUMN,
																		APP_STORAGE_KEY_TUTORIAL_SWIPE_RUN,
																		0);*/
//	private final String[] updateQueriesV2 = new String[]{ INSERT_APPSTORAGE_SWIPTE_TUTORIAL_RUN };
	
	//create queries ================================================================================================================================
	private final String[] createQueries = new String[] { CREATE_POSTS_TABLE, CREATE_LOCATION_HISTORY_TABLE, CREATE_APP_STORAGE_TABLE };
	
	
	public Adapter(Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
//		try{
			runSqlQueries(db, createQueries);
//		}
//		catch (Exception e){
			//Log.d("sqlite_db", e.getMessage());
//		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*if (newVersion == 2 && oldVersion == 1){
			runSqlQueries(db, updateQueriesV2);
		}*/
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
			//Log.d("sqlite_db", queries.length +" "+ s);
			db.execSQL(s);
		}
	}

}
