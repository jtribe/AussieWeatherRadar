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

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

public class SettingsPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.aussie_weather_radar_preferences);
		
		PreferenceCategory fileStoragePref = (PreferenceCategory) findPreference("file_storage_pref");
		CacheDialogPreference cachePref = new CacheDialogPreference(this, null);
		cachePref.setTitle("Clear Cache");
		cachePref.setKey("dialog_pref");
		cachePref.setSummary("Select to clear cached images");
		
		cachePref.setDialogTitle("Clear Cache");
		cachePref.setDialogMessage("Clear Now!");
		fileStoragePref.addPreference(cachePref);
	}

}
