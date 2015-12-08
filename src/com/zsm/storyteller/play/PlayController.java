package com.zsm.storyteller.play;

import android.net.Uri;

import com.zsm.storyteller.PlayInfo;

public interface PlayController {
	public enum PLAYER_STATE { 
		IDLE, INITIALIZED, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACKCOMPLETED }

	public static final String ACTION_PLAYER_PLAY_PAUSE
		= "com.zsm.storyteller.PLAYER.PLAY_PAUSE";
	public static final String ACTION_PLAYER_PLAY
		= "com.zsm.storyteller.PLAYER.PLAY";
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
	public static final String ACTION_PLAYER_MAIN_ACTIVITY
		= "com.zsm.storyteller.PLAYER.MAIN_ACTIVITY";
	public static final String ACTION_PLAYER_EMPTY
		= "com.zsm.storyteller.PLAYER.EMPTY_ACTION";
	public static final String ACTION_GET_PLAYER_STATE
		= "com.zsm.storyteller.PLAYER.GET_STATE";

	public static final String KEY_PLAYER_STATE = "PLAYER_STATE";
	public static final String KEY_PLAYER_RESULT_RECEIVER = "PLAYER_RESULT_RECEIVER";
	
	public static final int REQUEST_RETRIEVE_CODE = 100;
	public static final int REQUEST_PLAY_CODE = 101;
	
	void selectOneToPlay(Uri uri, long startPosition);

	void stop();

	void toNext();

	void toPrevious();

	void forward();

	void rewind();

	void seekTo(int progress);

	void pause(boolean updateView);

	void start(boolean updateView);

	boolean inPlayingState();

	void playPause();

	void onDestroy();

	void setPlayInfo(PlayInfo playInfo);

//	PlayInfo getPlayInfo();

	PlayController.PLAYER_STATE getState();
}
