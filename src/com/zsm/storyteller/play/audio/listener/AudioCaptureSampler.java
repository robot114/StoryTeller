package com.zsm.storyteller.play.audio.listener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.zsm.storyteller.R;

public class AudioCaptureSampler implements AudioSampler {

	private static final int MAX_PIANO_FREQUENCY = 4400;
	public static final int CAPTURE_SIZE = Visualizer.getCaptureSizeRange()[1];
	private static final int CAPTURE_RATE = Visualizer.getMaxCaptureRate();
	private static final int MUSIC_BAND_NUM = 48;
	private static final int SPECTRUM_BAND_FACTOR
			= (MAX_PIANO_FREQUENCY*CAPTURE_SIZE/22050+MUSIC_BAND_NUM-1)/MUSIC_BAND_NUM;
	// Spectrum number is up to piano's highest frequency, and round to band number
	private static final int SPECTRUM_NUM = SPECTRUM_BAND_FACTOR*MUSIC_BAND_NUM;
	public static final int HUMAN_BAND_NUM = 500*MUSIC_BAND_NUM/MAX_PIANO_FREQUENCY;
	
	private Visualizer mVisializer;
	private Equalizer mEqualizer;
	private AudioDataListener mDataListener;
	private OnDataCaptureListener mCaptureListener;
	private Context mContext;
	private int mCaptureRate;
	
	public AudioCaptureSampler(Context context) {
		mContext = context;
	}

	@Override
	public void setAudioSession( int audioSession ) {
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
			&& ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) 
				!= PackageManager.PERMISSION_GRANTED ) {
			// needed for Android 6, in case users deny microphone permission,
			// otherwise we get java.lang.SecurityException from ContentResolver.query()
			// see https://developer.android.com/training/permissions/requesting.html
			new Handler( Looper.getMainLooper() ).post( new Runnable(){
				@Override
				public void run() {
					Toast.makeText(mContext, R.string.permissionNoMicrophone,
								   Toast.LENGTH_LONG ).show();
				}
			} );
			return;
		}

		mVisializer = new Visualizer(audioSession);
		mEqualizer = new Equalizer( 0, audioSession );
		mEqualizer.setEnabled(true);
		
		mVisializer.setEnabled(false);
		
		mVisializer.setScalingMode( Visualizer.SCALING_MODE_NORMALIZED );
		mVisializer.setMeasurementMode( Visualizer.MEASUREMENT_MODE_PEAK_RMS );
		mVisializer.setCaptureSize( CAPTURE_SIZE );
		
		mCaptureListener = new Visualizer.OnDataCaptureListener() {
			public void onWaveFormDataCapture(Visualizer visualizer,
					byte[] bytes, int samplingRate) {
				
		        mDataListener.updateData(AudioDataListener.DATA_FORMAT.WAVEFORM,
		        						 samplingRate, bytes);
			}

			public void onFftDataCapture(Visualizer visualizer, byte[] fft,
										 int samplingRate) {
				
				byte[] model = doSpectrum(fft);
		        mDataListener.updateData(AudioDataListener.DATA_FORMAT.FFT,
		        						 samplingRate, model);
			}
		};
	}

	@Override
	public void setEnabled( boolean enabled ) {
		if( mVisializer == null ) {
			return;
		}
		if( enabled ) {
			int rate
				= ( mCaptureRate > CAPTURE_RATE || mCaptureRate == 0 ) 
					? CAPTURE_RATE : mCaptureRate;
			mVisializer
				.setDataCaptureListener(mCaptureListener, rate, false, true );
		} else {
			mVisializer.setDataCaptureListener(null, 0, false, false);
		}
		mVisializer.setEnabled( enabled );
		mEqualizer.setEnabled(enabled);
	}
	
	@Override
	public boolean getEnabled() {
		return mVisializer != null && mVisializer.getEnabled();
	}
	
	// Get the envelope of the spectrum up to piano's max frequency.
	private byte[] doSpectrum( byte[] data ) {
		int spectrumNum = Math.min( data.length / 2 + 1, SPECTRUM_NUM );
		int bandNum = spectrumNum/SPECTRUM_BAND_FACTOR;
        byte[] model = new byte[bandNum];  
        
        model[0] = (byte) Math.abs(data[0]);
        for (int i = 2, j = 0; j < bandNum; j++ ) {
        	model[j] = maxSpectrum(data, i);
            i += 2*SPECTRUM_BAND_FACTOR;
        }
        
        return model;
	}

	private byte envelopeSpectrum(byte[] data, int i) {
		int total = 0;
		for( int k = 0; k < SPECTRUM_BAND_FACTOR; k++ ) {
		    total += Math.hypot(data[i], data[i + 1]);  
		}
		return (byte) (total/SPECTRUM_BAND_FACTOR);
	}

	private byte maxSpectrum(byte[] data, int i) {
		byte max = 0;
		byte spec;
		for( int k = 0; k < SPECTRUM_BAND_FACTOR; k++ ) {
		    spec = (byte) Math.hypot(data[i], data[i + 1]);
		    max = spec > max ? spec : max;
		}
		return max;
	}

	@Override
	public void setDataListener(AudioDataListener l, int captureRate) {
		mDataListener = l;
		mCaptureRate
			= ( captureRate > 0 && captureRate < CAPTURE_RATE ) 
				? captureRate : CAPTURE_RATE;
		l.setCaptureRate( mCaptureRate );
	}

	@Override
	public int getMaxDataLength() {
		return CAPTURE_SIZE;
	}

	@Override
	public int getMaxCaptureRate() {
		return CAPTURE_RATE;
	}

}
