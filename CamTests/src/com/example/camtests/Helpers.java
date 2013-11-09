package com.example.camtests;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

public class Helpers {
	
	/*=========================================================================================================*/
	
	
	/*===================DISTANCES======================================================================================*/
	public static double distanceFrom(double lat1, double lng1, double lat2, double lng2, boolean inMiles) {
		double earthRadius = 6371;//in km 
		if (inMiles()){
			earthRadius = 3959;
		}
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	    double mileConversion = 0.621371192;
	    if (inMiles)
    		return Double.valueOf(dist * mileConversion).doubleValue();
	    else
	    	return Double.valueOf(dist).doubleValue();
	}
	
	public static boolean inMiles(){
		if (Locale.getDefault().getCountry().equals("GB") || Locale.getDefault().getCountry().equals("US"))
			return true;
//		return false;//TODO
		return true;
	}
	
	
	/*===================DATES======================================================================================*/
	public static String dateToString(Date date){
    	DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
    	Date dateInLocalTimeZone = shiftTimeZone(date, TimeZone.getDefault());
    	return df.format(dateInLocalTimeZone);
    }
    
    public static Date stringToDate(String string){
    	try {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return null;
    }

    public static Date shiftTimeZone(Date date, TimeZone targetTimeZone){
    	return shiftTimeZone(date, TimeZone.getTimeZone("UTC"), targetTimeZone);
    }
    
    public static Date shiftTimeZone(Date date, TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        Calendar sourceCalendar = Calendar.getInstance();
        sourceCalendar.setTime(date);
        sourceCalendar.setTimeZone(sourceTimeZone);

        Calendar targetCalendar = Calendar.getInstance();
        for (int field : new int[] {Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND}) {
            targetCalendar.set(field, sourceCalendar.get(field));
        }
        targetCalendar.setTimeZone(targetTimeZone);

        return targetCalendar.getTime();
    }
    
	/*=========================================================================================================*/
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
    
    public static boolean renewList(List<Post> target, List<Post> source){
    	return renewList(target, source, true);
    }
    
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
	    		Collections.sort(target);
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
		else{
			reqFromLatitude = lastKnownLocation.getLatitude();
			reqFromLongitude = lastKnownLocation.getLongitude();
		}
		retVal[0] = reqFromLatitude;
		retVal[1] = reqFromLongitude;
		
		return retVal;
	}
    
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
	         
	         Log.v(">>>>>>>>>>>>>Orient<<<<<<<<<<<<<", "Exif orientation: " + orientation);
	     } catch (Exception e) {
	         e.printStackTrace();
	     }
	    return rotate;
	 }
	
	
}
