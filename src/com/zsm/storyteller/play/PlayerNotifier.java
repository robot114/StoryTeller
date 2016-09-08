package com.zsm.storyteller.play;

import android.net.Uri;

import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.play.audio.listener.AudioDataListener.DATA_FORMAT;

public interface PlayerNotifier {
	
	void stateChanged( AbstractPlayer.PLAYER_STATE newState );

	void newAudioData( DATA_FORMAT format, int samplingRate, byte[] data );
	
	void updateTime( int ellapsed, int duration );
	
	void updateDataSource( Uri uri );
	
	void updatePlayList( PlayInfo pi );
	
	void notifyCannotPlay( int promptId );
}
