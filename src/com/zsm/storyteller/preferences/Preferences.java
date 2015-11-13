package com.zsm.storyteller.preferences;

import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Preferences {

	public enum FORWARD_SKIP_TYPE { BY_PERCENT, BY_SECOND };
	
	static private Preferences instance;
	
	final private SharedPreferences preferences;
	
	private StackTraceElement[] stackTrace;

	private int forwardSkipPercentProgressValue = -1;
	private int forwardSkipSecondProgressValue = -1;
	private FORWARD_SKIP_TYPE forwardSkipType = null;

	private boolean skipHeader;
	private int skipHeaderSecond = -1;

	public static final String KEY_LIST_TYPE = "LIST_TYPE";
	public static final String KEY_LIST_INFO = "LIST_INFO";
	public static final String KEY_CURRENT_PLAYING = "CURRENT_PLAYING";
	public static final String KEY_CURRENT_POSITION = "CURRENT_POSITION";
	private static final String KEY_FORWARD_SKIP_TYPE = "FORWARD_SKIP_TYPE";
	private static final String KEY_FORWARD_SKIP_PERCENT = "FORWARD_SKIP_PERCENT";
	private static final String KEY_FORWARD_SKIP_SECOND = "FORWARD_SKIP_SECOND";
	private static final String KEY_SKIP_HEADER = "SKIP_HEADER";
	private static final String KEY_SKIP_HEADER_SECOND = "SKIP_HEADER_SECOND";
	private static final String KEY_AUTO_START_PLAYING = "AUTO_START_PLAYING";
	
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

	public void setCurrentPlayingPosition(long position) {
		preferences
			.edit()
			.putLong(Preferences.KEY_CURRENT_POSITION, position )
			.apply();
	}
	
	public FORWARD_SKIP_TYPE getForwardSkipType() {
		if( forwardSkipType != null ) {
			return forwardSkipType;
		}
		
		String typeStr
			= preferences.getString( KEY_FORWARD_SKIP_TYPE,
									 FORWARD_SKIP_TYPE.BY_SECOND.name() );
		forwardSkipType = FORWARD_SKIP_TYPE.BY_SECOND;
		try {
			forwardSkipType = FORWARD_SKIP_TYPE.valueOf(typeStr);
		} catch ( Exception e ) {
			Log.e( e, "Failed to get forward skip type from preferences: ",
				   typeStr );
		}
		
		return forwardSkipType;
	}
	
	public int getForwardSkipPercentProgressValue() {
		if( forwardSkipPercentProgressValue >= 0 ) {
			return forwardSkipPercentProgressValue;
		}
		
		forwardSkipPercentProgressValue = 0;
		try {
			forwardSkipPercentProgressValue
				= preferences.getInt(KEY_FORWARD_SKIP_PERCENT, 0 );
		} catch ( Exception e ) {
			Log.e( e );
		}
		
		return forwardSkipPercentProgressValue;
	}
	
	public int getForwardSkipSecondProgressValue() {
		if( forwardSkipSecondProgressValue >= 0 ) {
			return forwardSkipSecondProgressValue;
		}
		
		forwardSkipSecondProgressValue = 0;
		try {
			forwardSkipSecondProgressValue
				= preferences.getInt(KEY_FORWARD_SKIP_SECOND, 4 );
		} catch ( Exception e ) {
			Log.e( e );
		}
		
		return forwardSkipSecondProgressValue;
	}
	
	public int getForwardSecond( int durationMillisecond ) {
		switch( getForwardSkipType() ) {
			case BY_PERCENT:
				float skipPercent
					= forwardSkipPercentProgressToRealValue( 
							getForwardSkipPercentProgressValue() )/100f;
				
				return (int)(durationMillisecond*skipPercent );
			case BY_SECOND:
				return forwardSkipSecondProgressToRealValue( 
							getForwardSkipSecondProgressValue() )*1000;
			default:
				throw new IllegalStateException( 
							"Invalid skip type: " + forwardSkipType );
		}
	}
	
	static public float forwardSkipPercentProgressToRealValue( 
							int progressValue ) {
		
		return progressValue/2.0f+0.5f;
	}
	
	static public int forwardSkipSecondProgressToRealValue( 
				int progressValue ) {
	
		return progressValue+1;
	}

	public void setForwardSkipValue( FORWARD_SKIP_TYPE type,
									 int progressValueOfPercent,
									 int progressValueOfSecond ) {
		
		preferences
			.edit()
			.putString(KEY_FORWARD_SKIP_TYPE, type.name() )
			.putInt(KEY_FORWARD_SKIP_PERCENT, progressValueOfPercent)
			.putInt(KEY_FORWARD_SKIP_SECOND, progressValueOfSecond)
			.commit();
		
		forwardSkipPercentProgressValue = progressValueOfPercent;
		forwardSkipSecondProgressValue = progressValueOfSecond;
		forwardSkipType = type;
	}

	public boolean getSkipHeaderAuto() {
		if( skipHeaderSecond >= 0 ) {
			return skipHeader;
		}
		
		skipHeader = preferences.getBoolean( KEY_SKIP_HEADER, false );
		return skipHeader;
	}

	public int getSkipHeaderValue() {
		if( skipHeaderSecond >= 0 ) {
			return skipHeaderSecond;
		}
		
		skipHeaderSecond = preferences.getInt( KEY_SKIP_HEADER_SECOND, 0 );
		return skipHeaderSecond;
	}
	
	public void setSkipHeader( boolean skipHeader, int skipSec ) {
		preferences
			.edit()
			.putBoolean(KEY_SKIP_HEADER, skipHeader)
			.putInt(KEY_SKIP_HEADER_SECOND, skipSec)
			.commit();
		
		this.skipHeader = skipHeader;
		skipHeaderSecond = skipSec;
	}
	
	public boolean autoStartPlaying() {
		return preferences.getBoolean( KEY_AUTO_START_PLAYING, true );
	}
}
