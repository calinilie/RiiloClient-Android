package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import com.riilo.main.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.riilo.interfaces.ILocationListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public abstract class BaseActivity extends FragmentActivity
		implements 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		LocationListener{

    /*protected String manufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.ENGLISH);
    protected String model = android.os.Build.MODEL.toLowerCase(Locale.ENGLISH);*/
    protected String deviceId="";
    
    private static final String TAG = "BASE ACTIVITY";
    
    /*private static String fileName;
    private static Uri cameraPicUri = null;
    private static Date dateCameraIntentStarted = null;*/
    
    protected Bundle savedInstanceState;
    
    
    private LocationRequest locationRequest;
    protected LocationClient locationClient;
    protected Location location;
    
    private List<ILocationListener> locationListeners;
    
    protected PostsCache postsCache;
    protected AnalyticsWrapper analytics;
    
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.savedInstanceState = savedInstanceState;     
        deviceId = Secure.getString(this.getContentResolver(),
                Secure.ANDROID_ID);        
        
        /*if (savedInstanceState!=null)
        	fileName = savedInstanceState.getString(StringKeys.TAKE_PICTURE_FILE_NAME_AND_PATH);*/
        
        postsCache = PostsCache.getInstance(this);
        analytics = AnalyticsWrapper.getInstance(this);
    }
	
	@Override
	public void onStart(){
		super.onStart();
		connectLocationClient();
		analytics.startTracker(this);
	}
	
	@Override
	protected void onStop(){
		disconnectLocationClient();
		analytics.stopTracker(this);
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		Intent intent = new Intent(this, WorkerService.class);
		intent.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_INSERT_LOCATION_HISTORY);
		startService(intent);
		super.onDestroy();
	}
	
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        /*if (fileName!=null){
	        if (!fileName.isEmpty()){
	        	savedInstanceState.putString(StringKeys.TAKE_PICTURE_FILE_NAME_AND_PATH, fileName);
	        }
        }
        
        if (dateCameraIntentStarted != null) {
            savedInstanceState.putString(StringKeys.TAKE_PICTURE_INTENT_START_DATETIME, Helpers.dateToString(dateCameraIntentStarted));
        }
        
        if (cameraPicUri != null) {
            savedInstanceState.putString(StringKeys.TAKE_PICTURE_PIC_URI, cameraPicUri.toString());
        }*/
    }
	
    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        /*if (savedInstanceState.containsKey(StringKeys.TAKE_PICTURE_INTENT_START_DATETIME)) {
            dateCameraIntentStarted = Helpers.stringToDate(savedInstanceState.getString(StringKeys.TAKE_PICTURE_INTENT_START_DATETIME));
        }
        if (savedInstanceState.containsKey(StringKeys.TAKE_PICTURE_PIC_URI)) {
            cameraPicUri = Uri.parse(savedInstanceState.getString(StringKeys.TAKE_PICTURE_PIC_URI));
        }*/
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode){
	        case (StringKeys.CONNECTION_FAILURE_RESOLUTION_REQUEST):
	        	if (resultCode == Activity.RESULT_OK){
	        		connectLocationClient();
	        	}
	        	else{
	        		showWarningDialog(getString(R.string.error_cannot_connect_to_location_services));
	        		//TODO exception handling in analytics
	        	}
	        	break;
        }
    }
    
    //====================================Take photo============================================== 
	/*protected void startCameraIntent() {
	    try {
	        dateCameraIntentStarted = new Date();
	        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        //NOTE: Do NOT SET: intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPicUri) on Samsung Galaxy S2/S3/.. for the following reasons:
	        // 1.) it will break the correct picture orientation
	        // 2.) the photo will be stored in two locations (the given path and additionally in the MediaStore)
	
	        String envPath = Environment.getExternalStorageDirectory().getPath();
	        if (!envPath.endsWith("/")){
	        	envPath+="/";
	        }
	        envPath += "Prototype/";
	        File directory = new File(envPath);
	        if (!directory.exists())
	        	directory.mkdir();
	        File file = new File(envPath+"Camtests_"+System.currentTimeMillis()+".jpg");
	        
	        fileName = file.getAbsolutePath();
	        Uri fileUri = Uri.fromFile(file);
	//	            if(!(manufacturer.contains("samsung")) && !(manufacturer.contains("sony")) && !(manufacturer.contains("htc") ) ) {
	//	                ContentValues values = new ContentValues();
	//	                values.put(MediaStore.Images.Media.TITLE, fileUri.toString());
	//	                cameraPicUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	//	                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPicUri);
	//	            }
	//	            if (manufacturer.contains("samsung") && model.contains("nexus")){
	//	                 intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	//	            }
		            //Log.d("#########################", fileName);
	        if( !manufacturer.contains("samsung") && !manufacturer.contains("google") )
	        	intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	        else if (model.contains("nexus")){
	        	intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	        }
	        
	        startActivityForResult(intent, StringKeys.TAKE_PICTURE_REQUEST_CODE);
	     } catch (ActivityNotFoundException e) {
	         showWarningDialog(getString(R.string.error_could_not_take_photo));
	     }      
	}*/
	 
	/*protected Uri onTakePhotoActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (cameraPicUri == null){
            	File f = new File(fileName);
            	if (f.exists()){
	            	cameraPicUri = Uri.parse(fileName);
	            	//Log.d("<<<<<<<<#####<<<<<<<<<", "From Activity Field: " + fileName);
            	}
            }
        	
        	if (cameraPicUri == null){
	            Cursor myCursor = null;
	            Date dateOfPicture = null;
	            try {
	                // Create a Cursor to obtain the file Path for the large image
	                String[] largeFileProjection = {MediaStore.Images.ImageColumns._ID,
	                                                MediaStore.Images.ImageColumns.DATA,
	                                                MediaStore.Images.ImageColumns.ORIENTATION,
	                                                MediaStore.Images.ImageColumns.DATE_TAKEN};
	                String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
	                myCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                                                    largeFileProjection, null, null, largeFileSort);
	                myCursor.moveToFirst();
	                // This will actually give you the file path location of the image.
	                String largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
	                Uri tempCameraPicUri = Uri.fromFile(new File(largeImagePath));
	                //Log.d("<<<<<<<<#####<<<<<<<<<", "From Media Store" + largeImagePath);
	                if (tempCameraPicUri != null) {
	                    dateOfPicture = new Date(myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
	                    if (dateOfPicture != null && dateOfPicture.after(dateCameraIntentStarted)) {
	                        cameraPicUri = tempCameraPicUri;
	                    }
	                }
	            } catch (Exception e) {
	              Log.w("TAG", "Exception - optaining the picture's uri failed: " + e.toString());
	            } finally {
	                if (myCursor != null) {
	                    myCursor.close();
	                }
	            }
        	}
            
            if (cameraPicUri == null) {
                try {
                    cameraPicUri = intent.getData();
                    //Log.d("<<<<<<<<#####<<<<<<<<<", "From Intent.getData()" + cameraPicUri.toString());
                } catch (Exception e){                  
                    showWarningDialog(getString(R.string.error_could_not_take_photo));
                }
            }
            
            if (cameraPicUri!=null){
            	File file = new File(Helpers.uriToFilePath(cameraPicUri));
            	if (!file.exists()){
            		showWarningDialog(getString(R.string.error_could_not_take_photo));
            		//TODO exception handling in analytics
            	}
            	Uri retval = Uri.parse(cameraPicUri.toString());
            	cameraPicUri = null;
            	return retval;//BitmapHelper.resizeAndSaveImage(this, cameraPicUri);
            	
//            	if (add){
//	            	Post post= new Post();
//	            	post.setUri(cameraPicUri.toString());
//	            	post.setDateCreated(Calendar.getInstance().getTime());
//	            	post.setLatitude(mCurrentLocation.getLatitude());
//	            	post.setLongitude(mCurrentLocation.getLongitude());
//	            	post.setAccuracy(mCurrentLocation.getAccuracy());
//	            	post.setUserAtLocation(true);
//	            	
//            		Intent intentPost= new Intent(this, WorkerService.class);
//            		intentPost.putExtra(StringKeys.WORKER_SERVICE_INTENT_TYPE, StringKeys.WS_INTENT_POST);
//            		intentPost.putExtra(StringKeys.POST_INTENT_BUNDLE, post.toBundle());
//	            	startService(intentPost);
//            }
//            else{
//            	showWarningDialog(getString(R.string.error_could_not_take_photo));
//            }
              
        } else if (resultCode == RESULT_CANCELED) {
        	//on HTC One X, if user cancels activity, the condition above is not satisfied !!!!
        	//Log.d("onTakePhotoActivityResult<<<<<<<<<<<<<<<", "RESULT_CANCELED");
        }
        } else {
//        	showWarningDialog(getString(R.string.error_could_not_take_photo));
    		//TODO exception handling in analytics
        }
//        showWarningDialog(getString(R.string.error_could_not_take_photo));
		//TODO exception handling in analytics
        //Log.d("onTakePhotoActivityResult<<<<<<<<<<<<<<<", "SHOULD Have showWarningDialog");
        return null;
    }*/
	
	//===================================location and play services===============================
	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
		if (locationListeners!=null && locationListeners.size()>0){
			for(ILocationListener listener: locationListeners){
				listener.onLocationChanged(location);
			}
		}
		LocationHistory lastKnownLocation = Facade.getInstance(this).getLastKnownLocation();
		Facade.getInstance(this).insertLocationToHistoryIfNeeded(location, lastKnownLocation);
		//TODO change logic in method above
	}
	
	public void addLocationListener(ILocationListener locationListener){
		if (locationListeners==null){
			locationListeners = new ArrayList<ILocationListener>();
		}
		locationListeners.add(locationListener);
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        StringKeys.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult);
        }
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		if (locationClient==null)
			initLocationClient(LocationRequest.PRIORITY_LOW_POWER, 2000, 1000);
		
		try{
			locationClient.requestLocationUpdates(locationRequest, this);
			location = locationClient.getLastLocation();
		}
		catch(Exception e){}
	}
	
	@Override
	public void onDisconnected() {

        Toast.makeText(this, "Location Services disconnected, trying to connect...",
                Toast.LENGTH_SHORT).show();
		
	}

	public Location getLocation() {
		return location;
	}

	//===================================helper/utility methods and classes=======================
	protected abstract void setupWidgetsViewElements();
	
	/**
	 * used by some startCameraIntent and onTakePhotoActivityResult
	 * displays an error message and kills app. 
	 * TODO consider using error dialog and handle exceptions more gracefully!
	 * @param message
	 */
 	protected void showWarningDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.warning_dialog_heading));
        alertDialogBuilder
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    cancelActivity();
                }
              });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();     
    }
 	
 	protected void showInfoDialog(String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.info_dialog_heading));
        alertDialogBuilder
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.tutorial_ok),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
              });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();   
 	}
    
	private void showErrorDialog(ConnectionResult connectionResult){
		// Get the error code
        int errorCode = connectionResult.getErrorCode();
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                StringKeys.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment =
                    new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(),
                    "Location Updates");
        }
	}
	
	/**
	 * used when google play services fail to connect
	 * disaplys resolution (download/install/update google play services)
	 * @author calin
	 *
	 */
	public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

	protected void initLocationClient(int priority, long updateInterval, long fastedUpdateInterval){
		locationClient = new LocationClient(this, this, this);
        // Create the LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(priority);
        // Set the update interval to 1 seconds
        locationRequest.setInterval(updateInterval);
        // Set the fastest update interval to 0.5 second
        locationRequest.setFastestInterval(fastedUpdateInterval);
	}
	
	//===================================private helper methods==================================
	private void cancelActivity() {
		this.finish();
    }
	
	private void connectLocationClient(){
		if (locationClient!=null){
			locationClient.connect();
		}
	}
	
	private void disconnectLocationClient(){
		if (locationClient!=null){
			// If the client is connected
	        if (locationClient.isConnected()) {
	            /*
	             * Remove location updates for a listener.
	             * The current Activity is the listener, so
	             * the argument is "this".
	             */
	        	locationClient.removeLocationUpdates((LocationListener) this);
	        }
	        /*
	         * After disconnect() is called, the client is
	         * considered "dead".
	         */
	        // Disconnecting the client invalidates it. 
	        locationClient.disconnect();
		}
	}
}
