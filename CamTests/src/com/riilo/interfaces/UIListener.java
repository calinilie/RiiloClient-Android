package com.riilo.interfaces;

import java.util.List;

import com.riilo.main.Post;

public interface UIListener {
	
	public void onLoadStart();
	
	public void onLoadEnd(List<Post> posts);
	
}
