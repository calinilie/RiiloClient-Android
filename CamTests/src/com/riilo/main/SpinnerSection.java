package com.riilo.main;

public class SpinnerSection {
	
	private String title;
	private int notifications;
	private int iconResId;
	private boolean showNotifications;
	
	public SpinnerSection(String title, int notifications, int iconResId, boolean showNotifications){
		this.title = title;
		this.notifications = notifications;
		this.iconResId = iconResId;
		this.showNotifications = showNotifications;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getNotifications() {
		return notifications;
	}
	public void setNotifications(int notifications) {
		this.notifications = notifications;
	}
	public int getIconResId() {
		return iconResId;
	}
	public void setIconResId(int iconRes) {
		this.iconResId = iconRes;
	}

	public boolean isShowNotifications() {
		return showNotifications;
	}

	public void setShowNotifications(boolean showNotifications) {
		this.showNotifications = showNotifications;
	}
	

}
