package com.zsm.storyteller.play;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

import com.zsm.storyteller.play.audio.listener.AudioDataListener;
import com.zsm.storyteller.play.audio.listener.AudioSampler;
import com.zsm.storyteller.play.audio.listener.AudioCaptureSampler;

public class AndroidMediaPlayer implements AbstractPlayer {

	private PLAYER_STATE mState;
	private PlayController mController;
	private MediaPlayer mPlayer;
	private AudioSampler mAudioHandler;
	
	public AndroidMediaPlayer( Context context, PlayController controller ) {
		mController = controller;
		mPlayer = new MediaPlayer();
		mState = PLAYER_STATE.IDLE;
		mAudioHandler = new AudioCaptureSampler( context );
	}
	
	@Override
	public void reset() {
		mPlayer.reset();
		mState = PLAYER_STATE.IDLE;
	}

	@Override
	public void setDataSource(Context context, Uri currentPlaying)
					throws IOException {
		
		mPlayer.setDataSource(context, currentPlaying);
		mState = PLAYER_STATE.INITIALIZED;
	}

	@Override
	public void prepareAsync() {
		mPlayer.prepareAsync();
		mState = PLAYER_STATE.PREPARING;
	}

	@Override
	public int getCurrentPosition() {
		return mPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return mPlayer.getDuration();
	}

	@Override
	public void setWakeMode(Context context, int mode) {
		mPlayer.setWakeMode(context, mode);
	}

	@Override
	public void start() {
		mPlayer.start();
		mState = PLAYER_STATE.STARTED;
	}

	@Override
	public void pause() {
		mPlayer.pause();
		mState = PLAYER_STATE.PAUSED;
	}

	@Override
	public void stop() {
		mPlayer.stop();
		mState = PLAYER_STATE.STOPPED;
	}

	@Override
	public void release() {
		mPlayer.release();
		mState = PLAYER_STATE.IDLE;
	}

	@Override
	public void seekTo(int msec) {
		mPlayer.seekTo(msec);
	}

	@Override
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}

	@Override
	public int getAudioSessionId() {
		return mPlayer.getAudioSessionId();
	}

	@Override
	public void setOnErrorListener(final OnPlayerErrorListener listener) {
		mPlayer.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				return listener.onError( AndroidMediaPlayer.this, what, extra );
			}
		});
	}

	@Override
	public void setOnCompletionListener( final OnPlayerCompletionListener listener) {
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				listener.onCompletion( AndroidMediaPlayer.this );
			}
		});
	}

	@Override
	public void setOnPreparedListener(final OnPlayerPreparedListener listener) {
		mPlayer.setOnPreparedListener(new OnPreparedListener(){
			@Override
			public void onPrepared(MediaPlayer mp) {
		        int audioSessionId = getAudioSessionId();
		        mAudioHandler.setAudioSession(audioSessionId);
		        enableAudioDataListener( false );
				listener.onPrepared( AndroidMediaPlayer.this );
			}
		} );
	}

	@Override
	public PLAYER_STATE getState() {
		return mState;
	}

	@Override
	public void setAudioDataListener(AudioDataListener l, int rate) {
		mAudioHandler.setDataListener(l, rate);
	}

	@Override
	synchronized public void enableAudioDataListener(boolean enable) {
		mAudioHandler.setEnabled(enable);
	}

	@Override
	public int getAudioCaptureRate() {
		return mAudioHandler.getMaxCaptureRate();
	}
}
