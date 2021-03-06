package com.riilo.main;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import android.content.Context;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

public class Helpers {
	
	private static final String TAG = "HELPERS";
	
	/**
	 * adds all items (that are not existent in source) into target, and removes all items from source that are in target
	 * @param target
	 * @param source
	 * @return  list of items that are already in target and not in source
	 */
	/*public static Collection<Post> mergeLists(Collection<Post> target, Collection<Post> source){
		List<Post> postsToRemove = new ArrayList<Post>();//posts from conversation already present, only one post per conversation in List
		if (source!=null){
			for (Post p:source){
				if (target.contains(p)){
					postsToRemove.add(p);
				}
				else{
					target.add(p);
				}
	    	}
			source.removeAll(postsToRemove);
		}
		return postsToRemove;
	}*/
	
	public static Collection<Post> mergeLists(Collection<Post> target, Collection<Post> source){
		List<Post> postsToRemove = new ArrayList<Post>();//posts from conversation already present, only one post per conversation in List
		if (source!=null && target!=null){
			for (Post p:target){
				if (source.contains(p)){
					postsToRemove.add(p);
				}
				else{
					target.add(p);
				}
	    	}
			source.removeAll(postsToRemove);
		}
		return postsToRemove;
	}
	
	
	
	/*====================MAP=====================================================================================*/
	public static void addMarkersToMap(List<LocationHistory> histories, ClusterManager<LocationHistory> clusterManager){
		for(LocationHistory h : histories){
			if (!h.isOnMap()){
//				map.addMarker(new MarkerOptions().position(h.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_human)));
				if (h.getPosition()!=null){
					clusterManager.addItem(h);
				}
				h.setIsOnMap(true);
			}
		}
		clusterManager.cluster();
	}
	
	public static synchronized void addMarkersToMap(List<LocationHistory> histories, GoogleMap map){
		for(LocationHistory h : histories){
			if (!h.isOnMap()){
				map.addMarker(new MarkerOptions().position(h.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_history)));
				h.setIsOnMap(true);
			}
		}
	}
	
	public static synchronized void addPostsToMap(Collection<Post> posts, GoogleMap map){
		for(Post p : posts){
//			if (!h.isOnMap()){
				//TODO create position property in Post type - just like LocationHistory
				p.setMarker(map.addMarker(new MarkerOptions().position(new LatLng(p.getLatitude(), p.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_history))));
//				h.setIsOnMap(true);
//			}
		}
	}
	
	/*===================DISTANCES======================================================================================*/
	public static double distanceFrom(double lat1, double lng1, double lat2, double lng2) {
		return distanceFrom(lat1, lng1, lat2, lng2, false);
	}
	
	public static double distanceInKmFrom(double lat1, double lng1, double lat2, double lng2) {
		return distanceFrom(lat1, lng1, lat2, lng2, true);
	}
	
	private static double distanceFrom(double lat1, double lng1, double lat2, double lng2, boolean ignoreUserPref) {
		double earthRadius = 6371;//in km 
		if (inMiles() && (!ignoreUserPref)){
			earthRadius = 3959;
		}
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	    
	    return Double.valueOf(dist).doubleValue();
	}
	
	public static boolean inMiles(){
		if (Locale.getDefault().getCountry().equalsIgnoreCase("GB") || Locale.getDefault().getCountry().equalsIgnoreCase("US"))
			return true;
		return false;
	}
	
	/*===================DATES======================================================================================*/
	public static String dateToString(Date date){
    	DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
    	df.setTimeZone(TimeZone.getDefault());
    	if (date == null) return "date is null!";
    	return df.format(date);
    }
    
    public static Date stringToDate(String string){
    	try {
    		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
    		df.setTimeZone(TimeZone.getTimeZone("UTC"));
			return df.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
	/*=============LIST SPECIFIC HELPERS============================================================================================*/
    public static boolean hasPostFromConversation(List<Post> targetList, Post post){
    	boolean found = false;
    	for(Post p : targetList){
    		if (p.getConversationId()==post.getConversationId()){
    			found = true;
    			break;
    		}
    	}
    	return found;
    }
    
    /**
     * adds all items from source into target. Note, only items that are NOT in target already will be added. Sorts desc by ID by default
     * @param target
     * @param source
     * @return true if new items were added to the target
     */
    public static boolean renewList(List<Post> target, List<Post> source){
    	return renewList(target, source, true);
    }
    
    /**
     * adds all items from source into target. Note, only items that are NOT in target already will be added. 
     * @param target
     * @param source
     * @param desc boolean specifying whether to sort desc by ID (same as desc chronological order - so far) 
     * @return true if new items were added to the target
     */
    public static boolean renewList(List<Post> target, List<Post> source, boolean desc){
    	boolean newItemsAdded = false;
    	if (source!=null && target!=null){
	    	for(Post p: source){
	    		if (!target.contains(p)){
	    			target.add(p);
	    			newItemsAdded = true;
	    		}
	    	}
	    	if (desc){
	    		Collections.sort(target, Collections.reverseOrder());
	    	}
	    	else{
	    		Collections.sort(target, Post.PostIdComparator);
	    	}
    	}
    	return newItemsAdded;
    }
    
	public static double[] setReqFrom_Latitude_and_Longitude(Location location, LocationHistory lastKnownLocation){
		double[] retVal = new double[2];
		double reqFromLatitude = 0;
		double reqFromLongitude = 0;
		if (location!=null){
			reqFromLatitude = location.getLatitude();
			reqFromLongitude = location.getLongitude();
		}
		else if (lastKnownLocation!=null){
			reqFromLatitude = lastKnownLocation.getLatitude();
			reqFromLongitude = lastKnownLocation.getLongitude();
		}
		retVal[0] = reqFromLatitude;
		retVal[1] = reqFromLongitude;
		
		return retVal;
	}

	
	/*=====PHOTO CAMERA HELPERS - NOT USED====================================================================================================*/
    public static String uriToFilePath(Uri uri){
    	return uriToFilePath(uri.toString());
    }
    
    public static String uriToFilePath(String uriAsString){
    	int index = uriAsString.indexOf("//")+1;
    	if (index!=-1)
    		return uriAsString.substring(index);
    	return uriAsString;
    }
    
	public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
	     int rotate = 0;
	     try {
	         context.getContentResolver().notifyChange(imageUri, null);
	         File imageFile = new File(imagePath);
	         ExifInterface exif = new ExifInterface(
	                 imageFile.getAbsolutePath());
	         int orientation = exif.getAttributeInt(
	                 ExifInterface.TAG_ORIENTATION,
	                 ExifInterface.ORIENTATION_NORMAL);

	         switch (orientation) {
	         case ExifInterface.ORIENTATION_ROTATE_270:
	             rotate = 270;
	             break;
	         case ExifInterface.ORIENTATION_ROTATE_180:
	             rotate = 180;
	             break;
	         case ExifInterface.ORIENTATION_ROTATE_90:
	             rotate = 90;
	             break;
	         }
	         
	     } catch (Exception e) {
	         e.printStackTrace();
	     }
	    return rotate;
	 }
	
	
}
