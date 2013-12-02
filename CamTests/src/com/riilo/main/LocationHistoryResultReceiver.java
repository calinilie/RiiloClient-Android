package com.riilo.main;

import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

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
		Log.d(TAG, "onReceiveResult");
		if (locationHistoryParcelable!=null){
			final List<LocationHistory> list = locationHistoryParcelable.getLocationHistory();
			Log.d(TAG, "locationHistoryParcelable!=null" + list.size());
			if (list!=null && !list.isEmpty()){
				Log.d(TAG, list.size()+"");
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						if (map!=null){
//							for(LocationHistory h:list){
//								map.addMarker(new MarkerOptions().position(h.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plusone_standard_off_client)));
//							}
							Helpers.addMarkersToMap(list, map);
						}
						
					}
				});
			}
		}
	}
	
	
}
