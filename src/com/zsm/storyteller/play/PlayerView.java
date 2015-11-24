package com.zsm.storyteller.play;

import java.util.List;

import android.content.Context;
import android.net.Uri;

public interface PlayerView {
	
	public final static String ACTION_UPDATE_DURATION
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_DURATION";
	public final static String ACTION_UPDATE_ELLAPSED_TIME
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_ELLAPSED_TIME";
	public final static String ACTION_UPDATE_PLAYER_STATE
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_PLAYER_STATE";
	public final static String ACTION_UPDATE_DATA_SOURCE
		= "com.zsm.storyteller.PLAYER_VIEW.UPDATE_DATA_SOURCE";
	
	public static final String KEY_DURATION = "DURATION";
	public static final String KEY_ELLAPSED_TIME = "ELLAPSED_TIME";
	public static final String KEY_PLAYER_STATE = "PLAY_STATE";
	public static final String KEY_DATA_SOURCE = "DATA_SOURCE";

	void setDuration(int duration);

	void updateTime(long curretPosition);

	void updatePlayerState(StoryPlayer.PLAYER_STATE state);

	void setDataSource(Context context, Uri uri);

	void updatePlayList(List<Uri> playList);

}
