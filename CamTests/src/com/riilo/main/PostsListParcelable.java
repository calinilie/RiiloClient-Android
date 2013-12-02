package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class PostsListParcelable implements Parcelable{

	private List<Post> postsList;
	
	public PostsListParcelable(List<Post> postsList){
		this.postsList = postsList;
	}
	
	public PostsListParcelable(Parcel parcel){
		postsList = new ArrayList<Post>();
		parcel.readList(postsList, getClass().getClassLoader());
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (postsList==null)
			throw new RuntimeException("PostsListParcelable.postsList is null!!");
		
		dest.writeList(postsList);
	}
	
    public List<Post> getPostsList() {
		return postsList;
	}

	public static final Parcelable.Creator<PostsListParcelable> CREATOR
	    = new Parcelable.Creator<PostsListParcelable>() {
    	
			public PostsListParcelable createFromParcel(Parcel in) {
			    return new PostsListParcelable(in);
			}
			
			public PostsListParcelable[] newArray(int size) {
			    return new PostsListParcelable[size];
			}
	};

	
	
}
