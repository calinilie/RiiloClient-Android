package com.riilo.interfaces;

import java.util.List;

import com.riilo.main.Post;

public interface UIListener {
	
	public void onLoadStart();
	
	//TODO use a result code to differentiate between event trigger types
	public void onLoadEnd(List<Post> posts, boolean isMapPostGroups);
	
}
