package com.riilo.interfaces;

import java.util.List;

import com.riilo.main.Post;

public interface ILatestPostsListener {

	public void retrievedLatestPosts(List<Post> newPosts);
	
}
