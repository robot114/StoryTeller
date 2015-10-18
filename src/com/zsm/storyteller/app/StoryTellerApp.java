package com.zsm.storyteller.app;

import com.zsm.driver.android.log.LogInstaller;
import com.zsm.driver.android.log.LogPreferences;
import com.zsm.storyteller.preferences.Preferences;

import android.app.Application;

public class StoryTellerApp extends Application {

	public StoryTellerApp() {
		super();
		LogInstaller.installAndroidLog( "StoryTeller" );
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogPreferences.init( this );
		LogInstaller.installFileLog( this );
		
		Preferences.init( this );
	}
}
