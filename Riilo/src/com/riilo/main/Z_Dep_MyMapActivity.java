package com.riilo.main;

import java.util.List;

import com.example.camtests.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Z_Dep_MyMapActivity extends Fragment implements OnMapClickListener{

	List<Post> posts;
	
	private GoogleMap mMap;
	double curLat, curLong;
	
	private MapView mMapView;
	private View view;

//	private PostsCache postsCache;
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        view = inflater.inflate(R.layout.z_dep_my_map_activity, container, false);
//        setContentView(R.layout.my_map_activity);
        setUpMapIfNeeded();
//        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMapView = (MapView) view.findViewById(R.id.map);
//        Intent intent = getIntent();
//        curLat = intent.getDoubleExtra(TakePhotoActivity.CURRENT_LOCATION_LAT_KEY, 0);
//        curLong = intent.getDoubleExtra(TakePhotoActivity.CURRENT_LOCATION_LONG_KEY, 0);
       
//        postsCache = PostsCache.getInstance();
        return null;
    }
    
    public void onStart() {
    	super.onStart();
    	setUpMapIfNeeded();
        CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(curLat, curLong), 15);
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
//        mMap.animateCamera(update);

        
    }
    
    private void addMarkersToMap() {
    	posts = PostsCache.getInstance(getActivity().getApplicationContext()).getPostsAsList();
    	LatLng postPosition = null;
    	for(Post p: posts){
    		postPosition = new LatLng(p.getLatitude(), p.getLongitude()); 
    		mMap.addMarker(new MarkerOptions()
						.position(postPosition)
						.snippet("Posted on "+ Helpers.dateToString(p.getDateCreated()))
						.draggable(false)
						.title("Some title"));
    	}
    	
    	
//        // Uses a colored icon.
//        mBrisbane = mMap.addMarker(new MarkerOptions()
//                .position(BRISBANE)
//                .title("Brisbane")
//                .snippet("Population: 2,074,200"));
//
//        // Uses a custom icon.
//        mSydney = mMap.addMarker(new MarkerOptions()
//                .position(SYDNEY)
//                .title("Sydney")
//                .snippet("Population: 4,627,300"));
//
//        // Creates a draggable marker. Long press to drag.
//        mMelbourne = mMap.addMarker(new MarkerOptions()
//                .position(MELBOURNE)
//                .title("Melbourne")
//                .snippet("Population: 4,137,400")
//                .draggable(true));
//
//        // A few more markers for good measure.
//        mPerth = mMap.addMarker(new MarkerOptions()
//                .position(PERTH)
//                .title("Perth")
//                .snippet("Population: 1,738,800"));
//        mAdelaide = mMap.addMarker(new MarkerOptions()
//                .position(ADELAIDE)
//                .title("Adelaide")
//                .snippet("Population: 1,213,000"));

        // Creates a marker rainbow demonstrating how to create default marker icons of different
        // hues (colors).
//        int numMarkersInRainbow = 12;
//        for (int i = 0; i < numMarkersInRainbow; i++) {
//            mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(
//                            -30 + 10 * Math.sin(i * Math.PI / (numMarkersInRainbow - 1)),
//                            135 - 10 * Math.cos(i * Math.PI / (numMarkersInRainbow - 1))))
//                    .title("Marker " + i)
//                    .icon(BitmapDescriptorFactory.defaultMarker(i * 360 / numMarkersInRainbow)));
//        }
    }

    @Override
	public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mMapView.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
    	addMarkersToMap();
    }

	@Override
	public void onMapClick(LatLng arg0) {
		Log.d("<<<<<<<<<<<<<<<<<<<<<<<<<", arg0.latitude+" "+arg0.longitude);
		View layout = view.findViewById(R.id.create_post_pannel);
		Animation slideIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                R.anim.slide_in_top);
		if (layout.getVisibility()==View.INVISIBLE){
			layout.setVisibility(View.VISIBLE);
//			layout.bringToFront();
//			layout.refreshDrawableState();
			layout.requestLayout();
			layout.startAnimation(slideIn);
			
		}
		
		View editorView = view.findViewById(R.id.editor_message);
		editorView.setVisibility(View.VISIBLE);
		
		
		view.findViewById(R.id.create_post_buttons).setVisibility(View.VISIBLE);
		view.findViewById(R.id.take_picture).setVisibility(View.VISIBLE);
		view.findViewById(R.id.choose_from_gallery).setVisibility(View.VISIBLE);
		view.findViewById(R.id.post).setVisibility(View.VISIBLE);		
	}
    
}
