package com.zsm.storyteller.play.audio.listener;

import java.util.Arrays;

import com.zsm.log.Log;
import com.zsm.storyteller.play.AbstractPlayer;

import android.content.Context;
import android.media.audiofx.Visualizer;

public class PushedAudioSampler implements AudioSampler, AudioDataListener {

	// This rate should be cabled by this machine
	private static final int DEFAULT_CAPTURE_RATE = Visualizer.getMaxCaptureRate();
	private static final int SAMPLE_SIZE = 256;
	private Context mContext;
	private AbstractPlayer mPlayer;
	private boolean mEnabled;
	private long mLastUpdateTime;
	private long mUpdateDelta;	// How long, in ns, to update the data to the sampler
	private AudioDataListener mClientDataListener;

	public PushedAudioSampler( Context context, AbstractPlayer player ) {
		mContext = context;
		mPlayer = player;
	}
	
	@Override
	public void setAudioSession(int audioSession) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
		mLastUpdateTime = 0L;
		Log.i( "Enable the PushedAudioSampler as", mEnabled );
	}
	
	@Override
	public boolean getEnabled() {
		return mEnabled;
	}

	@Override
	public void setDataListener(AudioDataListener l, int captureRate) {
		mClientDataListener = l;
		setCaptureRate(captureRate);
	}

	@Override
	synchronized public void updateData(DATA_FORMAT format, int samplingRate,
										byte[] data) {
		
		long currentTime = System.nanoTime();
		if( mClientDataListener != null && mEnabled
			&& currentTime - mLastUpdateTime > mUpdateDelta ) {
			
			mClientDataListener.updateData(format, samplingRate,
										   Arrays.copyOf(data, data.length));
			mLastUpdateTime = currentTime;
		}
	}

	@Override
	public int getMaxDataLength() {
		return SAMPLE_SIZE;
	}

	@Override
	public void setCaptureRate(int captureRate) {
		int usedCR = captureRate <= 0 ? DEFAULT_CAPTURE_RATE : captureRate;
		if( mClientDataListener != null && usedCR > 0 ) {
			mClientDataListener.setCaptureRate(usedCR);
		}
		// nanosecond to milliHz
		mUpdateDelta = 1000*1000*1000L/captureRate;
	}

	@Override
	public int getMaxCaptureRate() {
		return DEFAULT_CAPTURE_RATE;
	}

}
