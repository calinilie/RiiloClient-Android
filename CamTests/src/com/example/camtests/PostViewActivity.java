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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.camtests.AnalyticsWrapper.EventLabel;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.model.people.Person.Collection;

public class PostViewActivity extends BaseActivity
	implements android.view.View.OnClickListener,
	OnMapClickListener,
	OnItemClickListener{
	
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
        getActionBar().setDisplayHomeAsUpEnabled(true);
        initLocationClient(LocationRequest.PRIORITY_HIGH_ACCURACY, 2000, 1000);
        
        Bundle bundle = getIntent().getBundleExtra(StringKeys.POST_BUNDLE);        
        currentPost = new Post(bundle);
        adapterData.add(currentPost);
    }
	
	@Override
	public void onStart(){
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
		
		
		analytics.recordScreenHit_Conversation();
		mMap.setOnMapClickListener(this);
		postsList.setOnItemClickListener(this);
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
			analytics.recordEvent_Conversation_ButtonClick(EventLabel.button_cancel);
			hideReplyToPostPannel();
			break;
		case(R.id.button_post):
			analytics.recordEvent_Conversation_ButtonClick(EventLabel.button_post);
			postButtonPressed();
			break;
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_reply:
			analytics.recordEvent_Conversation_ButtonClick(EventLabel.reply_button);
			showReplyToPostPannel();
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
     
     private void showReplyToPostPannel(){
    	 Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(), 
    			 R.anim.slide_in_bottom);
    	 View replyView = findViewById(R.id.reply_to_post_pannel);
    	 if (replyView.getVisibility() == View.GONE){
    		 replyView.setVisibility(View.VISIBLE);
    		 replyView.requestLayout();
    		 replyView.startAnimation(slideIn);
    	 }
     }
     
     private void hideReplyToPostPannel(){
    	 InputMethodManager inputManager = (InputMethodManager)
                 getSystemService(Context.INPUT_METHOD_SERVICE);
    	 if (inputManager!=null)
    		 if (getCurrentFocus()!=null)
		    	 inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
		                    InputMethodManager.HIDE_NOT_ALWAYS);
    	 
    	 Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(),
                 R.anim.slide_out_bottom);
    	 View replyView = findViewById(R.id.reply_to_post_pannel);
    	 
    	 if (replyView.getVisibility()==View.VISIBLE){
    		 replyView.startAnimation(slideOut);
    		 replyView.requestLayout();
    		 replyView.setVisibility(View.GONE);
    	 }
     }
     
    private void postButtonPressed(){
			String message = inputText.getText().toString();
			if (message!=null && !message.isEmpty()){
				Post replyPost = new Post();
				replyPost.setMessage(message);
				replyPost.setUserAtLocation(location, currentPost.getLatitude(), currentPost.getLongitude());
				replyPost.setDateCreated(Calendar.getInstance().getTime());
				replyPost.setUserId(deviceId);
				replyPost.setLatitude(currentPost.getLatitude());
				replyPost.setLongitude(currentPost.getLongitude());
				replyPost.setRepliesToPostId(currentPost.getId());
				replyPost.setConversationId(currentPost.getConversationId());
				
				hideReplyToPostPannel();
				adapterData.add(replyPost);
				adapter.notifyDataSetChanged();
	
	    		Intent intentPost= new Intent(this, WorkerService.class);
	    		intentPost.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_POST);
	    		intentPost.putExtra(StringKeys.POST_BUNDLE, replyPost.toBundle());
	        	startService(intentPost);
	        	
	        	inputText.setText("");
			}
			else{
				Toast.makeText(this, getString(R.string.invalid_post_empty), Toast.LENGTH_LONG).show();
			}
     }

	@Override
	public void onMapClick(LatLng arg0) {
		analytics.recordEvent_Conversation_MapClick(EventLabel.map);
	}
	
	@Override
	public void onBackPressed(){
		View replyView = findViewById(R.id.reply_to_post_pannel);
		if (replyView.getVisibility()==View.VISIBLE){
			hideReplyToPostPannel();
		}
		else{
			super.onBackPressed();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		analytics.recordEvent_Conversation_ItemClick(post.getConversationId());
		
		//TODO
		if (post.isUserAtLocation()){
			showInfoDialog(getString(R.string.user_at_location_dialog_message));
		}
		
	}
}
