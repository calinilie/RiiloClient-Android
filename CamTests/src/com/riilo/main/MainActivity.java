package com.riilo.main;

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
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends BaseActivity implements OnNavigationListener{
	
    AppSectionsPagerAdapter appSectionsPagerAdapter;

    ViewPager viewPager;
    SpinnerAdapter spinnerAdapter;
    
    PullToRefreshAttacher pullToRefreshAttacher;
    private boolean wasTutorialRunThisSession = false;
    private IBackButtonListener backButtonListener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        wasTutorialRunThisSession = savedInstanceState!=null && savedInstanceState.getBoolean(StringKeys.WAS_TUTORIAL_RUN);
        boolean showTutorial = !Facade.getInstance(this).wasTutorialRun() && !wasTutorialRunThisSession;
        if (showTutorial){
        	wasTutorialRunThisSession = true;
        	Intent intent = new Intent(this, TutorialActivity.class);
        	startActivity(intent);
        }
        
        setContentView(R.layout.activity_main_layout);
        
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.riilo_sections,
                android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(spinnerAdapter, this);
        actionBar.setHomeButtonEnabled(false);
        
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
        return super.onCreateOptionsMenu(menu);
    }
    
    /*@Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
    	final MenuItem menuItem = menu.findItem(R.id.all_notifications);     
    	return super.onPrepareOptionsMenu(menu);
    }*/
    
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
}
