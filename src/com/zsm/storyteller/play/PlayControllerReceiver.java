package com.zsm.storyteller.play;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.zsm.log.Log;

public class PlayControllerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d( intent );
		if( intent.getAction().equals( AudioManager.ACTION_AUDIO_BECOMING_NOISY ) ) {
			sendActionToService(context, PlayController.ACTION_PLAYER_PAUSE );
		} else if( intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			KeyEvent event
				= (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			handleMediaButton( context, event );
		}
	}

	void handleMediaButton( Context context, KeyEvent event ) {
        if (event == null)
            return;

        if( event.getAction() == KeyEvent.ACTION_DOWN 
        	&& event.getKeyCode() != KeyEvent.KEYCODE_HEADSETHOOK ) {
        	
        	return;
        }
		Log.d(event);
        String action = null;
        switch (event.getKeyCode()) {
	        //这里根据按下的时间和操作，分离出具体的控制
	        case KeyEvent.KEYCODE_HEADSETHOOK:
	        	action = handleHandsetHook( event );
	            break;
	        //下面是常规的播放、暂停、停止、上下曲　
	        case KeyEvent.KEYCODE_MEDIA_PLAY:
	        	action = PlayController.ACTION_PLAYER_PLAY;
	        	break;
	        case KeyEvent.KEYCODE_MEDIA_PAUSE:
	        	action = PlayController.ACTION_PLAYER_PAUSE;
	        	break;
	        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
	        	action = PlayController.ACTION_PLAYER_PLAY_PAUSE;
	            break;
	        case KeyEvent.KEYCODE_MEDIA_STOP:
	        	action = PlayController.ACTION_PLAYER_STOP;
	            break;
	        case KeyEvent.KEYCODE_MEDIA_NEXT:
	        	action = PlayController.ACTION_PLAYER_PLAY_NEXT;
	            break;
	        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
	        	action = PlayController.ACTION_PLAYER_PLAY_PREVIOUS;
	            break;
	        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
	        	action = PlayController.ACTION_PLAYER_PLAY_FAST_FORWARD;
	            break;
	        case KeyEvent.KEYCODE_MEDIA_REWIND:
	        	action = PlayController.ACTION_PLAYER_PLAY_REWIND;
	            break;
	    }
        sendActionToService(context, action);
    }

	private void sendActionToService(Context context, String action) {
		if( action != null ) {
        	Intent intent = new Intent( action );
        	PendingIntent pi
        		= PendingIntent.getService(context,
        								   PlayController.REQUEST_PLAY_CODE,
        								   intent,
        								   0);
        	try {
				pi.send();
			} catch (CanceledException e) {
				Log.e( "Action from media button does not sent", "action", action );
			}
        }
	}
	
    /*
     * one click => play/pause long click => previous double click => next
     */
	private long mHeadsetDownTime;
	private long mHeadsetUpTime;
	private String handleHandsetHook( KeyEvent event ) {
        long time = SystemClock.uptimeMillis();
        String action = null;
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                if (event.getRepeatCount() > 0)
                    break;
                mHeadsetDownTime = time;
                break;
            case KeyEvent.ACTION_UP:
                if (time - mHeadsetDownTime >= 1000) {
                    // long click
                	action = PlayController.ACTION_PLAYER_PLAY_PREVIOUS;
                    time = 0;
                } else if (time - mHeadsetUpTime <= 500) {
                    // double click
                    action = PlayController.ACTION_PLAYER_PLAY_NEXT;
                } else {
                    // one click
                	action = PlayController.ACTION_PLAYER_PLAY_PAUSE;
                }
                mHeadsetUpTime = time;
                break;
        }
        
        return action;
	}
}