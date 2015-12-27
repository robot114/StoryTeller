package com.zsm.storyteller.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class AudioDataReceiver extends BroadcastReceiver {

	private AudioDataListener listener;
	private boolean registered;
	
	public static final int SPECTRUM_NUM = 48;
	public static final String KEY_AUDIO_DATA = "AUDIO_DATA";
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
			listener.updateData( data );
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
