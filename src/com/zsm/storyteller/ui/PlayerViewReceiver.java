package com.zsm.storyteller.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.play.PlayController;

public class PlayerViewReceiver {

	private PlayerView view;

	PlayerViewReceiver( PlayerView view ) {
		super();
		this.view = view;
	}
	
	public boolean onReceive(Context context, Intent intent) {
		switch( intent.getAction() ) {
			case PlayerView.ACTION_UPDATE_PLAYER_STATE:
				String stateStr
					= intent.getStringExtra( PlayerView.KEY_PLAYER_STATE );
				PlayController.PLAYER_STATE state; 
				try {
					state = PlayController.PLAYER_STATE.valueOf( stateStr );
				} catch ( Exception e ) {
					Log.e( e, "Invalid state", stateStr );
					break;
				}
				view.updatePlayerState( state );
				break;
			case PlayerView.ACTION_UPDATE_DATA_SOURCE:
				Uri uri = (Uri)intent.getParcelableExtra(PlayerView.KEY_DATA_SOURCE);
				if( uri != null ) {
					view.setDataSource(context, uri);
				}
				break;
			case PlayerView.ACTION_UPDATE_ELLAPSED_TIME:
				int time = intent.getIntExtra(PlayerView.KEY_ELLAPSED_TIME, 0);
				int duration = intent.getIntExtra(PlayerView.KEY_DURATION, 0);
				view.updateTime(time, duration);
				break;
			case PlayerView.ACTION_UPDATE_PLAY_INFO:
				PlayInfo pi = intent.getParcelableExtra( PlayerView.KEY_PLAY_INFO );
				Log.d(pi);
				view.updatePlayList( pi.getCurrentPlayList() );
				break;
			default:
				Log.d( "Unsupported action and type", intent.getAction() );
				return false;
		}
		
		return true;
	}

}
