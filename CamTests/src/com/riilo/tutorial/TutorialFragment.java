package com.riilo.tutorial;

import com.example.camtests.MainActivity;
import com.example.camtests.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TutorialFragment extends Fragment implements OnClickListener{

	private String text;
	private boolean isLastPage;
	
	public void setText(String text){
		this.text = text;
	}
	
	public void setIsLastPage(boolean isLastPage){
		this.isLastPage = isLastPage;
	}
	
	private Activity activity;
	
	@Override
	public void onAttach(Activity activity){
 		this.activity = activity;
 		super.onAttach(activity);
 	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
    	ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tutorial_fragment_layout, container, false);
    	((TextView)rootView.findViewById(R.id.tutorial_textView)).setText(text);
    	if (isLastPage){
    		((Button)rootView.findViewById(R.id.button_end_tutorial)).setVisibility(View.VISIBLE);
    		((Button)rootView.findViewById(R.id.button_end_tutorial)).setOnClickListener(this);
    	}
        return rootView;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button_end_tutorial){
			activity.finish();
		}
	}
}