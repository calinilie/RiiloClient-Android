package com.example.camtests;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.example.camtests.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Z_Dep_TakePhotoActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener {

	public final static String TAG = Z_Dep_TakePhotoActivity.class.getName();

	public static final String CURRENT_LOCATION_LAT_KEY = "org.calin.camtests.CurrentLocationLatitude";
	public static final String CURRENT_LOCATION_LONG_KEY = "org.calin.camtests.CurrentLocationLongitude";

	// public static final String NEARBY_POIS = "org.calin.camtests.NearbyPOIs";

	// Update frequency in milliseconds
	private static final int UPDATE_INTERVAL = 10000;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = 5000;

	Bundle savedInstanceState;

	ProgressBar progressBar;
	Button buttonTakePicture;
	Button buttonGetLocation;
	Button buttonNavigateMap;
	ListView postsListView;

	// Facade dataFacade;
	// PostsCache postsCache;
	private boolean distancesComputed;

	private LocationHistory lastKnownLocation;

	PostListItemAdapter itemsAdapter;
	List<Post> items = new ArrayList<Post>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.savedInstanceState = savedInstanceState;

		setContentView(R.layout.z_dep_activity_take_photo_action);

		deviceId = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);

		buttonTakePicture = ((Button) findViewById(R.id.button_feed));
		buttonTakePicture.setOnClickListener(this);

		buttonGetLocation = (Button) findViewById(R.id.button_get_location);
		buttonGetLocation.setOnClickListener(this);

		buttonNavigateMap = (Button) findViewById(R.id.button_navigate_map);
		buttonNavigateMap.setOnClickListener(this);

		progressBar = (ProgressBar) findViewById(R.id.loading);

		postsListView = (ListView) findViewById(R.id.posts_list_view);
		postsListView.setOnItemClickListener(this);

		items = PostsCache.getInstance(this).getPostsAsList();
		itemsAdapter = new PostListItemAdapter(this,
				R.layout.post_list_view_item_layout, items, deviceId, true);
		postsListView.setAdapter(itemsAdapter);

		lastKnownLocation = Facade.getInstance(this).getLastKnownLocation();
		super.initLocationClient(
				LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
				UPDATE_INTERVAL, FASTEST_INTERVAL);

//		PostsCache.getInstance(this).getLatestPosts(itemsAdapter, items);
//		PostsCache.getInstance(this).getNotifications(deviceId, null, null,
//				buttonTakePicture, false,
//				StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_view_layout_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		/*switch (requestCode) {
		case StringKeys.TAKE_PICTURE_REQUEST_CODE: {
			Uri uri = super.onTakePhotoActivityResult(requestCode, resultCode,
					intent);
			if (uri != null) {
				String filePath = Helpers.uriToFilePath(uri);
				File file = new File(filePath);
				if (!file.exists())
					throw new RuntimeException("file does not exist!");
			}
			break;
		}
		}*/
	}

	@Override
	public void onStart() {
		super.onStart();
		setupWidgetsViewElements();
		Helpers.renewList(items, PostsCache.getInstance(this).getPostsAsList());
		Log.d("<<<<<<<<<TakePhotoActivity.onStart>>>>>>>>>",
				"No of posts in cahce: "
						+ PostsCache.getInstance(this).getPostsAsList().size());
		itemsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.button_feed:
			/*
			 * if (savedInstanceState == null) { if
			 * (Environment.getExternalStorageState
			 * ().equals(Environment.MEDIA_MOUNTED)) { startCameraIntent();
			 * prepareLoading(); } else {
			 * super.showWarningDialog(getString(R.string
			 * .error_sd_card_not_mounted)); } }
			 */
			intent = new Intent(this, PostsNotificationsFragment.class);
			startActivity(intent);
			break;
		case R.id.button_get_location:
			/*
			 * location = locationClient.getLastLocation(); Intent intent = new
			 * Intent(this, WorkerService.class); startService(intent);
			 * 
			 * if (location!=null) Toast.makeText(this,
			 * location.getLatitude()+" "
			 * +location.getLongitude()+" "+location.getAccuracy(),
			 * Toast.LENGTH_SHORT).show(); else Toast.makeText(this,
			 * "location is NULL!", Toast.LENGTH_SHORT).show();
			 */

			intent = new Intent(this, PostsNearbyFragment.class);
			startActivity(intent);

			break;
		case R.id.button_navigate_map:
			startMapIntent();
			break;
		}

	}

	private void startMapIntent() {
		Intent intent = new Intent(this, ToLocationPostFragment.class);
		// intent.putExtra(CURRENT_LOCATION_LAT_KEY,
		// mCurrentLocation.getLatitude());
		// intent.putExtra(CURRENT_LOCATION_LONG_KEY,
		// mCurrentLocation.getLongitude());
		startActivity(intent);
	}

	private void prepareLoading() {
		progressBar.setVisibility(View.VISIBLE);
		buttonTakePicture.setVisibility(View.INVISIBLE);
		buttonGetLocation.setVisibility(View.INVISIBLE);
		postsListView.setVisibility(View.INVISIBLE);
	}

	private void stopLoading() {
		progressBar.setVisibility(View.INVISIBLE);
		buttonTakePicture.setVisibility(View.VISIBLE);
		buttonGetLocation.setVisibility(View.VISIBLE);
		postsListView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void setupWidgetsViewElements() {
		int length = PostsCache.getInstance(this).getNotifications().size();
		if (length > 0) {
			buttonTakePicture.setText("Feed" + String.format(" (%s)", length));
		} else {
			buttonTakePicture.setText("Feed");
		}
	}

	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		double[] latLong;
		// Log.d("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", "location Changed!" +
		// distancesComputed);
		if (!distancesComputed) {
			lastKnownLocation = Facade.getInstance(this).getLastKnownLocation();
			// location = null;
			if (location != null) {
				for (Post p : items) {
					p.setDistanceFromCurLoc(location);
				}
				distancesComputed = true;
			} else {
				for (Post p : items) {
					p.setDistanceFromLastKnownLocation(lastKnownLocation);
				}
			}
			if (Facade.getInstance(this).insertLocationToHistoryIfNeeded(
					location, lastKnownLocation)) {
				// TODO change logic in method above
			}
			latLong = Helpers.setReqFrom_Latitude_and_Longitude(location,
					lastKnownLocation);
//			PostsCache.getInstance(this).getNearbyPosts(latLong[0], latLong[1],
//					null, null, buttonGetLocation, false,
//					StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW);
			itemsAdapter.notifyDataSetChanged();
		}/*
		 * else{ latLong = Helpers.setReqFrom_Latitude_and_Longitude(location,
		 * lastKnownLocation);
		 * PostsCache.getInstance(this).getNearbyPosts(latLong[0], latLong[1],
		 * null, null, buttonGetLocation, false,
		 * StringKeys.POST_RESULT_RECEIVER_CODE_UPDATE_VIEW); }
		 */
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position,
			long index) {
		Post post = items.get((int) index);
		Intent postViewIntent = new Intent(this, PostViewActivity.class);
		postViewIntent.putExtra(StringKeys.POST_BUNDLE, post.toBundle());
		startActivity(postViewIntent);
	}
}