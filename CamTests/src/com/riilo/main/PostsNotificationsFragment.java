package com.riilo.main;

import java.util.Arrays;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

import com.riilo.interfaces.FragmentBase;
import com.riilo.main.R;
import com.riilo.main.AnalyticsWrapper.EventLabel;
import com.riilo.utils.TutorialFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PostsNotificationsFragment 
				extends FragmentBase 
				implements OnItemClickListener{
	
 	ListView postsListView;
    
 	private View view;
 	private MainActivity activity;
 	
 	private TutorialFactory tutorial;
 	
 	
 	public PostsNotificationsFragment(){
		super();
		Log.d("PostsNotificationsFragment", "PostsNotificationsFragment constructor");
	}
 	
 	
 	public void onAttach(Activity activity){
 		this.activity = (MainActivity)activity;
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
 		
 		setupWidgetsViewElements();
 		
 		pullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout_fragment);
 		ActionBarPullToRefresh.from(activity)
 			.options(Options.create().scrollDistance(.33f).build())
 			.allChildrenArePullable()
 			.listener(this)
 			.setup(pullToRefreshLayout);
        
 		setupTutorials();
 		
 		return view;
 	}
 	
 	@Override
	public void onStart(){
		super.onStart();
		
		if (adapter==null){
			adapter = new PostListItemAdapter(activity, R.layout.post_item_layout, adapterData, activity.deviceId, false);
		}		
		postsListView.setAdapter(adapter);
		
		List<Post> newNotifications = PostsCache.getInstance(activity).getNotifications(activity.deviceId, this, true);
		if (Helpers.renewList(adapterData, newNotifications)){
			adapter.notifyDataSetChanged();
		}
 	}
 	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putParcelable(StringKeys.POST_LIST_PARCELABLE, new PostsListParcelable(adapterData));
	}
 	
	protected void setupWidgetsViewElements() {
		postsListView = (ListView)view.findViewById(R.id.posts_listView);
		postsListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long index) {
		Post post = adapterData.get((int) index);
		
		activity.analytics.recordEvent_General_ItemClick(EventLabel.tab_notifications);
		
		PostsCache.getInstance(activity).removeNotification(post);
		activity.getSpinnerAdapter().getItem(3).descreaseNotificationNumber();
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
		activity.setAnimationType(StringKeys.ANIMATION_TYPE_SLIDE_IN_RIGHT);
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		activity.analytics.recordEvent_General_PullToRefresh(EventLabel.tab_notifications);
		PostsCache.getInstance(activity).getNotifications(activity.deviceId, this, true);
	}
	
	private void setupTutorials(){
		List<Integer> firstTutorial = Arrays.asList(R.layout.tutorial_notifications_dialog);
		tutorial = new TutorialFactory(activity, (ViewGroup) view, firstTutorial);
		tutorial.startTutorial(true);
	}

}
