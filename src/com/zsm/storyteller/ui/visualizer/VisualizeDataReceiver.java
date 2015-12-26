package com.zsm.storyteller.ui.visualizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class VisualizeDataReceiver extends BroadcastReceiver {

	private VisualizeDataListener listener;
	private boolean registered;
	
	public static final int SPECTRUM_NUM = 48;
	public static final String KEY_VISUAL_DATA = "VISUAL_DATA";
	public static final String ACTION_UPDATE_VISUAL_DATA
		= "com.zsm.storyteller.VISUALIZER.UPDATE_VISUAL_DATA";

	public VisualizeDataReceiver( VisualizeDataListener l ) {
		listener = l;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if( VisualizeDataReceiver.ACTION_UPDATE_VISUAL_DATA
				.equals(intent.getAction() ) ) {
			
			byte[] data = intent.getByteArrayExtra( KEY_VISUAL_DATA );
			listener.updateData( data );
		}
	}
	
	public void registerMe( Context context ) {
		if( !registered ) {
			IntentFilter filter = new IntentFilter( ACTION_UPDATE_VISUAL_DATA );
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
