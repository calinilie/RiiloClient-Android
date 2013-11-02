package com.example.camtests;

import java.util.Calendar;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class UserAtLocationPostActivity extends BaseActivity
	implements OnClickListener{

	private GoogleMap mMap;
	private Post currentPost;
	
	private Button buttonPost;
	private Button buttonTakePicture;
	private Button buttonChooseFromGallery;
	private EditText inputMessage;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_dep_user_at_location_post);
        initLocationClient(LocationRequest.PRIORITY_HIGH_ACCURACY, 2000, 1000);
        currentPost = new Post();
    }
	
	@Override
    protected void onStart() {
    	super.onStart();
    	setUpMapIfNeeded();
    	setupWidgetsViewElements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	switch(requestCode){
	    	case StringKeys.TAKE_PICTURE_REQUEST_CODE:
	    		Uri uri = super.onTakePhotoActivityResult(requestCode, resultCode, intent);
	    		if (uri!=null)
	    			currentPost.setUri(uri.toString());
	    		break;
    	}
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.button_post:
			String message = inputMessage.getText().toString();
			currentPost.setAccuracy(location.getAccuracy());
			currentPost.setLatitude(location.getLatitude());
			currentPost.setLongitude(location.getLongitude());
			currentPost.setMessage(message);
			currentPost.setUserAtLocation(true);
			currentPost.setDateCreated(Calendar.getInstance().getTime());
			currentPost.setUserId(deviceId);
			if (currentPost.getUri()!=null)
				Log.d("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", currentPost.getUri());
    		Intent intentPost= new Intent(this, WorkerService.class);
    		intentPost.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_POST);
    		intentPost.putExtra(StringKeys.POST_BUNDLE, currentPost.toBundle());
        	startService(intentPost);
        	
			break;
		case R.id.button_take_picture:
			super.startCameraIntent();
			break;
		case R.id.button_choose_from_gallery:
			break;
		}
	}
	
	@Override
	public void onLocationChanged(Location location){
		super.onLocationChanged(location);
		CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15);
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
		mMap.animateCamera(update);
		if (location.getAccuracy()<50){
			buttonPost.setEnabled(true);
			if (location.getAccuracy()<20)
				buttonPost.setBackgroundColor(getResources().getColor(R.color.goodGps));
			else
				buttonPost.setBackgroundColor(getResources().getColor(R.color.mediumGps));
		}
	}

	@Override
	protected void setupWidgetsViewElements() {
		buttonPost = ((Button)findViewById(R.id.button_post));
        buttonPost.setOnClickListener(this);
        
        buttonTakePicture = ((Button)findViewById(R.id.button_take_picture));
        buttonTakePicture.setOnClickListener(this);
        
        buttonChooseFromGallery = ((Button)findViewById(R.id.button_choose_from_gallery));
        buttonChooseFromGallery.setOnClickListener(this);
        
        inputMessage = (EditText)findViewById(R.id.editor_message);
	}
	
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
            	mMap.setMyLocationEnabled(true);
//                setUpMap();
            }
        }
    }

}
