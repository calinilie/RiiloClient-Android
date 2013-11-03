package com.example.camtests;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PostsFeedFragment 
				extends Fragment 
				implements OnItemClickListener{

    // Update frequency in milliseconds
    private static final int UPDATE_INTERVAL = 10000;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = 5000;
	
	PostListItemAdapter adapter;
 	List<Post> adapterData = new ArrayList<Post>();
 	ListView postsList;
    
 	private View view;
 	private BaseActivity activity;
 	
 	public void onAttach(Activity activity){
 		this.activity = (BaseActivity)activity;
 		super.onAttach(activity);
 	}
 	
 	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState!=null && savedInstanceState.containsKey(StringKeys.POST_LIST_PARCELABLE)){
			PostsListParcelable parcelable = savedInstanceState.getParcelable(StringKeys.POST_LIST_PARCELABLE); 
			if (adapter!=null && Helpers.renewList(adapterData, parcelable.getPostsList())){
				adapter.notifyDataSetChanged();
			}
		}
//        initLocationClient(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL, FASTEST_INTERVAL);//TODO
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInsatnceState){
 		view = inflater.inflate(R.layout.posts_lists_layout, container, false);
 		return view;
 	}
 	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_view_layout_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}*/
 	
 	@Override
	public void onStart(){
		super.onStart();
		setupWidgetsViewElements();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_list_view_item_layout, adapterData, activity.deviceId, false);
		}		
		postsList.setAdapter(adapter);
		
		List<Post> newNotifications = PostsCache.getInstance(activity).getNotifications(activity.deviceId, adapter, adapterData, null, null, false, StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_ADAPTER_DESC);
		if (Helpers.renewList(adapterData, newNotifications)){
			adapter.notifyDataSetChanged();
		}
 	}
 	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(adapterData));
	}
 	
	protected void setupWidgetsViewElements() {
		postsList = (ListView)view.findViewById(R.id.posts_listView);
		postsList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		
		PostsCache.getInstance(activity).removeNotification(post);
		adapterData.remove(post);
		
		long conversationId = post.getConversationId();
		
		Intent invalidateConversation = new Intent(activity, WorkerService.class);
		invalidateConversation.putExtra(StringKeys.WS_INTENT_TYPE, StringKeys.WS_INTENT_NOTIFICATIONS_SILENCE);
		invalidateConversation.putExtra(StringKeys.NOTIFICATION_SILENCE_CONVERSATION_ID, conversationId);
		invalidateConversation.putExtra(StringKeys.NOTIFICATION_SILENCE_USER_ID, activity.deviceId);
		activity.startService(invalidateConversation);
		
		Intent postViewIntent = new Intent(activity, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
	}

}
