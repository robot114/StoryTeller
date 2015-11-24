package com.zsm.storyteller.app;

import android.app.Application;

import com.zsm.driver.android.log.LogInstaller;
import com.zsm.driver.android.log.LogPreferences;
import com.zsm.log.Log;
import com.zsm.storyteller.play.PlayFileHandler;
import com.zsm.storyteller.play.StoryPlayer;
import com.zsm.storyteller.preferences.Preferences;

public class StoryTellerApp extends Application {

	public final static String[] EXTENSION = {
		".3gp", ".aac", ".flac", ".m4a", ".mp4", ".mid", ".mp3", ".xmf",
		".mxmf", ".rtx", ".rtttl", ".ota", ".imy", ".ogg", ".mkv", ".wav"
	};

	private StoryPlayer player;
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
		
		player = new StoryPlayer( this );
		playFileHandler = new PlayFileHandler(player);
		
		Log.d( this );
	}
	
	public StoryPlayer getPlayer() {
		return player;
	}

	public PlayFileHandler getPlayFileHandler() {
		return playFileHandler;
	}
}
