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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

public class CacheDialogPreference extends DialogPreference {

	public CacheDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CacheDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		deleteCache(which);
		super.onClick(dialog, which);
	}

	public final static void deleteCache(int which) {
		if (which == Dialog.BUTTON1) {
			try {
				File tempFileDir = new File(Environment.getExternalStorageDirectory() + "/AussieWeatherRadar");
				String[] filesToDelete = tempFileDir.list();
				File toDelete;
				if (filesToDelete != null) {
					for (int i = 0; i < filesToDelete.length; i++) {
						toDelete = new File(tempFileDir, filesToDelete[i]);
						toDelete.delete();
					}
				}
				tempFileDir.delete();
			} catch (Exception e) {
				Log.e(AussieWeatherRadar.TAG, "Error while try to delete cache folder", e);
			}
		}
	}
}
