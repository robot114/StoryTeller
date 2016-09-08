package com.zsm.storyteller.play;

import com.zsm.storyteller.play.audio.listener.AudioDataListener.DATA_FORMAT;
import com.zsm.storyteller.play.audio.listener.AudioDataListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class AudioDataReceiver extends BroadcastReceiver {

	private static final int DEFAULT_SAMPLING_RATE = 44100;
	private AudioDataListener listener;
	private boolean registered;
	
	public static final String KEY_AUDIO_DATA = "AUDIO_DATA";
	public static final String KEY_AUDIO_DATA_FORMAT = "AUDIO_DATA_FORMAT";
	public static final String KEY_AUDIO_DATA_SAMPLING_RATE = "AUDIO_DATA_SAMPLING_RATE";	// in Hz
	
	public static final String ACTION_UPDATE_AUDIO_DATA
		= "com.zsm.storyteller.AUDIODATA.UPDATE_AUDIO_DATA";

	public AudioDataReceiver( AudioDataListener l ) {
		listener = l;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if( AudioDataReceiver.ACTION_UPDATE_AUDIO_DATA
				.equals(intent.getAction() ) ) {
			
			byte[] data = intent.getByteArrayExtra( KEY_AUDIO_DATA );
			int sr = intent.getIntExtra( KEY_AUDIO_DATA_SAMPLING_RATE,
										 DEFAULT_SAMPLING_RATE );
			DATA_FORMAT format
				= (DATA_FORMAT) intent.getSerializableExtra( KEY_AUDIO_DATA_FORMAT );
			format = format == null ? DATA_FORMAT.FFT : format;
			
			listener.updateData( format, sr, data );
		}
	}
	
	public void registerMe( Context context ) {
		if( !registered ) {
			IntentFilter filter = new IntentFilter( ACTION_UPDATE_AUDIO_DATA );
			context.registerReceiver( this, filter );
			registered = true;
		}
	}

	public void unregisterMe( Context context ) {
		if( registered ) {
			context.unregisterReceiver( this );
			registered = false;
		}
	}
	
	public boolean isRegistered() {
		return registered;
	}
}
