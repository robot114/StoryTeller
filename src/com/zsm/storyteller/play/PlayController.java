package com.zsm.storyteller.play;

import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.play.StoryPlayer.PLAYER_STATE;

import android.net.Uri;

public interface PlayController {
	public static final String ACTION_PLAYER_PLAY
		= "com.zsm.storyteller.PLAYER.PLAY";
	public static final String ACTION_PLAYER_PLAY_NEXT
		= "com.zsm.storyteller.PLAYER.PLAY_NEXT";
	public static final String ACTION_PLAYER_MAIN_ACTIVITY
		= "com.zsm.storyteller.PLAYER.MAIN_ACTIVITY";

	void selectOneToPlay(Uri uri, long startPosition);

	void stop();

	void toNext();

	void toPrevious();

	void forward();

	void rewind();

	void seekTo(int progress);

	PLAYER_STATE getState();

	void pause(boolean updateView);

	void start(boolean updateView);

	boolean inPlayingState();

	void playPause();

	void onDestory();

	void setPlayerView(PlayerView pv);

	void updatePlayInfo(PlayInfo playInfo);

	PlayInfo getPlayInfo();
}
