package com.riilo.main;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class LocationHistoryParcelable implements Parcelable{

	private List<LocationHistory> list;
	
	public LocationHistoryParcelable(List<LocationHistory> list){
		this.list = list;
	}
	
	public LocationHistoryParcelable(Parcel parcel){
		list = new ArrayList<LocationHistory>();
		parcel.readList(list, getClass().getClassLoader());
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (list==null)
			throw new RuntimeException("PostsListParcelable.postsList is null!!");
		
		dest.writeList(list);
	}
	
	public List<LocationHistory> getLocationHistory(){
		return this.list;
	}

	public static final Parcelable.Creator<LocationHistoryParcelable> CREATOR
    	= new Parcelable.Creator<LocationHistoryParcelable>() {
	
		public LocationHistoryParcelable createFromParcel(Parcel in) {
		    return new LocationHistoryParcelable(in);
		}
		
		public LocationHistoryParcelable[] newArray(int size) {
		    return new LocationHistoryParcelable[size];
		}
	};
}
