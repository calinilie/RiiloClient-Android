package com.riilo.main;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class LocationHistory implements Serializable, ClusterItem{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Date date;
	private long locationHistoryId;
	private double latitude;
	private double longitude;
	private float accuracy;
	private String userId;
	private boolean isSent;
	private boolean isOnMap;
	private LatLng position;
	
	public LocationHistory(){
		
	}
	
	public LocationHistory(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
		this.position = new LatLng(this.latitude, this.longitude);
	}
	
	public LocationHistory(JSONObject json) throws JSONException{
		this.locationHistoryId = json.getInt("locationHistoryId");
		this.latitude = json.getDouble("latitude");
		this.longitude = json.getDouble("longitude");
		this.position = new LatLng(this.latitude, this.longitude);
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
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
	public long getLocationHistoryId() {
		return locationHistoryId;
	}
	public void setLocationHistoryId(long locationHistoryId) {
		this.locationHistoryId = locationHistoryId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public boolean isSent() {
		return isSent;
	}
	public void setSent(boolean isSent) {
		this.isSent = isSent;
	}
	public LatLng getLatLng(){
		return new LatLng(latitude, longitude);
	}
	public boolean isOnMap() {
		return isOnMap;
	}
	public void setIsOnMap(boolean isOnMap) {
		this.isOnMap = isOnMap;
	}

	public JSONObject toJson() throws JSONException{
		JSONObject retVal = new JSONObject();
		retVal.put("userId", this.userId);
		retVal.put("latitude", this.latitude);
		retVal.put("longitude", this.longitude);
		return retVal;
	}
	
	@Override
	public boolean equals(Object another){
		if (another == null){
			return false;
		}
		if (!(another instanceof LocationHistory)){
			return false;
		}
		
		return this.locationHistoryId == ((LocationHistory)another).getLocationHistoryId();
	}
	
	@Override
	public int hashCode() {
		long hash = 1;
		hash = hash * 17 + locationHistoryId;
		return (hash+"").hashCode();
	}
	
	@Override
	public String toString(){
		return String.format("%s %s %s %s",locationHistoryId+"", userId, latitude+"", longitude+"");
	}

	@Override
	public LatLng getPosition() {
		return this.position;
	}
	
	
}
