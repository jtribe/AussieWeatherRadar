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
import java.util.Scanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MyOverlay extends ItemizedOverlay<OverlayItem> {
	private Activity activity;
	private static ArrayList<OverlayItem> pinList;

	public MyOverlay(Activity context, Drawable defaultPin) {
		super(defaultPin);
		this.activity = context;
		pinList = new ArrayList<OverlayItem>();

		boundCenter(defaultPin);

		try {
			InputStream ipstream = activity.getResources().openRawResource(R.raw.resultset);
			Scanner scanner = new Scanner(ipstream);
			scanner.nextLine();
			while (scanner.hasNextLine()) {
				parseLineAndAddPin(scanner.nextLine());
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		populate();
	}

	private void parseLineAndAddPin(String line) {
		line = line.replaceAll("\"", "");
		String[] parseString = line.split(",");
		double latitude = Double.valueOf(parseString[6]) * 1000000;
		double longitude = Double.valueOf(parseString[7]) * 1000000;
		GeoPoint gp = new GeoPoint((int) latitude, (int) longitude);
		pinList.add(new OverlayItem(gp, parseString[1], ""));

	}

	@Override
	protected OverlayItem createItem(int i) {
		return pinList.get(i);
	}

	@Override
	public int size() {
		return pinList.size();
	}

	@Override
	protected boolean onTap(int index) {
		Toast.makeText(activity, getItem(index).getTitle(), Toast.LENGTH_SHORT).show();
		Intent intentResult = new Intent();
		intentResult.putExtra(AussieWeatherRadar.STATION_NAME, getItem(index).getTitle());
		activity.setResult(Activity.RESULT_OK, intentResult);
		activity.finish();
		return super.onTap(index);
	}
}
