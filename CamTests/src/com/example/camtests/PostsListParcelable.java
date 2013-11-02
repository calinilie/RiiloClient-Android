package com.example.camtests;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (postsList==null)
			throw new RuntimeException("PostsListParcelable.postsList is null!!");
		
		int length = postsList.size();
//		if (length==0)
//			throw new RuntimeException("PostsListParcelable.postsList is empty!!");
		
//		Bundle[] postsBundleArray = new Bundle[length];
//		for (int i=0; i<length; i++) {
//			postsBundleArray[i] = postsList.get(i).toBundle();
//		}
		Log.d(">>>>>>>>>>>>>>>>>>>>>>>>", "list size:" + postsList.size());
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
