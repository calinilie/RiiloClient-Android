package com.example.camtests;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostListItemAdapter extends ArrayAdapter<Post>{

	private int layoutId;
	private List<Post> items;
	DecimalFormat df10, df1;
	private String currentUserId;
	private boolean showDistance;
	
	public PostListItemAdapter(Context context, int resource, List<Post> items, String currentUserId, boolean showDistance) {
		super(context, resource, items);
		this.layoutId = resource;
		this.items = items;
		df10 = new DecimalFormat("#.#");
		df1 = new DecimalFormat("#.##");
		this.currentUserId = currentUserId;
		this.showDistance = showDistance;
	}
	
	@Override
	public int getCount() {
	    // TODO Auto-generated method stub
		int count = (items != null) ? items.size() : 0;
//		Log.d("PostsCache.getPostsByConversationId()","itemAdapter.getcount "+count);
	    return count;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LinearLayout postView;
		Post post = getItem(position);
		if (convertView == null) {
			postView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater)getContext().getSystemService(inflater);
			li.inflate(layoutId, postView, true);
	    } else {
	    	postView = (LinearLayout) convertView;
	    }
		
		TextView userId_textView = (TextView) postView.findViewById(R.id.postListItem_userId);
		TextView userAction_textView = (TextView) postView.findViewById(R.id.postListItem_userAction);
		TextView distanceAndDate_textView = (TextView) postView.findViewById(R.id.postListItem_distance_date);
		TextView message_textView = (TextView) postView.findViewById(R.id.postListItem_message);
		TextView postId_textView = (TextView) postView.findViewById(R.id.postListItem_postId);
		
		
		userId_textView.setText(post.getUserId().equalsIgnoreCase(this.currentUserId)? "You" : "User "+post.getUserId());
		String userAction = post.getRepliesToPostId() == 0 ? "posted:" : "replyed:";
		userAction_textView.setText(userAction);
		String postedOnDistance = "";
		
		if (showDistance){
			if (post.getDistanceFromCurLoc()==-1){
				postedOnDistance = String.format("waiting for location, posted on %s", post.getDateAsString());
			}
			else{
				double dist = post.getDistanceFromCurLoc();
				String distanceAsString = "";
				if (dist<1){
					distanceAsString = df1.format(dist);
				}
				else if (dist<10){
					distanceAsString = df10.format(dist);
				}
				else {
					distanceAsString = ((int)dist)+"";
				}
				postedOnDistance = String.format("~ %s %s away, posted on %s", distanceAsString, Helpers.inMiles() ? "mi" : "km",post.getDateAsString());
			}
		}
		else{
			postedOnDistance = String.format("posted on %s", post.getDateAsString());
		}
		distanceAndDate_textView.setText(postedOnDistance);
		message_textView.setText(String.format("\"%s\"", post.getMessage()));
		postId_textView.setText("Post "+post.getId() + " " + post.getConversationId());
		
		
		return postView;
	}
}
