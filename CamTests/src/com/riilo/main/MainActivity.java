package com.riilo.main;

import java.util.ArrayList;
import java.util.List;

import com.riilo.main.R;
import com.riilo.interfaces.IBackButtonListener;
import com.riilo.tutorial.TutorialActivity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends BaseActivity implements ActionBar.TabListener{

	/**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;
    
    PullToRefreshAttacher pullToRefreshAttacher;
//    private ToLocationPostFragment toLocationPostFragment;
    private boolean wasTutorialRunThisSession = false;
    private IBackButtonListener backButtonListener;
    
    
    private List<Tab> tabs;

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
        
        LocationHistoryManager.getInstance(this).startService();
        
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
            	actionBar.setSelectedNavigationItem(position);
            }
        });
        tabs = new ArrayList<ActionBar.Tab>();
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
        	Tab tab =  actionBar.newTab()
                    .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this);
            actionBar.addTab(tab);
            tabs.add(tab);
        }
        
        //create pullToRefreshAttacher
        pullToRefreshAttacher = PullToRefreshAttacher.get(this);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putBoolean(StringKeys.WAS_TUTORIAL_RUN, wasTutorialRunThisSession);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        analytics.recordScreenHit_TabSelect(tab.getPosition());
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
    
    public List<Tab> getTabs(){
    	return tabs;
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
	
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
        	//Log.d(">>>>>>>>getItem", "getItem "+i);
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
            case 0:
            	return "Post";
            case 1:
                return "Latest";
            case 2:
            	return "Nearby";
            case 3:
            default:
            	return "Notifications: 0";
        	}
        }
    }
    
	@Override
	protected void setupWidgetsViewElements() {
		// TODO Auto-generated method stub
	}

	public PullToRefreshAttacher getPullToRefreshAttacher(){
		return this.pullToRefreshAttacher;
	}
}
