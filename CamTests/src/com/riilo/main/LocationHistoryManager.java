package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LocationHistoryManager {

	private static final String TAG = "LocationHistoryManager";
	
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
	
	public List<LocationHistory> getLocationHistory(ClusterManager<LocationHistory> mapClusterManager, GoogleMap map){
		if (list.isEmpty())
			list = facade.getLocationHistory(); 
		
		startService(mapClusterManager, map);
		
		return list;
	}
	
	public List<LocationHistory> getLocationHistory(){
		if (list.isEmpty())
			list = facade.getLocationHistory(); 
		return list;
	}
	
	public void getRemoteLocationHistory(ClusterManager<LocationHistory> mapClusterManager, GoogleMap map){
		startService(mapClusterManager, map);
	}
	
	private void startService(ClusterManager<LocationHistory> mapClusterManager, GoogleMap map){
		if (!wasRequestMade){
	    	Intent intent = new Intent(context, WorkerService.class);
	    	intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_GET_LOCATION_HISTORY);
	    	LocationHistoryResultReceiver resultReceiver = new LocationHistoryResultReceiver(new Handler());
	    	resultReceiver.setMap(map);
	    	resultReceiver.setClusterManager(mapClusterManager);
	    	intent.putExtra(StringKeys.LOCATION_HISTORY_RESULT_RECEIVER, resultReceiver);
	    	context.startService(intent);
	    	wasRequestMade = true;
		}
	}
	
	public synchronized void mergeLocationhistories(List<LocationHistory> locations){
		for(LocationHistory h : locations){
			if (!this.list.contains(h)){
				this.list.add(h);
			}
		}
	}
	
	public synchronized void addLocationHistory(LocationHistory h){
		if (!list.contains(h))
			list.add(h);
	}
	
	public synchronized void locationHistoryMarkersRemoved(){
		for(LocationHistory h:this.list)
			h.setIsOnMap(false);
	}
	
}