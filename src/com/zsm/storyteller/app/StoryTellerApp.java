package com.zsm.storyteller.app;

import android.app.Application;
import android.content.Intent;

import com.zsm.driver.android.log.LogInstaller;
import com.zsm.driver.android.log.LogPreferences;
import com.zsm.log.Log;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayFileHandler;
import com.zsm.storyteller.play.PlayService;
import com.zsm.storyteller.preferences.Preferences;

public class StoryTellerApp extends Application {

	public final static String[] EXTENSION = {
		".3gp", ".aac", ".flac", ".m4a", ".mp4", ".mid", ".mp3", ".xmf",
		".mxmf", ".rtx", ".rtttl", ".ota", ".imy", ".ogg", ".mkv", ".wav"
	};

	private PlayFileHandler playFileHandler;
	
	public StoryTellerApp() {
		LogInstaller.installAndroidLog( "StoryTeller" );
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogPreferences.init( this );
		LogInstaller.installFileLog( this );
		Log.setGlobalLevel( Log.LEVEL.DEBUG );
		
		Preferences.init( this );
		
		startPlayService();
		
		playFileHandler = new PlayFileHandler(this);
	}
	
	private void startPlayService() {
		Intent intent = new Intent(this, PlayService.class);
		intent.setAction( PlayController.ACTION_PLAYER_EMPTY );
		startService(intent);
	}
	
	public PlayFileHandler getPlayFileHandler() {
		return playFileHandler;
	}

}
