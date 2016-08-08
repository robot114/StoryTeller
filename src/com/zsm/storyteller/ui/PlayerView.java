package com.zsm.storyteller.ui;

import java.util.List;

import android.content.Context;
import android.net.Uri;

import com.zsm.storyteller.play.AbstractPlayer;
import com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;

public interface PlayerView {
	
	public final static int PLAYER_VIEW_REQUEST_ID = 100;
	
	public final static String ACTION_UPDATE_ELLAPSED_TIME
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_ELLAPSED_TIME";
	public final static String ACTION_UPDATE_PLAYER_STATE
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_PLAYER_STATE";
	public final static String ACTION_UPDATE_DATA_SOURCE
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_DATA_SOURCE";
	public final static String ACTION_UPDATE_PLAY_INFO
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_PLAY_INFO";
	
	public static final String KEY_DURATION = "DURATION";
	public static final String KEY_ELLAPSED_TIME = "ELLAPSED_TIME";
	public static final String KEY_PLAYER_STATE = "PLAY_STATE";
	public static final String KEY_DATA_SOURCE = "DATA_SOURCE";
	public static final String KEY_PLAY_INFO = "PLAY_LIST";
	
	void updateTime(int curretPosition, int duration);

	void updatePlayerState(AbstractPlayer.PLAYER_STATE state);

	void setDataSource(Context context, Uri uri);

	void updatePlayList(List<Uri> playList);

	void updatePlayPauseType(Context context, PLAY_PAUSE_TYPE type);
}
