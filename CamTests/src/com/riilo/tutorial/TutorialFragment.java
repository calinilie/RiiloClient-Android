package com.riilo.tutorial;

import com.riilo.main.R;
import com.riilo.main.Facade;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;

public class TutorialFragment extends Fragment implements OnClickListener, AnimationListener{

	private boolean isLastPage;
	private int position;
	private View view;
	
	public void setIsLastPage(boolean isLastPage){
		this.isLastPage = isLastPage;
	}
	
	public void setPosition(int position){
		this.position = position;
	}
	
	
	private TutorialActivity activity;
	
	@Override
	public void onAttach(Activity activity){
 		this.activity = (TutorialActivity) activity;
 		super.onAttach(activity);
 	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		int resourceId = 0;
		switch (this.position){
		case 0:
			resourceId = R.layout.z_dep_tutorial_fragment_layout_0;
			break;
		case 1:
			resourceId = R.layout.z_dep_tutorial_fragment_layout_1;
			break;
		case 2:
			resourceId = R.layout.z_dep_tutorial_fragment_layout_2;
			break;
		case 3:
			resourceId = R.layout.z_dep_tutorial_fragment_layout_3;
			break;
		case 4:
			resourceId = R.layout.z_dep_tutorial_fragment_layout_4;
			break;
		case 5:
			resourceId = R.layout.z_dep_tutorial_fragment_layout_5;
			break;
		}
		
    	ViewGroup rootView = (ViewGroup) inflater.inflate(resourceId, container, false);
    	if (isLastPage){
    		((Button)rootView.findViewById(R.id.button_end_tutorial)).setVisibility(View.VISIBLE);
    		((Button)rootView.findViewById(R.id.button_end_tutorial)).setOnClickListener(this);
    	}
    	else if (position==0){
    		rootView.findViewById(R.id.tutorial_layout_content0).setOnClickListener(this);
    		rootView.findViewById(R.id.button_close_tutorial_swipe).setOnClickListener(this);
    	}
    	this.view = rootView;
        return rootView;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button_end_tutorial){
			Facade.getInstance(activity).updateTutorialRun();
			activity.analytics.recordEvent_Tutorial_EndButtonClick();
			activity.finish();
		}
		else if (v.getId()==R.id.tutorial_layout_content0){
			View tutorialSwipeContainer = view.findViewById(R.id.tutorial_tutorial_swipe_container);
			
			Animation fadeIn = AnimationUtils.loadAnimation(activity.getApplicationContext(),
   	                R.anim.fade_in);
   	   	 	
	   	   	if (tutorialSwipeContainer.getVisibility()==View.GONE){
	   	   		 tutorialSwipeContainer.startAnimation(fadeIn);
	   	   		 tutorialSwipeContainer.requestLayout();
	   	   		 tutorialSwipeContainer.setVisibility(View.VISIBLE);
	   	   		 view.findViewById(R.id.tutorial_swipe).setVisibility(View.VISIBLE);
	   	   	 }
		}else if (v.getId() == R.id.button_close_tutorial_swipe){
			View tutorialSwipe = view.findViewById(R.id.tutorial_swipe);
			
			Animation slideOut = AnimationUtils.loadAnimation(activity.getApplicationContext(),
	                R.anim.slide_out_bottom);
	   	 	slideOut.setAnimationListener(this);
	   	 	
	   	   	if (tutorialSwipe != null && tutorialSwipe.getVisibility()==View.VISIBLE){
	   	   		tutorialSwipe.startAnimation(slideOut);
	   	   		tutorialSwipe.requestLayout();
	   			tutorialSwipe.setVisibility(View.GONE);
	   	   	 }
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		View tutorialSwipeContainer = view.findViewById(R.id.tutorial_tutorial_swipe_container);
		
		Animation fadeOut = AnimationUtils.loadAnimation(activity.getApplicationContext(),
	                R.anim.fade_out);
	   	 
   	   	if (tutorialSwipeContainer != null && tutorialSwipeContainer.getVisibility()==View.VISIBLE){
   	   		 tutorialSwipeContainer.startAnimation(fadeOut);
   	   		 tutorialSwipeContainer.requestLayout();
   	   		 tutorialSwipeContainer.setVisibility(View.GONE);
   	   	 }
		
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		
	}
}
