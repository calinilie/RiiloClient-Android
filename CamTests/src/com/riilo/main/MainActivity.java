package com.riilo.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.location.LocationRequest;
import com.riilo.main.R;
import com.riilo.interfaces.IBackButtonListener;
import com.riilo.tutorial.TutorialActivity;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends BaseActivity implements OnNavigationListener{
	
    AppSectionsPagerAdapter appSectionsPagerAdapter;

    ViewPager viewPager;
//    SpinnerAdapter spinnerAdapter;
    SpinnerSectionItemAdapter spinnerAdapter;
    
    PullToRefreshAttacher pullToRefreshAttacher;
    private boolean wasTutorialRunThisSession = false;
    private IBackButtonListener backButtonListener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        
        wasTutorialRunThisSession = savedInstanceState!=null && savedInstanceState.getBoolean(StringKeys.WAS_TUTORIAL_RUN);
        boolean showTutorial = !Facade.getInstance(this).wasTutorialRun() && !wasTutorialRunThisSession;
        if (showTutorial){
        	wasTutorialRunThisSession = true;
        	Intent intent = new Intent(this, TutorialActivity.class);
        	startActivity(intent);
        }
        
        setContentView(R.layout.activity_main_layout);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
//        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.riilo_sections,
//                android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter = new SpinnerSectionItemAdapter(this, R.layout.spinner_section_item_layout, sections);
        actionBar.setListNavigationCallbacks(spinnerAdapter, this);

        
        appSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(appSectionsPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	actionBar.setSelectedNavigationItem(position);
            }
        });        
        
        
        pullToRefreshAttacher = PullToRefreshAttacher.get(this);
        initLocationClient(LocationRequest.PRIORITY_LOW_POWER, 2000, 1000);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putBoolean(StringKeys.WAS_TUTORIAL_RUN, wasTutorialRunThisSession);
        super.onSaveInstanceState(savedInstanceState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_layout_menu, menu);
//		MenuItem item = menu.findItem(R.id.all_notifications);
//		((TextView) item.getActionView().findViewById(R.id.notifications_number)).setText("got you fucker!");
        return super.onCreateOptionsMenu(menu);
    }
    
    public SpinnerAdapter getSpinner(){
    	return spinnerAdapter;
    }

    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                	ToLocationPostFragment toLocationPostFragment = new ToLocationPostFragment();
                	addLocationListener(toLocationPostFragment);
                	backButtonListener = toLocationPostFragment;
                	return toLocationPostFragment;
                case 1:
                	PostsLatestFragment latestPostsFragment = new PostsLatestFragment();
                	addLocationListener(latestPostsFragment);
                	latestPostsFragment.setHasOptionsMenu(true);
                	return latestPostsFragment;
                case 2:
                	PostsNearbyFragment nearbyPostsFragment = new PostsNearbyFragment();
                	addLocationListener(nearbyPostsFragment);
                    return nearbyPostsFragment;
                case 3:
                default:
                	PostsNotificationsFragment notificationsPostsFragment = new PostsNotificationsFragment();
                	return notificationsPostsFragment;
                	
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	switch (position) {
            /*case 0:
            	return "Post";
            case 1:
                return "Latest";
            case 2:
            	return "Nearby";
            case 3:*/
            default:
            	return "Notifications: 0";
        	}
        }
    }
    
	@Override
	protected void setupWidgetsViewElements() {
	}

	public PullToRefreshAttacher getPullToRefreshAttacher(){
		return this.pullToRefreshAttacher;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		viewPager.setCurrentItem(position);
		return true;
	}
	
	private static final List<SpinnerSection> sections = new ArrayList<SpinnerSection>(
			Arrays.asList(
				new SpinnerSection("Post Something", 0, R.drawable.riilo_logo, false),
				new SpinnerSection("Latest", 0, R.drawable.ic_latest_posts, false),
				new SpinnerSection("Nearby", 3, R.drawable.ic_nearby_posts, true),
				new SpinnerSection("Notifications", 10, R.drawable.ic_map_marker_human, true)
			)
		);
}
