package com.zsm.storyteller.play;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;

public class RemotePlayer implements PlayController {

	private Context context;
	
	public RemotePlayer( Context context ) {
		this.context = context;
	}

	@Override
	public void selectOneToPlay(Uri uri, long startPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		Intent intent = new Intent( PlayController.ACTION_PLAYER_STOP );
		sendRequest(intent);
	}

	@Override
	public void toNext() {
		// TODO Auto-generated method stub

	}

	@Override
	public void toPrevious() {
		// TODO Auto-generated method stub

	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rewind() {
		// TODO Auto-generated method stub

	}

	@Override
	public void seekTo(int progress) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause(boolean updateView) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(boolean updateView) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean inPlayingState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void playPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlayInfo(PlayInfo playInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public PLAYER_STATE getState() {
		// TODO Auto-generated method stub
		return null;
	}

	private void sendRequest(Intent intent) {
		PendingIntent pi
			= PendingIntent.getService(context, PlayController.REQUEST_PLAY_CODE,
									   intent, 0 );
		try {
			pi.send();
		} catch (CanceledException e) {
			Log.e( e, "Cannot do the action", intent.getAction() );
		}
	}

}
