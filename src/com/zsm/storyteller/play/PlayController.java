package com.zsm.storyteller.play;

import android.net.Uri;

import com.zsm.storyteller.PlayInfo;

public interface PlayController {
	public enum PLAYER_STATE { 
		IDLE, INITIALIZED, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACKCOMPLETED }
	
	public enum PLAY_ORDER { BY_NAME, RANDOM }
	public enum PLAY_PAUSE_TYPE { CONTINUOUS, TO_PAUSE }

	public static final String ACTION_PLAYER_PLAY_PAUSE
		= "com.zsm.storyteller.PLAYER.PLAY_PAUSE";
	public static final String ACTION_PLAYER_PLAY
		= "com.zsm.storyteller.PLAYER.PLAY";
	public static final String ACTION_PLAYER_START
		= "com.zsm.storyteller.PLAYER.START";
	public static final String ACTION_PLAYER_PAUSE
		= "com.zsm.storyteller.PLAYER.PAUSE";
	public static final String ACTION_PLAYER_STOP
		= "com.zsm.storyteller.PLAYER.STOP";
	public static final String ACTION_PLAYER_PLAY_PREVIOUS
		= "com.zsm.storyteller.PLAYER.PLAY_PREVIOUS";
	public static final String ACTION_PLAYER_PLAY_NEXT
		= "com.zsm.storyteller.PLAYER.PLAY_NEXT";
	public static final String ACTION_PLAYER_PLAY_FAST_FORWARD
		= "com.zsm.storyteller.PLAYER.PLAY_FAST_FORWARD";
	public static final String ACTION_PLAYER_PLAY_REWIND
		= "com.zsm.storyteller.PLAYER.PLAY_REWIND";
	public static final String ACTION_PLAYER_SEEK_TO
		= "com.zsm.storyteller.PLAYER.SEEK_TO";
	public static final String ACTION_PLAYER_SET_PLAY_INFO
		= "com.zsm.storyteller.PLAYER.SET_PLAY_INFO";
	public static final String ACTION_PLAYER_MAIN_ACTIVITY
		= "com.zsm.storyteller.PLAYER.MAIN_ACTIVITY";
	public static final String ACTION_PLAYER_EMPTY
		= "com.zsm.storyteller.PLAYER.EMPTY_ACTION";
	public static final String ACTION_GET_PLAYER_STATE
		= "com.zsm.storyteller.PLAYER.GET_STATE";
	public final static String ACTION_UPDATE_PLAY_PAUSE_TYPE
		= "com.zsm.storyteller.PLAYER.UPDATE_PLAY_PAUSE_TYPE";
	public final static String ACTION_ENABLE_CAPTURE
		= "com.zsm.storyteller.PLAYER.ENABLE_CAPTURE";
	
	public static final int REQUEST_RETRIEVE_CODE = 100;
	public static final int REQUEST_PLAY_CODE = 101;

	public static final String KEY_PLAYER_STATE = "PLAYER_STATE";
	public static final String KEY_PLAY_PAUSE_TYPE = "PLAY_PAUSE_TYPE";
	public static final String KEY_PLAYER_RESULT_RECEIVER = "PLAYER_RESULT_RECEIVER";
	public static final String KEY_MEDIA_POSITION = "MEDIA_POSITION";
	public static final String KEY_PLAYER_UPDATE_VIEW = "PLAYER_UPDATE_VIEW";
	public static final String KEY_PLAYER_PLAY_INFO = "PLAYER_PLAY_INFO";
	public static final String KEY_PLAY_ITEM = "PLAY_ITEM";
	public static final String KEY_ENABLE_CAPTURE = "ENABLE_CAPTURE";
	public static final String KEY_CAPTURE_SOURCE = "CAPTURE_SOURCE";
	
	void play(Uri uri, int startPosition);

	void stop();

	void toNext();

	void toPrevious();

	void forward();

	void rewind();

	void seekTo(int progress);

	void pause(boolean updateView);

	void start(boolean updateView);

	void playPause();

	void onDestroy();

	void setPlayInfo(PlayInfo playInfo);

	PlayController.PLAYER_STATE getState();

	void enableCapture(String source, boolean enabled);
}
