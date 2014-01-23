package com.riilo.utils;

import java.util.List;

import com.riilo.main.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class TutorialFactory implements OnClickListener{

	private boolean show = true;
	private Context context;
	private LayoutInflater inflater;
	private ViewGroup masterView;
	private View currentView;
	private View darkBackground;
	
	private int currentResource;
	private List<Integer> resources;
	
	private TutorialFactory(Context context, ViewGroup masterView){
		this.context = context;
		this.masterView = masterView;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public TutorialFactory(Context context, ViewGroup masterView, List<Integer> dialogResource){
		this(context, masterView);
		this.resources = dialogResource;
	}
	
	public void startTutorial(){
		this.startTutorial(false);
	}
	
	public void startTutorial(boolean auto){
		if (show){
			darkBackground = inflater.inflate(R.layout.tutorial_dark_bg, masterView, false);
			darkBackground.setVisibility(View.VISIBLE);
			masterView.addView(darkBackground);
			if (auto)
				showTutorialDialog(resources.get(0));
		}
	}
	
	public void endTutorial(){
		masterView.removeView(darkBackground);
	}
	
	public void showTutorialDialog(int resource){
		if (show){
			currentView = inflater.inflate(resource, masterView, false);
			currentView.findViewById(R.id.button_close_tutorial_dialog).setOnClickListener(this);
			currentResource = resource;
			currentView.setVisibility(View.VISIBLE);
			masterView.addView(currentView);
		}
	}
	
	private void closeCurrentDialog(){
//		currentView.setVisibility(View.GONE);
		masterView.removeView(currentView);
		
		int currentResourceIndex = resources.indexOf(currentResource);
		if (currentResourceIndex < resources.size()-1){
			showTutorialDialog(resources.get(currentResourceIndex+1));
		}
		else{
			endTutorial();
		}
	}

	@Override
	public void onClick(View arg0) {
		closeCurrentDialog();
	}
}
