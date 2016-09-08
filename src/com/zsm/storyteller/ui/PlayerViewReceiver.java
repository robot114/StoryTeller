package com.zsm.storyteller.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.zsm.android.util.IntentUtil;
import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.play.AbstractPlayer;
import com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;

public class PlayerViewReceiver {

	private PlayerView view;

	PlayerViewReceiver( PlayerView view ) {
		super();
		this.view = view;
	}
	
	public boolean onReceive(Context context, Intent intent) {
		switch( intent.getAction() ) {
			case PlayerView.ACTION_UPDATE_PLAYER_STATE:
				AbstractPlayer.PLAYER_STATE state
					= IntentUtil.getEnumValueIntent(
							intent, PlayerView.KEY_PLAYER_STATE,
							AbstractPlayer.PLAYER_STATE.class, null);
				if( state != null ) {
					view.updatePlayerState( state );
				}
				Log.d(state);
				break;
			case PlayController.ACTION_UPDATE_PLAY_PAUSE_TYPE:
				PLAY_PAUSE_TYPE type
					=  IntentUtil.getEnumValueIntent(
							intent, PlayController.KEY_PLAY_PAUSE_TYPE,
							PLAY_PAUSE_TYPE.class, null);
				if( type != null ) {
					view.updatePlayPauseType( context, type );
				}
				break;
			case PlayerView.ACTION_UPDATE_DATA_SOURCE:
				Uri uri = (Uri)intent.getParcelableExtra(PlayerView.KEY_DATA_SOURCE);
				if( uri != null ) {
					Log.d(this);
					view.setDataSource(context, uri);
				}
				break;
			case PlayerView.ACTION_UPDATE_ELLAPSED_TIME:
				int time = intent.getIntExtra(PlayerView.KEY_ELLAPSED_TIME, 0);
				int duration = intent.getIntExtra(PlayerView.KEY_DURATION, 0);
				view.updateTime(time, duration);
				Log.d( "time", time, "duration", duration );
				break;
			case PlayerView.ACTION_UPDATE_PLAY_INFO:
				PlayInfo pi = intent.getParcelableExtra( PlayerView.KEY_PLAY_INFO );
				view.updatePlayList( pi.getCurrentPlayList() );
				break;
			default:
				Log.d( "Unsupported action and type", intent.getAction() );
				return false;
		}
		
		return true;
	}
}
