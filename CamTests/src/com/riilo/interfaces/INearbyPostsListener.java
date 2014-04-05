package com.riilo.interfaces;

import java.util.List;

import com.riilo.main.Post;

public interface INearbyPostsListener {

	public void retrievedNearbyPosts(List<Post> newPosts);
	
}
