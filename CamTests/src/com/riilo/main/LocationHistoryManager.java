package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;

public class LocationHistoryManager {

	
	private Facade facade;
	private Context context;
	private List<LocationHistory> list;
	private boolean wasRequestMade = false;
	
	private static LocationHistoryManager instance;
	private LocationHistoryManager(Context context){
		facade = Facade.getInstance(context);
		this.context = context;
		this.list = new ArrayList<LocationHistory>();
		wasRequestMade = false;
	}
	
	public static LocationHistoryManager getInstance(Context context){
		if (instance==null)
			instance = new LocationHistoryManager(context);
		return instance;
	}
	
	public List<LocationHistory> getLocationHistory(){
		if (list.isEmpty())
			list = facade.getLocationHistory(); 
		return list;
	}
	
	public void startService(){
		if (!wasRequestMade){
	    	Intent intent = new Intent(context, WorkerService.class);
	    	intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_LOCATION_HISTORY);
	    	context.startService(intent);
		}
	}
	
}