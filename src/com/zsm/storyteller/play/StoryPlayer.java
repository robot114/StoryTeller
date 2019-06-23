package com.zsm.storyteller.play;

import java.io.IOException;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;

import com.zsm.log.Log;
import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE;
import com.zsm.storyteller.play.audio.listener.AudioDataListener;
import com.zsm.storyteller.preferences.Preferences;

class StoryPlayer implements PlayController {
	
	private enum TASK_STATE { INIT, RUNNING, PAUSE, STOP };
	
	private Runnable updateRunner = new Runnable() {
		
		private int updateTimes = 0;
		
		@Override
		synchronized public void run() {
			PLAYER_STATE playerState = mediaPlayer.getState();
			if( playerState  == PLAYER_STATE.IDLE || playerState == PLAYER_STATE.END ) {
				return;
			}
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
	
	private static final int SAVING_POSITION_FACTOR = 120;
	
	private AbstractPlayer mediaPlayer;

	private PlayInfo playInfo;
	private TimeTimerTask timeTimerTask;
	private Context context;
	private PlayerNotifier mPlayerNotifier;

	private Handler handler;

	private boolean newStartFlag;
	
	private HashSet<String> mListenBy = new HashSet<String>();

	private LoudnessEnhancer mLoudnessEnhancer;

	public StoryPlayer( Context context, PlayerNotifier playerNotifier ) {
		this.context = context;
		this.mPlayerNotifier = playerNotifier;
		
		handler = new Handler();
		initPlayer(context);
		notifyStateChanged();
		newStartFlag = true;
	}

	private void initPlayer(Context context) {
		if( Preferences.getInstance().useSystemDefaultDecoder() ) {
			mediaPlayer = new AndroidMediaPlayer( context, this );
		} else {
			mediaPlayer = new DecodingPlayer( context );
		}
		
		mediaPlayer.reset();
		mediaPlayer.setWakeMode( context, PowerManager.PARTIAL_WAKE_LOCK );
		mediaPlayer.setOnErrorListener( new OnPlayerErrorListener() {
			@Override
			public boolean onError(AbstractPlayer player, int what, int extra) {
				Log.e( "Something is wrong.", player, "what", what, "extra", extra,
					   "newStartFlag", newStartFlag );
				return newStartFlag;
			}
		} );
		
		mediaPlayer.setOnCompletionListener( new OnPlayerCompletionListener() {
			@Override
			public void onCompletion(AbstractPlayer player) {
				stop();
				toNext( );
			}
		} );
		
		mediaPlayer.setOnPreparedListener( new OnPlayerPreparedListener() {
			@Override
			public void onPrepared(AbstractPlayer player) {
				enableAudioListenerByState();
				play();
			}
		} );
		
		mediaPlayer.setAudioDataListener( new AudioDataListener() {
			@Override
			public void updateData(DATA_FORMAT format, int samplingRate, byte[] data) {
				mPlayerNotifier.newAudioData(format, samplingRate, data);
			}

			@Override
			public void setCaptureRate(int captureRate) {
				// PlayerNotifier does not care about capture rate
			}
		}, 0 );
	}

	@Override
	public void start(boolean updateView) {
		mediaPlayer.start();
		enableAudioListenerByState();
		if( updateView ) {
			notifyStateChanged();
		}
	}

	@Override
	public void pause(boolean updateView) {
		if( mediaPlayer.isPlaying() ) {
			updateTime( mediaPlayer.getCurrentPosition(),
						mediaPlayer.getDuration(), 0 );
		}
		if( getState() != PLAYER_STATE.STARTED ) {
			return; 
		}
		enableAudioListenerByState();
		mediaPlayer.pause();
		if( updateView ) {
			notifyStateChanged();
			if( timeTimerTask != null ) {
				timeTimerTask.state = TASK_STATE.PAUSE;
			}
		}
	}
	
	@SuppressLint("Assert")
	private void play() {
		int cp = (int) getPlayInfoInner().getCurrentPlayingPosition();
		mediaPlayer.seekTo( cp );
		mediaPlayer.start();
		updateTime( cp, mediaPlayer.getDuration(), 0 );
		enableAudioListenerByState();
		notifyStateChanged();
		if( timeTimerTask == null ) {
			timeTimerTask = new TimeTimerTask();
			new Thread( timeTimerTask ).start();
		}
		
		assert( timeTimerTask.state != TASK_STATE.STOP );
		timeTimerTask.state = TASK_STATE.RUNNING;
		
		int gainmB = Preferences.getInstance().getLoudnessEnhancerValuebyVolumeFactor();
		changeVolumeLoudnessEnhance( gainmB );

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
			mediaPlayer.reset();
			playInfo.setCurrentPlaying(uri);
			playInfo.setCurrentPlayingPosition(startPosition);
			updatePlayerViewMedia(uri, startPosition);
		}
		playCurrent();
	}

	synchronized private void updatePlayerViewMedia(Uri uri, long startPosition) {
		mPlayerNotifier.updateDataSource( uri );
		MediaInfo currentMediaInfo = new MediaInfo( context, uri );
		int duration = currentMediaInfo.getDuration();
		
		int headerLength = Preferences.getInstance().getSkipHeaderValue()*1000;
		long sp = shouldSkipHeader( startPosition, headerLength, duration )
					? headerLength : startPosition;
		
		getPlayInfoInner().setCurrentPlaying( uri );
		getPlayInfoInner().setCurrentPlayingPosition( sp );
		updateTime((int)sp, duration, 0);		
	}

	private boolean shouldSkipHeader( long startPosition, int headerLength,
									  long duration ) {
		Preferences pref = Preferences.getInstance();
		return startPosition == 0 && pref.getSkipHeaderAuto() && headerLength < duration;
	}

	@Override
	public void stop() {
		if( inPlayingState() ) {
			mediaPlayer.stop();
			mediaPlayer.reset();
		}
		
		notifyStateChanged();
		stopTimeTimerTask();
		mediaPlayer.enableAudioDataListener(false);
		updateTime( 0, 0, 0 );
	}
	
	private boolean inPlayingState() {
		PLAYER_STATE playerState = mediaPlayer.getState();
		return playerState  == PLAYER_STATE.PAUSED 
				|| playerState == PLAYER_STATE.STARTED
				|| playerState == PLAYER_STATE.PREPARED;
	}
	
	@Override
	public void seekTo(int progress) {
		mediaPlayer.seekTo(progress);
		updateTime( progress, mediaPlayer.getDuration(), 0 );
	}

	private void notifyStateChanged() {
		mPlayerNotifier.stateChanged(mediaPlayer.getState());
	}
	
	private void updateTime(int currentPosition, int duration, int times) {
		if( times % SAVING_POSITION_FACTOR == 0 ) {
			getPlayInfoInner().setCurrentPlayingPosition(currentPosition);
		}
		mPlayerNotifier.updateTime(currentPosition, duration);
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
		stop();
		if( uri != null ) {
			play(uri, 0);
		}
	}

	@Override
	public void toPrevious() {
		Uri uri = getPlayInfoInner().previousOne( playRandomly() );
		stop();
		if( uri != null ) {
			play(uri, 0);
		}
	}
	
	private boolean playRandomly(){
		return Preferences.getInstance().getPlayOrder()
					== PlayController.PLAY_ORDER.RANDOM;
	}
	
	@Override
	public void playPause() {
		switch( mediaPlayer.getState()  ) {
			case STARTED:
				pause( true );
				break;
			case STOPPED:
			case IDLE:
			case INITIALIZED:
			case PAUSED:
			case PLAYBACKCOMPLETED:
				playCurrent();
				break;
			default:
				Log.e( new Exception( "Invalid state: " + mediaPlayer.getState() ) );
				break;
		}
	}

	private void playCurrent() {
		Uri currentPlaying = getPlayInfoInner().refreshCurrentPlaying();
		if( currentPlaying == null ) {
			mPlayerNotifier.notifyCannotPlay( R.string.openPlayFileFirst );
			return;
		}
		
		PLAYER_STATE state = mediaPlayer.getState();
		switch( state ) {
			case STOPPED:
			case IDLE:
			case INITIALIZED:
			case PLAYBACKCOMPLETED:
				prepareToPlay(currentPlaying);
				break;
			case PAUSED:
				play();
				break;
			default:
				Log.e( new Exception( "Invalid state: " + state ) );
				break;
		}
	}
	
	private boolean prepareToPlay(Uri currentPlaying) {
		mediaPlayer.reset();
		notifyStateChanged();
		try {
			mediaPlayer.setDataSource( context, currentPlaying );
			mediaPlayer.prepareAsync();
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			
			Log.e( e, "Cannot make the file to be played: ",
				   currentPlaying );
			mPlayerNotifier.notifyCannotPlay( R.string.openFileFailed );
			return false;
		}
		
		return true;
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
		playInfo.getPlayList(context, StoryTellerApp.getAudioFileFilter(context), true);
		mPlayerNotifier.updatePlayList( playInfo );
		Uri currentPlaying = playInfo.refreshCurrentPlaying();
		if( currentPlaying == null ) {
			mPlayerNotifier.notifyCannotPlay( R.string.noMediaToPlay );
			return;
		}
		
		// When the main activity is displayed after it is "killed", and the player is playing
		// the main activity has to be set the play info. But the state of the player is started.
		if( mediaPlayer.getState() == PLAYER_STATE.IDLE ) {
			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(context, currentPlaying);
			} catch (IOException e) {
				Log.e( e, "Cannot play the media" + currentPlaying );
				mPlayerNotifier.notifyCannotPlay( R.string.noMediaToPlay );
				return;
			}
		}
		
		long startPosition = playInfo.getCurrentPlayingPosition();
		updatePlayerViewMedia(currentPlaying, startPosition);
		notifyStateChanged();
	}

	private PlayInfo getPlayInfoInner() {
		if( playInfo == null ) {
			playInfo = Preferences.getInstance().readPlayListInfo();
			playInfo.getPlayList( context, StoryTellerApp.getAudioFileFilter(context), true );
		}
		
		return playInfo;
	}

	@Override
	public PLAYER_STATE getState() {
		return mediaPlayer.getState();
	}

	@Override
	synchronized public void enableAudioListener(String source, boolean enabled) {
		if( enabled ) {
			mListenBy.add(source);
		} else {
			mListenBy.remove(source);
		}
		enableAudioListenerByState();
	}

	private void enableAudioListenerByState() {
		boolean shouldEnable
			= ( mediaPlayer.getState() == PLAYER_STATE.STARTED && !mListenBy.isEmpty() );
		mediaPlayer.enableAudioDataListener( shouldEnable );
	}
	
	synchronized public void disableAudioListener() {
		mediaPlayer.enableAudioDataListener( false );
	}

	public int getAudioCaptureRate() {
		return mediaPlayer.getAudioCaptureRate();
	}
	
	@Override
	public void changeVolumeLoudnessEnhance( int gainmB ) {
		final int audioSessionId = mediaPlayer.getAudioSessionId();
		if( mLoudnessEnhancer == null ) {
			mLoudnessEnhancer = new LoudnessEnhancer( audioSessionId );
		}
		
        NoiseSuppressor.create(audioSessionId);
        AcousticEchoCanceler.create(audioSessionId);

        mLoudnessEnhancer.setTargetGain(gainmB);
        mLoudnessEnhancer.setEnabled(true);
	}
}
