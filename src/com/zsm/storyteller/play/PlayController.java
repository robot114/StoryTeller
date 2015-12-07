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
	public static final String ACTION_PLAYER_MAIN_ACTIVITY
		= "com.zsm.storyteller.PLAYER.MAIN_ACTIVITY";
	public static final String ACTION_PLAYER_EMPTY
		= "com.zsm.storyteller.PLAYER.EMPTY_ACTION";

	void selectOneToPlay(Uri uri, long startPosition);

	void stop();

	void toNext();

	void toPrevious();

	void forward();

	void rewind();

	void seekTo(int progress);

	PlayController.PLAYER_STATE getState();

	void pause(boolean updateView);

	void start(boolean updateView);

	boolean inPlayingState();

	void playPause();

	void onDestroy();

	void setPlayInfo(PlayInfo playInfo);

	PlayInfo getPlayInfo();
}
