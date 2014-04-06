package com.riilo.interfaces;

import java.util.List;

import com.riilo.main.Post;

public interface IPostsListener {

	public void startedRetrievingPosts();
	
	public void retrievedPosts(List<Post> newPosts);
	
}
