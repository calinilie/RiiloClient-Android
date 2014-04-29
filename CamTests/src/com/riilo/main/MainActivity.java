package com.riilo.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh.SetupWizard;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

import com.google.android.gms.location.LocationRequest;
import com.riilo.main.R;
import com.riilo.interfaces.FragmentBase;
import com.riilo.interfaces.IBackButtonListener;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends BaseActivity implements OnNavigationListener{
	
    AppSectionsPagerAdapter appSectionsPagerAdapter;

    ViewPager viewPager;
    SpinnerSectionItemAdapter spinnerAdapter;
    
    private IBackButtonListener backButtonListener;
    private int animationType;
    
    private PullToRefreshLayout pullToRefreshLayout;
    private SetupWizard setupWizard;
    
    private List<SpinnerSection> sections;

    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);        
        setupSpinnerSections();
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        
        setContentView(R.layout.activity_main_layout);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        spinnerAdapter = new SpinnerSectionItemAdapter(this, R.layout.spinner_section_item_layout, sections);
        actionBar.setListNavigationCallbacks(spinnerAdapter, this);

        appSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        appSectionsPagerAdapter.setActionBar(actionBar);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(appSectionsPagerAdapter);
        viewPager.setOnPageChangeListener(appSectionsPagerAdapter);
        
        //=================================================
        boolean showNotifications = getIntent().getBooleanExtra(StringKeys.SHOW_NOTIFICATIONS_TAB_FIRST, false);
        
        if (showNotifications)
        	actionBar.setSelectedNavigationItem(3);
        //=================================================
        
        pullToRefreshLayout =  (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        
 		setupWizard = ActionBarPullToRefresh.from(this)
 			.allChildrenArePullable();
		setupWizard.setup(pullToRefreshLayout);
        
        initLocationClient(LocationRequest.PRIORITY_LOW_POWER, 2000, 1000);
        
		LocationHistoryManager.getInstance(this).getLocationHistory(null, null);
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	if (this.animationType == StringKeys.ANIMATION_TYPE_SLIDE_IN_RIGHT)
    		this.overridePendingTransition(R.anim.fade_in, R.anim.activity_slideout_to_right);
    	if (this.animationType == StringKeys.ANIMATION_TYPE_SLIDE_IN_BOTTOM)
    		this.overridePendingTransition(R.anim.fade_in, R.anim.activity_slideout_to_bottom);
    		
    }
    
    @Override
    public void onBackPressed(){
    	if (backButtonListener!=null){
    		if (backButtonListener.onBackPressed())
    			super.onBackPressed();
    	}
    	else {
    		super.onBackPressed();
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_activity_layout_menu, menu);
//		MenuItem item = menu.findItem(R.id.all_notifications);
//		((TextView) item.getActionView().findViewById(R.id.notifications_number)).setText("got you!");
    	return super.onCreateOptionsMenu(menu);
    }
    
    public SpinnerSectionItemAdapter getSpinnerAdapter(){
    	return spinnerAdapter;
    }

    public class AppSectionsPagerAdapter extends FragmentPagerAdapter implements OnPageChangeListener{

    	private FragmentBase[] fragments;
    	private ActionBar actionBar;
    	
        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new FragmentBase[3];
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
            	case 0:
            		ExploreFragment exploreFragment = new ExploreFragment();
            		addLocationListener(exploreFragment);
            		return exploreFragment;
                case 1:
                	FragmentBase latestPostsFragment = new PostsLatestFragment();
                	addLocationListener((PostsLatestFragment)latestPostsFragment);
                	fragments[0] = latestPostsFragment;
                	return latestPostsFragment;
                case 2:
                	FragmentBase nearbyPostsFragment = new PostsNearbyFragment();
                	addLocationListener((PostsNearbyFragment)nearbyPostsFragment);
                	fragments[1] = nearbyPostsFragment;
                    return nearbyPostsFragment;
                case 3:
                default:
                	FragmentBase notificationsPostsFragment = new PostsNotificationsFragment();
                	fragments[2] = notificationsPostsFragment;
                	return notificationsPostsFragment;
            }
        }
        
        public void setActionBar(ActionBar bar){
        	this.actionBar = bar;
        }

        @Override
        public int getCount() {
            return 4;
        }

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			actionBar.setSelectedNavigationItem(position);
			if (position==0)
				return;
			
			position--;
			boolean canSetRefreshing = !this.isPTRRefreshing(position);
			
			try{
				fragments[position].isSelected(canSetRefreshing);
			}
			catch(Exception e){
				
			}
		}
		
		private boolean isPTRRefreshing(int skipIndex){
			boolean retVal = false;
			for(int i=0; i<fragments.length; i++){
				if (i!=skipIndex && fragments[i] != null){
					retVal |= fragments[i].isPTRRefreshing();
				}
			}
			return retVal;
		}
    }
    
	@Override
	protected void setupWidgetsViewElements() {
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_reply:
			analytics.recordEvent_General_ReplyButtonClicked();
			Intent intent = new Intent(this, ToLocationPostActivity.class);
			startActivity(intent);
			this.setAnimationType(StringKeys.ANIMATION_TYPE_SLIDE_IN_BOTTOM);
			return true;
		default:
            return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		viewPager.setCurrentItem(position);
		return true;
	}
	
	private void setupSpinnerSections(){
		if (sections==null){
			sections = new ArrayList<SpinnerSection>(
				Arrays.asList(
//						new SpinnerSection(0, getString(R.string.section_post), R.drawable.riilo_logo, false),
						new SpinnerSection(0, "Explore", R.drawable.riilo_logo, false),
						new SpinnerSection(1, getString(R.string.section_latest), R.drawable.ic_latest_posts, false),
						new SpinnerSection(2, getString(R.string.section_nearby), R.drawable.ic_nearby_posts, true),
						new SpinnerSection(3, getString(R.string.section_notifications), R.drawable.ic_map_marker_human, true)
					));
		}
	}
	
	public void setAnimationType(int animationType){
		this.animationType = animationType;
	}
}
