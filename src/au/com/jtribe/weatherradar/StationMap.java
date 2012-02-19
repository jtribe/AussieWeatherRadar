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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class StationMap extends MapActivity {
	public static final String TAG = "AussieWeatherRadar";
	private MapView map;
	private MyOverlay myOverlay;

	private LocationManager locManagerNetwork = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		locManagerNetwork = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locManagerNetwork.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, onLocationChangeNetwork);

		map = (MapView) findViewById(R.id.map);
		map.setBuiltInZoomControls(true);

		Drawable pin = getResources().getDrawable(R.drawable.pin);
		pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());

		myOverlay = new MyOverlay(this, pin);
		map.getOverlays().add(myOverlay);

		// zoom to Australia for debugging on emulator
		// map.getController().animateTo(new GeoPoint(-25274398, 133775136));
		map.getController().setZoom(6);
	}

	private LocationListener onLocationChangeNetwork = new LocationListener() {
		@Override
		public void onLocationChanged(Location loc) {
			locManagerNetwork.removeUpdates(onLocationChangeNetwork);
			map.getController().animateTo(getGeoPoint(loc.getLatitude(), loc.getLongitude()));
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private GeoPoint getGeoPoint(double latitude, double longitude) {
		return (new GeoPoint((int) (latitude * 1000000), (int) (longitude * 1000000)));
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}