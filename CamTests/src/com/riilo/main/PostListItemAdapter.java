package com.riilo.main;

import java.text.DecimalFormat;
import java.util.List;

import com.riilo.main.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostListItemAdapter extends ArrayAdapter<Post>{

	private static final String TAG = "<<<<<<<<PostListItemAdapter>>>>>>>>>";
	
	private List<Post> items;
	DecimalFormat decimalFormat10, decimalFormat1;
	private String currentUserId;
	private boolean showDistance;
	private String unitOfMeasure = "km";
	private Context context;
	
	
	public PostListItemAdapter(Context context, int layoutId, List<Post> items, String currentUserId, boolean showDistance) {
		super(context, layoutId, items);
		this.context = context;
		this.items = items;
		decimalFormat10 = new DecimalFormat("#.#");
		decimalFormat1 = new DecimalFormat("#.##");
		this.currentUserId = currentUserId;
		this.showDistance = showDistance;
		if (Helpers.inMiles())
			unitOfMeasure = "mi";
	}
	
	@Override
	public int getCount() {
		int count = (items != null) ? items.size() : 0;
	    return count;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LinearLayout postView;
		Post post = getItem(position);
//		if (convertView == null) {
			postView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater)getContext().getSystemService(inflater);
			
			if (post.isAnouncement()){//post.isAnouncement()
				li.inflate(R.layout.announcement_item_layout, postView, true);
				setupAnnouncementView(post, postView);
			}
			else if (post.getAchievementId() == 1){//post.getAchievementId() == 1
				li.inflate(R.layout.achievement_item_layout, postView, true);
				setupAchievementView(post, postView);
			}else {
				li.inflate(R.layout.post_item_layout, postView, true);
				setupPostView(post, postView);
			}
//	    } else {
//    	postView = (LinearLayout) convertView;
//    }
		
		return postView;
	}
	
	private void setupAchievementView(Post post, View view){
		TextView message_textView = (TextView) view.findViewById(R.id.achievement_message);
		message_textView.setText(post.getMessage());
	}
	
	private void setupAnnouncementView(Post post, View view){
		TextView message_textView = (TextView) view.findViewById(R.id.announcement_message);
		TextView date_textView = (TextView) view.findViewById(R.id.announcement_date);
		
		message_textView.setText(post.getMessage());
		date_textView.setText(this.context.getString(R.string.post_adapter_posted_on)+" "+post.getDateAsString());
	}
	
	private void setupPostView(Post post, View view){
		TextView userId_textView = (TextView) view.findViewById(R.id.postListItem_userId);
		TextView userAction_textView = (TextView) view.findViewById(R.id.postListItem_userAction);
		TextView distanceAndDate_textView = (TextView) view.findViewById(R.id.postListItem_distance_date);
		TextView message_textView = (TextView) view.findViewById(R.id.postListItem_message);
		ImageView userAtLocation_ImageView = (ImageView)view.findViewById(R.id.postListItem_userAtLocation);
		ImageView badge_imageView = (ImageView)view.findViewById(R.id.badgeRiiloFirst);
		
		String alias = post.getAlias();
		boolean hasAlias = alias != null && !alias.isEmpty();
		
		if (post.getUserId().equalsIgnoreCase(this.currentUserId)){
			if (hasAlias){
				userId_textView.setText(String.format("%s, %s,", this.context.getString(R.string.post_adapter_you), alias));
			}
			else{
				userId_textView.setText(this.context.getString(R.string.post_adapter_you));
			}
			
			if (post.getRepliesToPostId() == 0){
				userAction_textView.setText(this.context.getString(R.string.post_adapter_posted_1st));
			}
			else{
				userAction_textView.setText(this.context.getString(R.string.post_adapter_replied_1st));
			}
		}
		else{
			if (hasAlias){
				userId_textView.setText(alias);
			}
			else{
				userId_textView.setText(this.context.getString(R.string.post_adapter_somebody));
			}
			if (post.getRepliesToPostId() == 0){
				userAction_textView.setText(this.context.getString(R.string.post_adapter_posted_3rd));
			}
			else{
				userAction_textView.setText(this.context.getString(R.string.post_adapter_replied_3rd));
			}
		}
		
		String postedOnDistance = "";
		if (showDistance){
			if (post.getDistanceFromCurLoc()==-1){
				postedOnDistance = String.format("%s %s %s",
												this.context.getString(R.string.post_adapter_waiting_for_location),
												this.context.getString(R.string.post_adapter_posted_on),
												post.getDateAsString());
			}
			else{
				double dist = post.getDistanceFromCurLoc();
				String distanceAsString = "";
				if (dist<1){
					distanceAsString = decimalFormat1.format(dist);
				}
				else if (dist<10){
					distanceAsString = decimalFormat10.format(dist);
				}
				else {
					distanceAsString = ((int)dist)+"";
				}
				postedOnDistance = String.format("~ %s %s %s %s %s", 
												distanceAsString, 
												unitOfMeasure, 
												this.context.getString(R.string.post_adapter_away),
												this.context.getString(R.string.post_adapter_posted_on),
												post.getDateAsString());
			}
		}
		else{
			postedOnDistance = String.format("%s %s", this.context.getString(R.string.post_adapter_posted_on), post.getDateAsString());
		}
		distanceAndDate_textView.setText(postedOnDistance);
		message_textView.setText(post.getMessage());
		if (!post.isUserAtLocation()){
			userAtLocation_ImageView.setVisibility(View.GONE);
		}
		if (hasAlias){
			badge_imageView.setVisibility(View.VISIBLE);
		}
	}
}
