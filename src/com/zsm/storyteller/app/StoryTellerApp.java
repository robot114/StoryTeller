package com.zsm.storyteller.app;

import java.util.concurrent.Semaphore;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.zsm.driver.android.log.LogInstaller;
import com.zsm.driver.android.log.LogPreferences;
import com.zsm.log.Log;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayFileHandler;
import com.zsm.storyteller.play.PlayService;
import com.zsm.storyteller.preferences.Preferences;

public class StoryTellerApp extends Application {

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		Log.d();
	}

	public final static String[] EXTENSION = {
		".3gp", ".aac", ".flac", ".m4a", ".mp4", ".mid", ".mp3", ".xmf",
		".mxmf", ".rtx", ".rtttl", ".ota", ".imy", ".ogg", ".mkv", ".wav"
	};

	private PlayController player;
	private PlayFileHandler playFileHandler;
	private Semaphore playerSemaphore = new Semaphore(0);
	
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
		
		ServiceConnection serviceConnection = new ServiceConnection() {
		    @Override
		    public void onServiceConnected(ComponentName name, IBinder service) {
		    	Log.d("Service connected", service);
		        PlayService.ServiceBinder binder = (PlayService.ServiceBinder) service;
		        player = binder.getService();
		        playerSemaphore.release();
		    }
		 
		    @Override
		    public void onServiceDisconnected(ComponentName name) {
		    	player = null;
		    }

		};
		
		Intent intent = new Intent(this, PlayService.class);
		bindService(intent, serviceConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_ABOVE_CLIENT);
		playFileHandler = new PlayFileHandler(player);
		
		Log.d( this );
	}
	
	public PlayFileHandler getPlayFileHandler() {
		return playFileHandler;
	}

	public PlayController getPlayer() {
		if( player == null ) {
			try {
				playerSemaphore.acquire();
			} catch (InterruptedException e) {
				Log.e( e );
			}
		}
		return player;
	}
}
