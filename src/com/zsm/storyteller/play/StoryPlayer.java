package com.zsm.storyteller.play;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.preferences.Preferences;

public class StoryPlayer implements PlayController {
	
	private enum TASK_STATE { INIT, RUNNING, PAUSE, STOP };
	
	private Runnable updateRunner = new Runnable() {
		
		private int updateTimes = 0;
		
		@Override
		public void run() {
			int currentPosition = mediaPlayer.getCurrentPosition();
			updateTime( currentPosition, updateTimes++ );
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
	
	public enum PLAYER_STATE { 
		IDLE, INITIALIZED, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACKCOMPLETED };
		
	private static final int SAVING_POSITION_FACTOR = 4;
	
	private MediaPlayer mediaPlayer;
	private PLAYER_STATE playerState = PLAYER_STATE.IDLE;
	private PlayerView playerView;

	private PlayInfo playInfo;
	private TimeTimerTask timeTimerTask;
	private Context context;
	private Handler handler;
	
	public StoryPlayer( Context context ) {
		this.context = context;
		handler = new Handler();
		mediaPlayer = new MediaPlayer();
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
	}

	@Override
	public void setPlayerView( PlayerView pv ) {
		this.playerView = pv;
	}

	@Override
	public void start(boolean updateView) {
		mediaPlayer.start();
		playerState = PLAYER_STATE.STARTED;
		if( updateView ) {
			playerView.updatePlayerState( playerState );
		}
	}

	@Override
	public void pause(boolean updateView) {
		mediaPlayer.pause();
		playerState = PLAYER_STATE.PAUSED;
		if( updateView ) {
			playerView.updatePlayerState( playerState );
		}
	}
	
	@SuppressLint("Assert")
	private void play() {
		int cp = (int) playInfo.getCurrentPlayingPosition();
		mediaPlayer.seekTo( cp );
		mediaPlayer.start();
		playerView.setDuration( mediaPlayer.getDuration() );
		playerView.updateTime( cp );
		playerState = PLAYER_STATE.STARTED;
		playerView.updatePlayerState( playerState );
		if( timeTimerTask == null ) {
			timeTimerTask = new TimeTimerTask();
			new Thread( timeTimerTask ).start();
		}
		
		assert( timeTimerTask.state != TASK_STATE.STOP );
		timeTimerTask.state = TASK_STATE.RUNNING;
	}

	@Override
	public void forward() {
		if( inPlayingState() ) {
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
		if( inPlayingState() ) {
			int msec = mediaPlayer.getCurrentPosition() - skipMillisecond();
			msec = msec < 0 ? 0 : msec;
			mediaPlayer.seekTo( msec );
		}
	}

	@Override
	public void selectOneToPlay(Uri uri, long startPosition ) {
		stopTimeTimerTask();
		if( !updatePlayerViewMedia(uri, startPosition) ) {
			return;
		}
		if( prepareToPlay( uri ) ) {
			playerState = PLAYER_STATE.PREPARED;
			playerView.updatePlayerState( playerState );
		}
	}

	private boolean updatePlayerViewMedia(Uri uri, long startPosition) {
		int headerLength = Preferences.getInstance().getSkipHeaderValue()*1000;
		long sp = shouldSkipHeader(startPosition) ? headerLength : startPosition;
		playInfo.setCurrentPlaying( uri );
		playInfo.setCurrentPlayingPosition( sp );
		try {
			playerView.setDataSource( uri );
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException e) {
			
			Log.e( e, "Cannot make the file to be played: ", uri );
			Toast.makeText( context, R.string.openFileFailed, Toast.LENGTH_LONG )
				 .show();
			return false;
		}
		playerView.updateTime(sp);		
		return true;
	}

	private boolean shouldSkipHeader(long startPosition) {
		Preferences pref = Preferences.getInstance();
		return startPosition == 0 
				&& pref.getSkipHeaderAuto() 
				&& pref.getSkipHeaderValue()*1000 < mediaPlayer.getDuration();
	}

	@Override
	public void stop() {
		if( playerState == PLAYER_STATE.STOPPED ) {
			return;
		}
		
		mediaPlayer.stop();
		mediaPlayer.reset();
		playerState = PLAYER_STATE.STOPPED;
		playerView.updatePlayerState( playerState );
		stopTimeTimerTask();
		updateTime( 0, 0 );
	}
	
	@Override
	public void seekTo(int progress) {
		mediaPlayer.seekTo(progress);
		updateTime( progress, 0 );
	}

	private void updateTime(int currentPosition, int times) {
		if( times % SAVING_POSITION_FACTOR == 0 ) {
			playInfo.setCurrentPlayingPosition(currentPosition);
		}
		playerView.updateTime( currentPosition );
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
		Uri uri = playInfo.nextOne();
		if( uri != null ) {
			selectOneToPlay(uri, 0);
		} else {
			stop();
		}
	}
	
	@Override
	public void toPrevious() {
		Uri uri = playInfo.previousOne();
		if( uri != null ) {
			selectOneToPlay(uri, 0);
		} else {
			stop();
		}
	}
	
	@Override
	public void playPause() {
		Uri currentPlaying = playInfo.refreshCurrentPlaying();
		if( currentPlaying == null ) {
			Toast.makeText( context, R.string.openPlayFileFirst, Toast.LENGTH_LONG )
				 .show();
			return;
		}
		switch( playerState ) {
			case STARTED:
				mediaPlayer.pause();
				playerState = PLAYER_STATE.PAUSED;
				playerView.updatePlayerState(playerState);
				timeTimerTask.state = TASK_STATE.PAUSE;
				break;
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
		playerView.updatePlayerState( playerState );
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

	@Override
	public void onDestory() {
		if( mediaPlayer != null ) {
			stop();
			mediaPlayer.release();
		}
	}
	
	@Override
	public boolean inPlayingState() {
		return playerState == PLAYER_STATE.PAUSED 
				|| playerState == PLAYER_STATE.STARTED
				|| playerState == PLAYER_STATE.PREPARED;
	}

	@Override
	public PLAYER_STATE getState() {
		return playerState;
	}

	@Override
	public void updatePlayInfo(PlayInfo playInfo) {
		this.playInfo = playInfo;
		playerView.updatePlayList( 
						playInfo.getPlayList(StoryTellerApp.EXTENSION, true) );
		Uri currentPlaying = playInfo.refreshCurrentPlaying();
		if( currentPlaying == null ) {
			Toast.makeText( context, R.string.noMediaToPlay, Toast.LENGTH_LONG )
			 	 .show();
			return;
		}
		
		long startPosition = playInfo.getCurrentPlayingPosition();
		if( Preferences.getInstance().autoStartPlaying() && playerState == PLAYER_STATE.STARTED ) {
			selectOneToPlay( currentPlaying, startPosition );
		} else {
			updatePlayerViewMedia(currentPlaying, startPosition);
		}
		
		Preferences.getInstance().savePlayListInfo( playInfo );
	}

	@Override
	public PlayInfo getPlayInfo() {
		playInfo.setCurrentPlayingPosition( mediaPlayer.getCurrentPosition() );
		return playInfo;
	}

}
