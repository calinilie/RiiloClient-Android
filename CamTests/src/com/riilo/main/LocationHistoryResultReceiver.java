package com.riilo.main;

import java.util.List;

import com.google.android.gms.maps.GoogleMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class LocationHistoryResultReceiver extends ResultReceiver{

	private static final String TAG = "LocationHistoryResultReceiver";
	private Handler handler;
	
	private GoogleMap map;
	public void setMap(GoogleMap map){
		this.map = map;
	}
	
	public LocationHistoryResultReceiver(Handler handler) {
		super(handler);
		this.handler = handler;
	}
	
	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData){
		LocationHistoryParcelable locationHistoryParcelable = resultData.getParcelable(StringKeys.LOCATION_HISTORY_PARCELABLE);
		if (locationHistoryParcelable!=null){
			final List<LocationHistory> list = locationHistoryParcelable.getLocationHistory();
			if (list!=null && !list.isEmpty()){
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						if (map!=null){
							Helpers.addMarkersToMap(list, map);
						}
					}
				});
			}
		}
	}
	
	
}
