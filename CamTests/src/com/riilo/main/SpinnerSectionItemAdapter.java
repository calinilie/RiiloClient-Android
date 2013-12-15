package com.riilo.main;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SpinnerSectionItemAdapter extends ArrayAdapter<SpinnerSection> implements SpinnerAdapter {

	private static final String TAG = "<<<<<<<<<SpinnerSectionItemAdapter>>>>>>>>>";
	private int layoutId;
	private List<SpinnerSection> items;
	
	public SpinnerSectionItemAdapter(Context context, int layoutId, List<SpinnerSection> objects) {
		super(context, layoutId, objects);
		this.layoutId = layoutId;
		this.items = objects;
	}
	
	@Override
	public int getCount() {
		int count = (items != null) ? items.size() : 0;
		//Log.d("PostsCache.getPostsByConversationId()","itemAdapter.getcount "+count);
	    return count;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LinearLayout sectionView;
		SpinnerSection section = getItem(position);
		if (convertView==null){
			sectionView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater)getContext().getSystemService(inflater);
			li.inflate(layoutId, sectionView, true);
		}
		else{
			sectionView = (LinearLayout) convertView;
		}
		
		TextView title = (TextView) sectionView.findViewById(R.id.section_title);
		TextView notifications = (TextView) sectionView.findViewById(R.id.section_notifications);
		ImageView image = (ImageView)sectionView.findViewById(R.id.section_icon);
		
		title.setText(section.getTitle());
		if (section.isShowNotifications()){
			notifications.setText(section.getNotifications()+"");
		}
		else{
			notifications.setVisibility(View.GONE);
		}
//		image.setVisibility(View.GONE);
		image.setImageResource(section.getIconResId());
		
		
		return sectionView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		LinearLayout sectionView;
		SpinnerSection section = getItem(position);
		if (convertView==null){
			sectionView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater)getContext().getSystemService(inflater);
			li.inflate(layoutId, sectionView, true);
		}
		else{
			sectionView = (LinearLayout) convertView;
		}
		
		TextView title = (TextView) sectionView.findViewById(R.id.section_title);
		TextView notifications = (TextView) sectionView.findViewById(R.id.section_notifications);
		ImageView image = (ImageView)sectionView.findViewById(R.id.section_icon);
		
		title.setText(section.getTitle());
		if (section.isShowNotifications()){
			notifications.setText(section.getNotifications()+"");
		}
		else{
			notifications.setVisibility(View.GONE);
		}
		image.setImageResource(section.getIconResId());
		
		
		return sectionView;
	}
	
}
