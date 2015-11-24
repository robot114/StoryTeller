package com.zsm.storyteller.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.zsm.log.Log;
import com.zsm.storyteller.play.PlayerView;
import com.zsm.storyteller.play.StoryPlayer.PLAYER_STATE;

public class PlayerViewReceiver {

	private PlayerView view;

	PlayerViewReceiver( PlayerView view ) {
		super();
		this.view = view;
	}
	
	IntentFilter buildIntentFilter( String action ) {
		Log.d( "Register the BroadcastReceiver.", "PlayerView", view, "Receiver", this );
		IntentFilter filter = new IntentFilter();
		filter.addAction( action );
		return filter;
	}

	public boolean onReceive(Context context, Intent intent) {
		Log.d(intent);
		switch( intent.getAction() ) {
			case PlayerView.ACTION_UPDATE_PLAYER_STATE:
				String stateStr
					= intent.getStringExtra( PlayerView.KEY_PLAYER_STATE );
				PLAYER_STATE state; 
				try {
					state = PLAYER_STATE.valueOf( stateStr );
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
			default:
				Log.d( "Unsupported action and type", intent.getAction() );
				return false;
		}
		
		return true;
	}

}
