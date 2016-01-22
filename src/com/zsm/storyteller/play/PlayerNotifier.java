package com.zsm.storyteller.play;

import android.net.Uri;

import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.play.PlayController.PLAYER_STATE;

public interface PlayerNotifier {
	void stateChanged( PLAYER_STATE newState );

	void newAudioData( byte[] data );
	
	void updateTime( int ellapsed, int duration );
	
	void updateDataSource( Uri uri );
	
	void updatePlayList( PlayInfo pi );
	
	void notifyCannotPlay( int promptId );
}
