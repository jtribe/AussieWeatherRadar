/**
 * Copyright (C) 2012 jTribe
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package au.com.jtribe.weatherradar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.TimeZone;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity (R.layout.radar)
@OptionsMenu(R.menu.home_menu)
public class AussieWeatherRadar extends Activity {
	public static final int REQUEST_CODE_PREF = 2;
	public static final int REQUEST_CODE_MAP = 1;

	public static final String TAG = "AussieWeatherRadar";
	public static final String FIRST_TIME = "first_time";
	public static final String STATION_NAME = "station_name";
	public static final String PRODUCT_CODE = "product_code";
	public static final String RADAR_LEVEL = "radar_level";

	public static final String BGROUND_IMG = "background_img";
	public static final String TOPO_IMG = "topo_img";
	public static final String LOC_IMG = "location_img";
	public static final String RANGE_IMG = "range_img";

	private static final int RADAR_64KM = Menu.FIRST;
	private static final int RADAR_128KM = Menu.FIRST + 1;
	private static final int RADAR_256KM = Menu.FIRST + 2;
	private static final int RADAR_512KM = Menu.FIRST + 3;

	private static SharedPreferences preferences;

	private String product_code_64km = "";
	private String product_code_128km = "";
	private String product_code_256km = "";
	private String product_code_512km = "";

	@ViewById (R.id.progress_text) TextView progressText;
	@ViewById (R.id.progress_bar) ProgressBar progressBar;
	@ViewById ViewFlipper animationVG;
	@ViewById RelativeLayout timeVG;

	@ViewById ImageView background;
	@ViewById ImageView topography;
	@ViewById ImageView location;
	@ViewById ImageView range;
				
	private Bundle changeBundle = null;

	@AfterViews
	void obtainStationPreferences() {

		preferences = getSharedPreferences("au.com.jtribe.weatherradar_preferences", Activity.MODE_PRIVATE);
		changeBundle = new Bundle();

		// have the user select a station on the map first if they
		// haven't done so previously
		if (preferences.getBoolean(AussieWeatherRadar.FIRST_TIME, true)) {
			Intent intentStationMap = new Intent(this, StationMap.class);
			startActivityForResult(intentStationMap, REQUEST_CODE_MAP);
		} else {
			parseDataAndSetLink();
			loadPrevMap();
		}
	}

	private void loadPrevMap() {
		if (preferences.getInt(RADAR_LEVEL, RADAR_256KM) == RADAR_256KM) {
			preferences.edit().putString(PRODUCT_CODE, product_code_256km).commit();
			setTitle(getString(R.string.app_name) + " 256 km");
			refreshCurrentConditions(product_code_256km);
		} else if (preferences.getInt(RADAR_LEVEL, RADAR_256KM) == RADAR_64KM) {
			if (product_code_64km.length() > 0) {
				preferences.edit().putString(PRODUCT_CODE, product_code_256km).commit();
				setTitle(getString(R.string.app_name) + " 64 km");
				refreshCurrentConditions(product_code_64km);
			} else {
				preferences.edit().putString(PRODUCT_CODE, product_code_256km).commit();
				setTitle(getString(R.string.app_name) + " 256 km");
				refreshCurrentConditions(product_code_256km);
			}
		} else if (preferences.getInt(RADAR_LEVEL, RADAR_256KM) == RADAR_128KM) {
			preferences.edit().putString(PRODUCT_CODE, product_code_256km).commit();
			setTitle(getString(R.string.app_name) + " 128 km");
			refreshCurrentConditions(product_code_128km);
		} else if (preferences.getInt(RADAR_LEVEL, RADAR_256KM) == RADAR_512KM) {
			preferences.edit().putString(PRODUCT_CODE, product_code_256km).commit();
			setTitle(getString(R.string.app_name) + " 512 km");
			refreshCurrentConditions(product_code_512km);
		}
	}

	/**
	 * open the list of stations from the csv file and load the product codes
	 * of the radar station selected in the preferences
	 */
	private void parseDataAndSetLink() {
		InputStream ipstream = this.getResources().openRawResource(R.raw.resultset);
		Scanner scanner = new Scanner(ipstream);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains(preferences.getString(STATION_NAME, "null"))) {
				line = line.replaceAll("\"", "");
				String[] parseString = line.split(",");
				product_code_64km = parseString[2];
				product_code_128km = parseString[3];
				product_code_256km = parseString[4];
				product_code_512km = parseString[5];
			}
		}
		scanner.close();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.radar).setTitle(preferences.getString(STATION_NAME, "") + " Zoom");
		menu.findItem(R.id.radar).setTitleCondensed("Zoom");
		menu.findItem(R.id.km64).setEnabled(product_code_64km.length() > 0);
		menu.findItem(R.id.km128).setEnabled(product_code_128km.length() > 0);
		menu.findItem(R.id.km256).setEnabled(product_code_256km.length() > 0);
		menu.findItem(R.id.km512).setEnabled(product_code_512km.length() > 0);
		return super.onPrepareOptionsMenu(menu);
	}
	
	private void quietlyStopRunnable() {
		try {
			if(bgLoaderRunnable != null) {
				bgLoaderRunnable.setStop(true);
			}
			if(aniRunnable != null) {
				aniRunnable.setStop(true);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error trying to stop runables", e);
		}
	}
	
	@OptionsItem
    void km64Selected() {
		quietlyStopRunnable();
		setTitle(getString(R.string.app_name) + " 64 km");
		preferences.edit().putString(PRODUCT_CODE, product_code_64km).commit();
		preferences.edit().putInt(RADAR_LEVEL, RADAR_64KM).commit();
		refreshCurrentConditions(product_code_64km);
    }
	
	@OptionsItem
    void km128Selected() {
		quietlyStopRunnable();
		setTitle(getString(R.string.app_name) + " 128 km");
		preferences.edit().putString(PRODUCT_CODE, product_code_128km).commit();
		preferences.edit().putInt(RADAR_LEVEL, RADAR_128KM).commit();
		refreshCurrentConditions(product_code_128km);
    }
	
	@OptionsItem
    void km256Selected() {
		quietlyStopRunnable();
		setTitle(getString(R.string.app_name) + " 256 km");
		preferences.edit().putString(PRODUCT_CODE, product_code_256km).commit();
		preferences.edit().putInt(RADAR_LEVEL, RADAR_256KM).commit();
		refreshCurrentConditions(product_code_256km);
    }
	
	@OptionsItem
    void km512Selected() {
		quietlyStopRunnable();
		this.setTitle(getString(R.string.app_name) + " 512 km");
		preferences.edit().putString(PRODUCT_CODE, product_code_512km).commit();
		preferences.edit().putInt(RADAR_LEVEL, RADAR_512KM).commit();
		refreshCurrentConditions(product_code_512km);
    }
	
	@OptionsItem
    void mapSelected() {
		quietlyStopRunnable();
		Intent intentStationMap = new Intent(this, StationMap.class);
		startActivityForResult(intentStationMap, REQUEST_CODE_MAP);
    }
	
	@OptionsItem
    void settingsSelected() {
		Intent intentSettings = new Intent(this, SettingsPreferenceActivity.class);
		changeBundle.putBoolean("topo_pref", preferences.getBoolean("topo_pref", true));
		changeBundle.putBoolean("location_pref", preferences.getBoolean("location_pref", true));
		changeBundle.putBoolean("range_pref", preferences.getBoolean("range_pref", true));
		startActivityForResult(intentSettings, REQUEST_CODE_PREF);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK) {
			if (preferences.getBoolean(AussieWeatherRadar.FIRST_TIME, true)) {
				preferences.edit().putBoolean(AussieWeatherRadar.FIRST_TIME, false).commit();
				CacheDialogPreference.deleteCache(Dialog.BUTTON1);
			}
			preferences.edit().putString(STATION_NAME, data.getExtras().getString(STATION_NAME)).commit();
			parseDataAndSetLink();
			loadPrevMap();
		} else if (requestCode == REQUEST_CODE_PREF) {
			boolean reload = false;
			if (changeBundle.getBoolean("topo_pref") != preferences.getBoolean("topo_pref", true)) {
				if (preferences.getBoolean("topo_pref", true)) {
					reload = true;
				} else {
					topography.setBackgroundDrawable(null);
				}
			}
			if (changeBundle.getBoolean("location_pref") != preferences.getBoolean("location_pref", true)) {
				if (preferences.getBoolean("location_pref", true)) {
					reload = true;
				} else {
					location.setBackgroundDrawable(null);
				}
			}
			if (changeBundle.getBoolean("range_pref") != preferences.getBoolean("range_pref", true)) {
				if (preferences.getBoolean("range_pref", true)) {
					reload = true;
				} else {
					range.setBackgroundDrawable(null);
				}
			}
			if (reload)
				loadPrevMap();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void hideProgressViews() {
		setProgessVisibility(View.GONE);
	}
	
	private void showProgressViews() {
		setProgessVisibility(View.VISIBLE);
	}
	
	private void setProgessVisibility(int visibility) {
		this.progressText.setVisibility(visibility);
		this.progressBar.setVisibility(visibility);
	}

	private void refreshCurrentConditions(final String productCode) {
		showProgressViews();
		this.animationVG.removeAllViews();
		this.timeVG.removeAllViews();
		
		final Handler toastHandler = new Handler() {

			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				hideProgressViews();

				background.setImageBitmap( (Bitmap) msg.getData().getParcelable(BGROUND_IMG));

				if (preferences.getBoolean("topo_pref", true)) {
					topography.setImageBitmap((Bitmap) msg.getData().getParcelable(TOPO_IMG));
				}

				if (preferences.getBoolean("location_pref", true)) {
					location.setImageBitmap((Bitmap) msg.getData().getParcelable(LOC_IMG));
				}

				if (preferences.getBoolean("range_pref", true)) {
					range.setImageBitmap((Bitmap) msg.getData().getParcelable(RANGE_IMG));
				}
				startAnimation(productCode);
			}
		};
		bgLoaderRunnable = new BgImageLoaderRunnable(productCode, toastHandler, preferences);
		new Thread(bgLoaderRunnable).start();
	}

	private BgImageLoaderRunnable bgLoaderRunnable = null;

	private void startAnimation(final String productCode) {
		showProgressViews();

		final Handler toastHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				animationVG.removeAllViews();
				timeVG.removeAllViews();

				ArrayList<String> animationLink = msg.getData().getStringArrayList("aniLink");

				View lastView = null;
				for (int i = 0; i < animationLink.size(); i++) {
					Bitmap original = (Bitmap) msg.getData().getParcelable(String.valueOf(i));
					String line = animationLink.get(i);				
					lastView = createFrame(original, line);
					animationVG.addView(lastView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				}
				
				Bitmap original = (Bitmap) msg.getData().getParcelable(String.valueOf(animationLink.size() - 1));
				String line = animationLink.get(animationLink.size() - 1);	

				animationVG.addView(createDuplicateFrame(original, line, lastView), LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				animationVG.addView(createDuplicateFrame(original, line, lastView), LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				
				animationVG.startFlipping();
				
				hideProgressViews();
			}
		};
		aniRunnable = new AnimationRunnable(productCode, toastHandler);
		new Thread(aniRunnable).start();
	}

	private View createDuplicateFrame(Bitmap original, String line, View from) {
		View frame = createFrame(original, line);
		
		TextView fromView = (TextView) from.findViewWithTag(line);
		TextView toView = (TextView) frame.findViewWithTag(line);
		
		toView.setText(fromView.getText());
		
		return frame;
	}
	
	private View createFrame(Bitmap original, String line) {
		RelativeLayout viewGroup = new RelativeLayout(AussieWeatherRadar.this);
		
		TextView tv = createTextView(line);				
		ImageView iv = new ImageView(AussieWeatherRadar.this);
		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		iv.setImageBitmap(original);
		
		viewGroup.addView(iv, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 22, 0, 0);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		viewGroup.addView(tv, params);
		
		return viewGroup;
	}
	
	private TextView createTextView(String line) {
		TextView tv = new TextView(this);
		tv.setBackgroundColor(1711276032);
		tv.setTextColor(Color.WHITE);
		tv.setPadding(5, 5, 5, 5);
		tv.setText("No Data");
		tv.setTag(line);

		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		long currMillis = cal.getTime().getTime();

		line = line.substring(40, line.indexOf(".png"));
		int mapYear = Integer.valueOf(line.substring(0, 4));
		int mapMonth = Integer.valueOf(line.substring(4, 6));
		int mapDay = Integer.valueOf(line.substring(6, 8));
		int mapHour = Integer.valueOf(line.substring(8, 10));
		int mapMinutes = Integer.valueOf(line.substring(10, 12));

		Calendar mapTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		mapTime.set(mapYear, mapMonth - 1, mapDay, mapHour, mapMinutes);
		long mapMillis = mapTime.getTime().getTime();

		long dif = currMillis - mapMillis;
		dif = dif / 1000 / 60;
		tv.setText(dif + " mins ago");
		
		return tv;
	}
	
	private AnimationRunnable aniRunnable = null;
}
