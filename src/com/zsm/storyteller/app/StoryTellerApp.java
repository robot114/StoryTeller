package com.zsm.storyteller.app;

import java.util.concurrent.Semaphore;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;

import com.zsm.driver.android.log.LogInstaller;
import com.zsm.driver.android.log.LogPreferences;
import com.zsm.log.Log;
import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayFileHandler;
import com.zsm.storyteller.play.PlayService;
import com.zsm.storyteller.preferences.Preferences;
import com.zsm.util.file.FileExtensionFilter;

public class StoryTellerApp extends Application {

	private static FileExtensionFilter[] FILE_FILTER_ARRAY;
	
	private static final String[] EXTENSION = {
		".3gp", ".aac", ".flac", ".m4a", ".mp4", ".mid", ".mp3", ".xmf",
		".mxmf", ".rtx", ".rtttl", ".ota", ".imy", ".ogg", ".mkv", ".wav"
	};

	PlayController player;
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
		
		bindPlayService();
		playFileHandler = new PlayFileHandler(this);
	}
	
	private void bindPlayService() {
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
	}
	
	public PlayFileHandler getPlayFileHandler() {
		return playFileHandler;
	}

	public PlayController getPlayer() {
		if( player == null ) {
			if( Looper.myLooper() == Looper.getMainLooper() ) {
				throw new IllegalStateException( "Cannot invoke this method in the main looper!" );
			}
			try {
				playerSemaphore.acquire();
			} catch (InterruptedException e) {
				Log.e( e );
			}
		}
		return player;
	}

	public static FileExtensionFilter[] getAudioFileFilterArray( Context c ) {
		if( FILE_FILTER_ARRAY == null ) {
			String filterDescr
				= c.getResources().getString( R.string.audioFileFilterDescription );
			FILE_FILTER_ARRAY
				=new FileExtensionFilter[] { 
						new FileExtensionFilter( EXTENSION, filterDescr ) };
		}
		
		return FILE_FILTER_ARRAY;
	}
	
	public static FileExtensionFilter getAudioFileFilter( Context c ) {
		return getAudioFileFilterArray( c )[0];
	}
}
