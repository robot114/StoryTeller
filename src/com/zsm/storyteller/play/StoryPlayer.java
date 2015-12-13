package com.zsm.storyteller.play;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import com.zsm.log.Log;
import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.preferences.Preferences;
import com.zsm.storyteller.ui.PlayerView;

class StoryPlayer implements PlayController {
	
	private enum TASK_STATE { INIT, RUNNING, PAUSE, STOP };
	
	private Runnable updateRunner = new Runnable() {
		
		private int updateTimes = 0;
		
		@Override
		public void run() {
			int currentPosition = mediaPlayer.getCurrentPosition();
			updateTime( currentPosition, mediaPlayer.getDuration(), updateTimes++ );
			if( playerState == PLAYER_STATE.STARTED && !mediaPlayer.isPlaying() ) {
				mediaPlayer.start();
			}
		}
	};
		
	private class TimeTimerTask implements Runnable {

		private TASK_STATE state = TASK_STATE.INIT;
		@Override
		public void run() {
			while( state != TASK_STATE.STOP ) {
				try {
					Thread.sleep( 500 );
					if( state == TASK_STATE.RUNNING ) {
						handler.post( updateRunner );
					}
				} catch (InterruptedException e) {
					Log.e( e );
				}
			}
		}
	}
	
	private static final int SAVING_POSITION_FACTOR = 4;
	
	private MediaPlayer mediaPlayer;
	private PLAYER_STATE playerState = PLAYER_STATE.IDLE;

	private PlayInfo playInfo;
	private TimeTimerTask timeTimerTask;
	private Context context;
	private Handler handler;

	private boolean newStartFlag;

	public StoryPlayer( Context context ) {
		this.context = context;
		handler = new Handler();
		mediaPlayer = new MediaPlayer();
		mediaPlayer.reset();
		
		mediaPlayer.setWakeMode( context, PowerManager.PARTIAL_WAKE_LOCK );
		
		mediaPlayer.setOnErrorListener( new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e( "Something is wrong.", mp, "what", what, "extra", extra,
					   "newStartFlag", newStartFlag );
				return newStartFlag;
			}
		} );
		
		mediaPlayer.setOnCompletionListener( new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				toNext( );
			}
		} );
		
		mediaPlayer.setOnPreparedListener( new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				play();
			}
		} );
		
		notifyStateChanged();
		newStartFlag = true;
	}

	@Override
	public void start(boolean updateView) {
		mediaPlayer.start();
		playerState = PLAYER_STATE.STARTED;
		if( updateView ) {
			notifyStateChanged();
		}
	}

	@Override
	public void pause(boolean updateView) {
		mediaPlayer.pause();
		playerState = PLAYER_STATE.PAUSED;
		if( updateView ) {
			notifyStateChanged();
		}
	}
	
	@SuppressLint("Assert")
	private void play() {
		int cp = (int) getPlayInfoInner().getCurrentPlayingPosition();
		mediaPlayer.seekTo( cp );
		mediaPlayer.start();
		updateTime( cp, mediaPlayer.getDuration(), 0 );
		playerState = PLAYER_STATE.STARTED;
		notifyStateChanged();
		if( timeTimerTask == null ) {
			timeTimerTask = new TimeTimerTask();
			new Thread( timeTimerTask ).start();
		}
		
		assert( timeTimerTask.state != TASK_STATE.STOP );
		timeTimerTask.state = TASK_STATE.RUNNING;
	}

	@Override
	public void forward() {
		if( mediaPlayer.isPlaying() ) {
			int msec = mediaPlayer.getCurrentPosition() + skipMillisecond();
			if( msec < mediaPlayer.getDuration() ) {
				mediaPlayer.seekTo( msec );
			} else {
				toNext();
			}
		}
	}

	@Override
	public void rewind() {
		if( mediaPlayer.isPlaying() ) {
			int msec = mediaPlayer.getCurrentPosition() - skipMillisecond();
			msec = msec < 0 ? 0 : msec;
			mediaPlayer.seekTo( msec );
		}
	}

	@Override
	public void play(Uri uri, int startPosition ) {
		stopTimeTimerTask();
		if( uri != null ) {
			playInfo.setCurrentPlaying(uri);
			playInfo.setCurrentPlayingPosition(startPosition);
			playerState = PLAYER_STATE.IDLE;
			updatePlayerViewMedia(uri, startPosition);
		}
		playCurrent();
	}

	synchronized private void updatePlayerViewMedia(Uri uri, long startPosition) {
		int headerLength = Preferences.getInstance().getSkipHeaderValue()*1000;
		long sp = shouldSkipHeader(startPosition) ? headerLength : startPosition;
		getPlayInfoInner().setCurrentPlaying( uri );
		getPlayInfoInner().setCurrentPlayingPosition( sp );
		updateDataSource( uri );
		MediaInfo currentMediaInfo = new MediaInfo( context, uri );
		updateTime((int)sp, currentMediaInfo.getDuration(), 0);		
	}

	private boolean shouldSkipHeader(long startPosition) {
		Preferences pref = Preferences.getInstance();
		return startPosition == 0 
				&& pref.getSkipHeaderAuto() 
				&& pref.getSkipHeaderValue()*1000 < mediaPlayer.getDuration();
	}

	@Override
	public void stop() {
		if( inPlayingState() ) {
			mediaPlayer.stop();
			mediaPlayer.reset();
		}
		
		playerState = PLAYER_STATE.STOPPED;
		notifyStateChanged();
		stopTimeTimerTask();
		updateTime( 0, 0, 0 );
	}
	
	private boolean inPlayingState() {
		return playerState == PLAYER_STATE.PAUSED 
				|| playerState == PLAYER_STATE.STARTED
				|| playerState == PLAYER_STATE.PREPARED;
	}
	
	@Override
	public void seekTo(int progress) {
		mediaPlayer.seekTo(progress);
		updateTime( progress, mediaPlayer.getDuration(), 0 );
	}

	private void notifyStateChanged() {
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_PLAYER_STATE );
		intent.putExtra( PlayerView.KEY_PLAYER_STATE, playerState.name() );
		context.sendBroadcast(intent);
	}
	
	private void updateTime(int currentPosition, int duration, int times) {
		if( times % SAVING_POSITION_FACTOR == 0 ) {
			getPlayInfoInner().setCurrentPlayingPosition(currentPosition);
		}
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_ELLAPSED_TIME );
		intent.putExtra( PlayerView.KEY_ELLAPSED_TIME, currentPosition );
		intent.putExtra( PlayerView.KEY_DURATION, duration );
		context.sendBroadcast(intent);
	}

	private void stopTimeTimerTask() {
		if( timeTimerTask != null ) {
			handler.removeCallbacks(updateRunner);
			timeTimerTask.state = TASK_STATE.STOP;
			timeTimerTask = null;
		}
	}

	private int skipMillisecond() {
		return Preferences.getInstance()
			.getForwardSecond( mediaPlayer.getDuration() );
	}
	
	@Override
	public void toNext() {
		Uri uri = getPlayInfoInner().nextOne( playRandomly() );
		if( uri != null ) {
			play(uri, 0);
		} else {
			stop();
		}
	}

	@Override
	public void toPrevious() {
		Uri uri = getPlayInfoInner().previousOne( playRandomly() );
		if( uri != null ) {
			play(uri, 0);
		} else {
			stop();
		}
	}
	
	private boolean playRandomly(){
		return Preferences.getInstance().getPlayOrder()
					== PlayController.PLAY_ORDER.RANDOM;
	}
	
	@Override
	public void playPause() {
		switch( playerState ) {
			case STARTED:
				mediaPlayer.pause();
				playerState = PLAYER_STATE.PAUSED;
				notifyStateChanged();
				if( timeTimerTask != null ) {
					timeTimerTask.state = TASK_STATE.PAUSE;
				}
				if( mediaPlayer.isPlaying() ) {
					updateTime( mediaPlayer.getCurrentPosition(),
								mediaPlayer.getDuration(), 0 );
				}
				break;
			case STOPPED:
			case IDLE:
			case PAUSED:
				playCurrent();
				break;
			default:
				Log.e( new Exception( "Invalid state: " + playerState ) );
				break;
		}
	}

	private void playCurrent() {
		Uri currentPlaying = getPlayInfoInner().refreshCurrentPlaying();
		if( currentPlaying == null ) {
			Toast.makeText( context, R.string.openPlayFileFirst, Toast.LENGTH_LONG )
				 .show();
			return;
		}
		
		switch( playerState ) {
			case STOPPED:
			case IDLE:
				prepareToPlay(currentPlaying);
				break;
			case PAUSED:
				play();
				break;
			default:
				Log.e( new Exception( "Invalid state: " + playerState ) );
				break;
		}
	}
	
	private boolean prepareToPlay(Uri currentPlaying) {
		mediaPlayer.reset();
		playerState = PLAYER_STATE.IDLE;
		notifyStateChanged();
		try {
			mediaPlayer.setDataSource( context, currentPlaying );
			mediaPlayer.prepareAsync();
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			
			Log.e( e, "Cannot make the file to be played: ",
				   currentPlaying );
			Toast.makeText( context, R.string.openFileFailed, Toast.LENGTH_LONG )
				 .show();
			return false;
		}
		
		return true;
	}

	private void updateDataSource( Uri uri ) {
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_DATA_SOURCE );
		intent.putExtra( PlayerView.KEY_DATA_SOURCE, uri );
		context.sendBroadcast(intent);
	}

	private void updatePlayList( PlayInfo pi ) {
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_PLAY_INFO );
		intent.putExtra(PlayerView.KEY_PLAY_INFO, pi);
		context.sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		if( mediaPlayer != null ) {
			pause( true );
			stopTimeTimerTask();
			mediaPlayer.release();
		}
	}
	
	@Override
	public void setPlayInfo(PlayInfo pi) {
		playInfo = pi;
		playInfo.getPlayList(StoryTellerApp.EXTENSION, true);
		updatePlayList( playInfo );
		Uri currentPlaying = playInfo.refreshCurrentPlaying();
		if( currentPlaying == null ) {
			Toast.makeText( context, R.string.noMediaToPlay, Toast.LENGTH_LONG )
			 	 .show();
			return;
		}
		
		long startPosition = playInfo.getCurrentPlayingPosition();
		updatePlayerViewMedia(currentPlaying, startPosition);
		notifyStateChanged();
	}

	private PlayInfo getPlayInfoInner() {
		if( playInfo == null ) {
			playInfo = Preferences.getInstance().readPlayListInfo();
			playInfo.getPlayList( StoryTellerApp.EXTENSION, true );
		}
		
		return playInfo;
	}

	@Override
	public PLAYER_STATE getState() {
		return playerState;
	}
}
