package com.zsm.storyteller.play;

import java.util.concurrent.Semaphore;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;

public class RemotePlayer implements PlayController {

	private final class PlayerStateResultReceiver extends ResultReceiver {
		private PLAYER_STATE state;

		private PlayerStateResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			String stateName
				= resultData.getString( PlayController.KEY_PLAYER_STATE, "" );
			state = PLAYER_STATE.valueOf(stateName);
			playerSemaphore.release();
		}
	}

	private Context context;
	private Semaphore playerSemaphore = new Semaphore( 0 );
	
	public RemotePlayer( Context context ) {
		this.context = context;
	}

	@Override
	public void play(Uri uri, int startPosition) {
		Bundle bundle = new Bundle();
		bundle.putParcelable( KEY_PLAY_ITEM, uri );
		bundle.putInt( KEY_MEDIA_POSITION, startPosition );
		sendRequest(ACTION_PLAYER_PLAY, bundle);
	}

	@Override
	public void stop() {
		sendRequest(ACTION_PLAYER_STOP);
	}

	@Override
	public void toNext() {
		sendRequest(ACTION_PLAYER_PLAY_NEXT);
	}

	@Override
	public void toPrevious() {
		sendRequest(ACTION_PLAYER_PLAY_PREVIOUS);
	}

	@Override
	public void forward() {
		sendRequest(ACTION_PLAYER_PLAY_FAST_FORWARD);
	}

	@Override
	public void rewind() {
		sendRequest(ACTION_PLAYER_PLAY_REWIND);
	}

	@Override
	public void seekTo(int progress) {
		Bundle bundle = new Bundle();
		bundle.putInt( KEY_MEDIA_POSITION, progress );
		sendRequest(ACTION_PLAYER_SEEK_TO, bundle);
	}

	@Override
	public void pause(boolean updateView) {
		Bundle bundle = new Bundle();
		bundle.putBoolean( KEY_PLAYER_UPDATE_VIEW, updateView );
		sendRequest(ACTION_PLAYER_PAUSE, bundle);
	}

	@Override
	public void start(boolean updateView) {
		Bundle bundle = new Bundle();
		bundle.putBoolean( KEY_PLAYER_UPDATE_VIEW, updateView );
		sendRequest(ACTION_PLAYER_START, bundle);
	}

	@Override
	public void playPause() {
		sendRequest(ACTION_PLAYER_PLAY_PAUSE);
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void setPlayInfo(PlayInfo playInfo) {
		Bundle bundle = new Bundle();
		bundle.putParcelable( KEY_PLAYER_PLAY_INFO, playInfo );
		sendRequest(ACTION_PLAYER_SET_PLAY_INFO, bundle);
	}

	@Override
	public PLAYER_STATE getState() {
		return getPlayerStateNow();
	}

	private void sendRequest(String action) {
		sendRequest(action, null);
	}

	private void sendRequest(String action, Bundle bundle) {
		Intent intent = new Intent( context, PlayService.class );
		intent.setAction( action );
		if( bundle != null ) {
			intent.putExtras( bundle );
		}
		
		PendingIntent pi
			= PendingIntent.getService(context, REQUEST_PLAY_CODE,
									   intent, PendingIntent.FLAG_UPDATE_CURRENT );
		try {
			pi.send();
		} catch (CanceledException e) {
			Log.e( e, "Cannot do the action", intent.getAction() );
		}
	}

	synchronized private PLAYER_STATE getPlayerStateNow() {
		if( Looper.myLooper() == Looper.getMainLooper() ) {
			IllegalStateException e = 
					new IllegalStateException( 
						"Cannot get player state from the service in the main thread!" );
			Log.e( e );
			throw e;
		}
		
		PlayerStateResultReceiver rr = new PlayerStateResultReceiver(null);
		Intent intent = new Intent( context, PlayService.class );
		intent.setAction( PlayController.ACTION_GET_PLAYER_STATE );
		intent.putExtra( PlayController.KEY_PLAYER_RESULT_RECEIVER, rr );
		PendingIntent pi
			= PendingIntent.getService( context, REQUEST_RETRIEVE_CODE, intent, 0 );
		try {
			pi.send();
			playerSemaphore.acquire();
		} catch (CanceledException | InterruptedException e) {
			Log.e( e, "Get player state failed!" );
			return null;
		}
		return rr.state;
	}
}
