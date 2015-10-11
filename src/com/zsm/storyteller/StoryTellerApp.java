package com.zsm.storyteller;

import com.zsm.driver.android.log.LogInstaller;
import com.zsm.driver.android.log.LogPreferences;

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
	}
}
