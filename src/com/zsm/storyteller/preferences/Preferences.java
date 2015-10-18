package com.zsm.storyteller.preferences;

import com.zsm.storyteller.PlayInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Preferences {

	static private Preferences instance;
	
	final private SharedPreferences preferences;
	
	private StackTraceElement[] stackTrace;

	public static final String KEY_CURRENT_PLAYING = "CURRENT_PLAYING";

	public static final String KEY_LIST_INFO = "LIST_INFO";

	public static final String KEY_LIST_TYEP = "LIST_TYPE";
	
	private Preferences( Context context ) {
		preferences
			= PreferenceManager
				.getDefaultSharedPreferences( context );
		
	}
	
	static public void init( Context c ) {
		if( instance != null ) {
			throw new IllegalStateException( "Preference has been initialized! "
											 + "Call getInitStackTrace() to get "
											 + "the initlization place." );
		}
		instance = new Preferences( c );
		instance.stackTrace = Thread.currentThread().getStackTrace();
	}
	
	static public Preferences getInstance() {
		return instance;
	}
	
	public StackTraceElement[] getInitStackTrace() {
		return stackTrace;
	}
	
	public void savePlayListInfo( PlayInfo pi ) {
		pi.toPreferences(preferences);
	}
	
	public PlayInfo readPlayListInf() {
		return PlayInfo.fromPreferences(preferences);
	}
	
	public void setCurrentPlaying( Uri currentPlaying ) {
		preferences
			.edit()
			.putString(Preferences.KEY_CURRENT_PLAYING, currentPlaying.toString() )
			.apply();
	}
}
