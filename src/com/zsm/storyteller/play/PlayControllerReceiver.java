package com.zsm.storyteller.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.zsm.android.util.IntentUtil;
import com.zsm.log.Log;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;

public class PlayControllerReceiver extends BroadcastReceiver {

	private PlayController player;

	public PlayControllerReceiver( PlayController player ) {
		this.player = player;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d( intent, this );
		if( intent.getAction().equals( AudioManager.ACTION_AUDIO_BECOMING_NOISY ) ) {
			player.pause( true );
		} else if( intent.getAction()
					.equals( PlayController.ACTION_UPDATE_PLAY_PAUSE_TYPE ) ) {
			
			PLAY_PAUSE_TYPE type
				= IntentUtil.getEnumValueIntent(intent,
												PlayController.KEY_PLAY_PAUSE_TYPE,
												PLAY_PAUSE_TYPE.class,
												null );
			if( type != null ) {
				player.setPlayPauseType( type );
			}
		}
	}

}