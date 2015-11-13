package com.zsm.storyteller.play;

import java.util.List;

import android.net.Uri;

public interface PlayerView {

	void setDuration(int duration);

	void updateTime(long curretPosition);

	void updatePlayerState(StoryPlayer.PLAYER_STATE started);

	void setDataSource(Uri uri);

	void updatePlayList(List<Uri> playList);

}
