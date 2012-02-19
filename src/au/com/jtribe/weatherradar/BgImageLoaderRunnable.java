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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BgImageLoaderRunnable implements Runnable {
	private boolean stop = false;
	private SharedPreferences preferences;
	private String productCode = "";
	private Handler toastHandler = null;

	private static final String bomUrl = "http://mirror.bom.gov.au/products/radar_transparencies/";
	private String backgroundUrl = "";
	private String topoUrl = "";
	private String locationUrl = "";
	private String rangeUrl = "";

	public BgImageLoaderRunnable(String productCode, Handler toastHandler, SharedPreferences preferences) {
		this.preferences = preferences;
		this.productCode = productCode;
		this.toastHandler = toastHandler;

		backgroundUrl = bomUrl + productCode + ".background.png";
		topoUrl = bomUrl + productCode + ".topography.png";
		locationUrl = bomUrl + productCode + ".locations.png";
		rangeUrl = bomUrl + productCode + ".range.png";
	}

	@Override
	public void run() {
		getImage(backgroundUrl, AussieWeatherRadar.BGROUND_IMG);
		if (preferences.getBoolean("topo_pref", true) && !stop)
			getImage(topoUrl, AussieWeatherRadar.TOPO_IMG);
		if (preferences.getBoolean("location_pref", true) && !stop)
			getImage(locationUrl, AussieWeatherRadar.LOC_IMG);
		if (preferences.getBoolean("range_pref", true) && !stop)
			getImage(rangeUrl, AussieWeatherRadar.RANGE_IMG);

		try {
			if (!stop) {
				Message message = new Message();
				message.setData(data);
				toastHandler.sendMessage(message);
			}
		} catch (Exception e) {
			Log.e(AussieWeatherRadar.TAG, "Error calling toastHandler", e);
		}
	}

	Bundle data = new Bundle();

	private void getImage(String url, String key) {
		boolean error = false;
		try {
			if (preferences.getBoolean("cache_pref", true)  && !stop) {
				File tempFileDir = new File(Environment.getExternalStorageDirectory() + "/AussieWeatherRadar");
				tempFileDir.mkdirs();
				File tempFile = new File(tempFileDir, productCode + key);

				if (!tempFile.exists() && tempFile.length() == 0) {
					tempFile.createNewFile();

					OutputStream outStream = new FileOutputStream(tempFile);

					URL imageURL = new URL(url);
					InputStream inStream = imageURL.openStream();

					byte[] buffer = new byte[10000];
					int read = 0;
					while (true && !stop) {
						read = inStream.read(buffer);
						if (read == -1) {
							break;
						}
						outStream.write(buffer, 0, read);
					}
					if(stop)
						tempFile.delete();
					inStream.close();
					outStream.close();
					Log.d(AussieWeatherRadar.TAG, "Cached images from Web");
				}
				InputStream inStream = new FileInputStream(tempFile);
				Bitmap bitmap = BitmapFactory.decodeStream(inStream);
				data.putParcelable(key, bitmap);
				inStream.close();
			}
		} catch (Exception e) {
			error = true;
			Log.e(AussieWeatherRadar.TAG, "Error getting from Cache, trying get images from Web", e);
		}

		if (error && !stop) {
			try {
				Bitmap bitmap = downloadImage(url);
				int i = 0;
				while((!(bitmap instanceof Bitmap) && !stop) || i < 3) {
					i++;
					bitmap = downloadImage(url);
				}
				data.putParcelable(key, bitmap);
				error = false;
			} catch (Exception e) {
				Log.e(AussieWeatherRadar.TAG, "Error getting from Web", e);
			}
		}
	}

	private Bitmap downloadImage(String url) throws MalformedURLException, IOException {
		URL imageURL = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) imageURL.openConnection();
		conn.setDoInput(true);
		conn.connect();
		conn.getContentLength();
		InputStream inStream = conn.getInputStream();
		Bitmap bitmap = BitmapFactory.decodeStream(inStream);
		conn.disconnect();
		return bitmap;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
}
