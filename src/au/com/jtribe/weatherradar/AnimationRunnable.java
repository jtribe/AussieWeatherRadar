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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AnimationRunnable implements Runnable {
	public static final String TAG = "AussieWeatherRadar";
	private boolean stop = false;
	private String productCode = "";
	ArrayList<String> animationLink = new ArrayList<String>();
	private Handler toastHandler = null;

	public AnimationRunnable(String productCode, Handler toastHandler) {
		this.productCode = productCode;
		this.toastHandler = toastHandler;
	}

	public void run() {
		try {
			String url = "http://mirror.bom.gov.au/products/" + productCode + ".loop.shtml";
			URL imgUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
			conn.setDoInput(true);
			conn.connect();
			conn.getContentLength();
			InputStream is = conn.getInputStream();
			Scanner scanner = new Scanner(is);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (stop)
					break;
				else if (line.contains("theImageNames[")) {
					line = line.substring(line.indexOf("/radar/"));
					line = line.substring(0, line.indexOf(".png\"") + 4);
					line = "http://mirror.bom.gov.au" + line;
					animationLink.add(line);
				} else if (line.contains("nImages = ")) {
					break;
				}
			}
			conn.disconnect();
		} catch (IOException e) {
			Log.e(TAG, "Error getting animation image link", e);
		}
		Message message = new Message();
		Bundle data = new Bundle();
		try {
			for (int i = 0; i < animationLink.size(); i++) {
				if (stop)
					break;
				else {
					URL aniImgUrl = new URL(animationLink.get(i));
					HttpURLConnection conn = (HttpURLConnection) aniImgUrl.openConnection();
					conn.setDoInput(true);
					conn.connect();
					conn.getContentLength();
					InputStream is = conn.getInputStream();
					Bitmap animationImg = BitmapFactory.decodeStream(is);
					data.putParcelable(String.valueOf(i), animationImg);
					message.setData(data);
					conn.disconnect();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error getting animation image", e);
		}
		try {
			if (!stop) {
				data.putStringArrayList("aniLink", animationLink);
				message.setData(data);
				toastHandler.sendMessage(message);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error calling toastHandler", e);
		}
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
}