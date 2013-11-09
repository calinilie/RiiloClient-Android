package com.example.camtests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.model.people.Person.Collection;

public class PostViewActivity extends BaseActivity
	implements android.view.View.OnClickListener{
	
	private GoogleMap mMap;
	
	Post currentPost = null;
	PostListItemAdapter adapter;
 	List<Post> adapterData = new ArrayList<Post>();
 	ListView postsList;
 	
 	ImageButton cancelButton;
 	ImageButton postButton;
 	EditText inputText; 	
 	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_layout);
        initLocationClient(LocationRequest.PRIORITY_HIGH_ACCURACY, 2000, 1000);
        
        Bundle bundle = getIntent().getBundleExtra(StringKeys.POST_BUNDLE);        
        currentPost = new Post(bundle);
        adapterData.add(currentPost);
    }
	
	@Override
	protected void onStart(){
		super.onStart();
		setUpMapIfNeeded();
		setupWidgetsViewElements();
		if (adapter==null){
			adapter = new PostListItemAdapter(this, R.layout.post_list_view_item_layout, adapterData, deviceId, false);
		}		
		postsList.setAdapter(adapter);
		
		List<Post> postsInConversation = PostsCache.getInstance(this).getPostsByConversationId(currentPost.getConversationId(), adapter, adapterData, null);
		if (Helpers.renewList(adapterData, postsInConversation, false)){
			adapter.notifyDataSetChanged();
		}
//		postsList.smoothScrollToPosition(adapterData.indexOf(currentPost));
		postsList.setSelection(adapterData.indexOf(currentPost));
		
		cancelButton.setOnClickListener(this);
		postButton.setOnClickListener(this);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(adapterData));
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		setupWidgetsViewElements();
		if (savedInstanceState.containsKey(StringKeys.POST_LIST_PARCELABLE)){
			PostsListParcelable parcelable = savedInstanceState.getParcelable(StringKeys.POST_LIST_PARCELABLE); 
			if (Helpers.renewList(adapterData, parcelable.getPostsList())){
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.post_view_layout_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void setupWidgetsViewElements() {
		postsList = (ListView)findViewById(R.id.posts_list_view);
		cancelButton = (ImageButton)findViewById(R.id.button_cancel);
		postButton = (ImageButton)findViewById(R.id.button_post);
		inputText = (EditText)findViewById(R.id.editor_message);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case (R.id.button_cancel):
			hideReplyToPostPannel_showList();
			break;
		case(R.id.button_post):
			postButtonPressed();
			break;
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_reply:
			hideList_showReplyToPostPannel();
			return true;
		default:
            return super.onOptionsItemSelected(item);
		}
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
                setUpMap();
            }
        }
    }
    
     private void setUpMap(){
    	 for(Post p : adapterData){
    		 mMap.addMarker(new MarkerOptions().position(new LatLng(p.getLatitude(), p.getLongitude())));
    	 }
    	 CameraPosition cPos = CameraPosition.fromLatLngZoom(new LatLng(currentPost.getLatitude(), currentPost.getLongitude()), 12);
    	 CameraUpdate update = CameraUpdateFactory.newCameraPosition(cPos);
    	 mMap.animateCamera(update);
     }
     
     private void hideList_showReplyToPostPannel(){
    	 /*Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(),
                 R.anim.slide_out_top);*/
    	 Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(), 
    			 R.anim.slide_in_top);
    	 //View list = findViewById(R.id.posts_list_view);
    	 View replyView = findViewById(R.id.reply_to_post_pannel);
    	 /*if (list.getVisibility()==View.VISIBLE){
    		 list.setVisibility(View.GONE);
    		 list.requestLayout();
    		 list.startAnimation(slideOut);
    	 }*/
    	 if (replyView.getVisibility() == View.GONE){
    		 replyView.setVisibility(View.VISIBLE);
    		 replyView.requestLayout();
    		 replyView.startAnimation(slideIn);
    	 }
     }
     
     private void hideReplyToPostPannel_showList(){
    	 InputMethodManager inputManager = (InputMethodManager)
                 getSystemService(Context.INPUT_METHOD_SERVICE);
    	 if (inputManager!=null)
    		 if (getCurrentFocus()!=null)
		    	 inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
		                    InputMethodManager.HIDE_NOT_ALWAYS);
    	 
    	 Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(),
                 R.anim.slide_out_top);
    	 Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(), 
    			 R.anim.slide_in_top);
    	 View list = findViewById(R.id.posts_list_view);
    	 View replyView = findViewById(R.id.reply_to_post_pannel);
    	 
    	 if (replyView.getVisibility()==View.VISIBLE){
    		 replyView.setVisibility(View.GONE);
    		 replyView.requestLayout();
    		 replyView.startAnimation(slideOut);
    	 }
    	 if (list.getVisibility() == View.GONE){
    		 list.setVisibility(View.VISIBLE);
    		 list.requestLayout();
    		 list.startAnimation(slideIn);
    	 }
     }
     
     private void postButtonPressed(){
			String message = inputText.getText().toString();
			Post replyPost = new Post();
			replyPost.setMessage(message);
			replyPost.setUserAtLocation(location, currentPost.getLatitude(), currentPost.getLongitude());
			replyPost.setDateCreated(Calendar.getInstance().getTime());
			replyPost.setUserId(deviceId);
			replyPost.setLatitude(currentPost.getLatitude());
			replyPost.setLongitude(currentPost.getLongitude());
			replyPost.setRepliesToPostId(currentPost.getId());
			replyPost.setConversationId(currentPost.getConversationId());
			
			hideReplyToPostPannel_showList();
			adapterData.add(replyPost);
			adapter.notifyDataSetChanged();
			
			/*double r = 0.0005;
			int numberOfMarkers = 30;
			double angle = 360/numberOfMarkers;
			double angleSum = 0;
			int counter=0;
			while (angleSum<360){
				angleSum+=angle;
				double newLatitude = currentPost.getLatitude() + r*Math.cos(angleSum*Math.PI/180);
				double newLongitude = currentPost.getLongitude() + r*Math.sin(angleSum*Math.PI/180);
				mMap.addMarker(new MarkerOptions()
								.position(new LatLng(newLatitude, newLongitude))
								.icon(BitmapDescriptorFactory.defaultMarker((float) (((counter % 2 == 0) ? counter : numberOfMarkers-counter)*359 / numberOfMarkers))));
				counter++;
			}*/

    		Intent intentPost= new Intent(this, WorkerService.class);
    		intentPost.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_POST);
    		intentPost.putExtra(StringKeys.POST_BUNDLE, replyPost.toBundle());
        	startService(intentPost);
     }
}
