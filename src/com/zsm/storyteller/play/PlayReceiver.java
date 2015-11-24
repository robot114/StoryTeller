package com.zsm.storyteller.play;

import com.zsm.log.Log;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.ui.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlayReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d( intent );
		StoryTellerApp app = (StoryTellerApp)context.getApplicationContext();
		switch( intent.getAction() ) {
			case PlayController.ACTION_PLAYER_PLAY:
				app.getPlayer().playPause();
				break;
			case PlayController.ACTION_PLAYER_PLAY_NEXT:
				app.getPlayer().toNext();
				break;
			case PlayController.ACTION_PLAYER_MAIN_ACTIVITY:
				startMainActivity( context );
				break;
			default:
				Log.w( "Invalid action type", intent );
				break;
		}
	}

	private void startMainActivity(Context context) {
		Intent intent = new Intent( context, MainActivity.class );
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
						| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
