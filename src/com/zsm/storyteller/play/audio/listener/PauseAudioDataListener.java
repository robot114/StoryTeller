package com.zsm.storyteller.play.audio.listener;

import com.zsm.log.Log;
import com.zsm.storyteller.play.PlayController;

public class PauseAudioDataListener implements AudioDataListener {

	// How long the silence time to stop playing, in nanosec
	private static final int STOP_SILENCE_NS = 200*1000;
	private PlayController player;
	private boolean soundPlayed = false;
	private int mSilenceTime = 0;			// in nanosec
	private int silenceToSilence = 5;
	private int mCaptureInterval = 0;		// in nanosec
	private int mSilenceTimeToPauseNs = STOP_SILENCE_NS;

	public PauseAudioDataListener( PlayController player ) {
		this.player = player;
		mSilenceTime = 0;
	}

	@Override
	public void updateData(DATA_FORMAT format, int samplingRate, byte[] data) {
		switch( format ) {
			case FFT:
				forFftData(samplingRate, data);
				break;
			case WAVEFORM:
				forWaveFormData(samplingRate, data);
				break;
			default:
				Log.e( "Unsupported audio data format", format );
				break;
		}
	}

	private void forFftData(int samplingRate, byte[] data) {
				
		int i = 1;
		// Only the human's spectrum is used as condition of pause
		int length = Math.min( data.length, AudioCaptureSampler.HUMAN_BAND_NUM );
		for( ; i < length && Math.abs( data[i] ) < silenceToSilence; i++ ) {
		}
		if( i >= length ) {
			mSilenceTime
				+= ( mCaptureInterval > 0 
					 ? mCaptureInterval : data.length*2000*1000/samplingRate);
			// Amplitude  of all human's spectrums are too low
			if( soundPlayed && mSilenceTime >= mSilenceTimeToPauseNs ) {
				// Human's spectrums are low enough to long enough
				pauseForSilence();
			}
		} else {
			soundPlayed = true;
			mSilenceTime = 0;
		}
	}

	private void forWaveFormData(int samplingRate, byte[] data) {
		int sampleDeltaNanosec = 1000*1000/samplingRate;
		
		mSilenceTime += mCaptureInterval;
		for( int i = 0; i < data.length; i++ ) {
			if( Math.abs( data[i] ) > silenceToSilence ) {
				soundPlayed = true;
				mSilenceTime = 0;
				break;
			}
			mSilenceTime += sampleDeltaNanosec;
			if( soundPlayed && mSilenceTime >= mSilenceTimeToPauseNs ) {
				pauseForSilence();
				break;
			}
		}
	}

	private void pauseForSilence() {
		player.pause( true );
		soundPlayed = false;
		mSilenceTime = 0;
	}

	/*
	 * 
	 */
	public void setSilenceTimeToPause(int silenceTimeMsToPause) {
		mSilenceTimeToPauseNs  = silenceTimeMsToPause*1000;
	}

	public void setSilenceToSilence(int silenceToSilence) {
		this.silenceToSilence = silenceToSilence;
	}

	@Override
	public void setCaptureRate(int captureRate) {
		// CR in milliHz. interval in ns = 1/(CR*0.001)*1000 = 1000*1000/CR
		mCaptureInterval = 1000*1000*1000/captureRate;
	}

}
