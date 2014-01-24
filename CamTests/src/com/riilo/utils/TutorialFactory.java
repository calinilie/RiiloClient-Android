package com.riilo.utils;

import java.util.List;

import com.riilo.main.Facade;
import com.riilo.main.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;

public class TutorialFactory implements OnClickListener{

	private boolean show = true;
	private Context context;
	private LayoutInflater inflater;
	private ViewGroup masterView;
	private View currentView;
	private View darkBackground;
	
	private int currentResourceId;
	private List<Integer> resources;
	private Facade facade;
	
	private TutorialFactory(Context context, ViewGroup masterView){
		this.context = context;
		this.masterView = masterView;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.facade = Facade.getInstance(context);
	}
	
	public TutorialFactory(Context context, ViewGroup masterView, List<Integer> dialogResource){
		this(context, masterView);
		this.resources = dialogResource;
	}
	
	public void startTutorial(){
		this.startTutorial(false);
	}
	
	public void startTutorial(boolean auto){
		show = !facade.wereTutorialsRun(resources);
		if (show){
			darkBackground = inflater.inflate(R.layout.tutorial_dark_bg, masterView, false);
			masterView.addView(darkBackground);
			showView(darkBackground);
			if (auto)
				showTutorialDialog(resources.get(0));
		}
	}
	
	public void endTutorial(){
		hideView(darkBackground);
		masterView.removeView(darkBackground);
	}
	
	public void showTutorialDialog(int resource){
		if (show){
			currentView = inflater.inflate(resource, masterView, false);
			currentView.findViewById(R.id.button_close_tutorial_dialog).setOnClickListener(this);
			currentResourceId = resource;
			masterView.addView(currentView);
			showView(currentView);
		}
	}
	
	@Override
	public void onClick(View arg0) {
		closeCurrentDialog();
	}
	
	private void closeCurrentDialog(){
		facade.updateTutorialRun(currentResourceId);
		hideView(currentView);
		masterView.removeView(currentView);
		
		int currentResourceIndex = resources.indexOf(currentResourceId);
		if (currentResourceIndex < resources.size()-1){
			showTutorialDialog(resources.get(currentResourceIndex+1));
		}
		else{
			endTutorial();
		}
	}

		
	//animation helper methods
	private void showView(View view){
		startAnimation(view, R.anim.fade_in);
		view.setVisibility(View.VISIBLE);
	}
	
	private void hideView(View view){
		startAnimation(view, R.anim.fade_out);
		view.setVisibility(View.GONE);
	}
	
	
	private void startAnimation(View view, int resourceId){
		Animation animation = AnimationUtils.loadAnimation(context.getApplicationContext(),
	                resourceId);
		view.startAnimation(animation);
		view.requestLayout();
		
	}
}
