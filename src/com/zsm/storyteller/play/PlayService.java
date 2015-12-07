package com.zsm.storyteller.play;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import com.zsm.log.Log;
import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.preferences.Preferences;
import com.zsm.storyteller.ui.MainActivity;

public class PlayService extends Service
				implements PlayController, OnAudioFocusChangeListener {

	public static final int NOTIFICATION_ID = 1;

	private IBinder binder = null;
	private PlayController player;
	private Notification notification;
	
	private PlayControllerReceiver receiver;

	private AudioManager audioManager;

	private ComponentName buttonReceiverCompName;

	public final class ServiceBinder extends Binder {
		public PlayService getService() {
		    return PlayService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PlayInfo playInfo = initPlayer();
		notification = buildNotification( this, playInfo );
		startForeground(NOTIFICATION_ID, notification);
		
		receiver = new PlayControllerReceiver();
		IntentFilter filter
			= new IntentFilter( AudioManager.ACTION_AUDIO_BECOMING_NOISY );
		registerReceiver( receiver, filter);
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		buttonReceiverCompName
			= new ComponentName( getPackageName(), 
								 PlayControllerReceiver.class.getName() );
		audioManager.registerMediaButtonEventReceiver(buttonReceiverCompName);
	}

	private PlayInfo initPlayer() {
		player = new StoryPlayer( this );
		PlayInfo playInfo = Preferences.getInstance().readPlayListInfo();
		setPlayInfo( playInfo );
		return playInfo;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if( binder == null ) {
			binder = new ServiceBinder();
		}
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent( intent );
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		receiver = null;
		audioManager.unregisterMediaButtonEventReceiver(buttonReceiverCompName);
		player.onDestroy();
		super.onDestroy();
	}

	private boolean allowToPlay() {
		int result
			= audioManager
				.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
								   AudioManager.AUDIOFOCUS_GAIN);
		
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}
	
	private void promptNotAllowed() {
		Toast.makeText( this, R.string.audioFocusPlayNotAllowed,
						Toast.LENGTH_SHORT )
			 .show();
	}
	
	@Override
	public void selectOneToPlay(Uri uri, long startPosition) {
		if( allowToPlay() ) {
			player.selectOneToPlay(uri, startPosition);
		} else {
			promptNotAllowed();
		}
	}

	@Override
	public void stop() {
		player.stop();
		audioManager.abandonAudioFocus( this );
	}

	@Override
	public void toNext() {
		if( allowToPlay() ) {
			player.toNext();
		} else {
			promptNotAllowed();
		}
	}

	@Override
	public void toPrevious() {
		if( allowToPlay() ) {
			player.toPrevious();
		} else {
			promptNotAllowed();
		}
	}

	@Override
	public void forward() {
		player.forward();
	}

	@Override
	public void rewind() {
		player.rewind();
	}

	@Override
	public void seekTo(int progress) {
		player.seekTo(progress);
	}

	@Override
	public PlayController.PLAYER_STATE getState() {
		return player.getState();
	}

	@Override
	public void pause(boolean updateView) {
		player.pause(updateView);
		audioManager.abandonAudioFocus( this );
	}

	@Override
	public void start(boolean updateView) {
		if( allowToPlay() ) {
			player.start(updateView);
		} else {
			promptNotAllowed();
		}
	}

	@Override
	public boolean inPlayingState() {
		return player.inPlayingState();
	}

	@Override
	public void playPause() {
		if( allowToPlay() ) {
			player.playPause();
		} else {
			promptNotAllowed();
		}
		
		if( !inPlayingState() ) {
			audioManager.abandonAudioFocus(this);
		}
	}

	@Override
	public void setPlayInfo(PlayInfo playInfo) {
		player.setPlayInfo(playInfo);
		Notification notification = buildNotify(playInfo);
	    NotificationManagerCompat
	    	.from(this).notify(PlayService.NOTIFICATION_ID, notification);
	}

	private Notification buildNotify(PlayInfo playInfo) {
		Notification notification
			= PlayService.buildNotification( this, playInfo );
		return notification;
	}

	@Override
	public PlayInfo getPlayInfo() {
		return player.getPlayInfo();
	}

	private void handleIntent(Intent intent) {
		Log.d(intent);
		if( intent == null ) {
			return;
		}
		switch( intent.getAction() ) {
			case PlayController.ACTION_PLAYER_PLAY_PAUSE:
				playPause();
				break;
			case PlayController.ACTION_PLAYER_PLAY:
				start( true );
				break;
			case PlayController.ACTION_PLAYER_PAUSE:
				pause( true );
				break;
			case PlayController.ACTION_PLAYER_STOP:
				stop();
				break;
			case PlayController.ACTION_PLAYER_PLAY_PREVIOUS:
				toPrevious();
				break;
			case PlayController.ACTION_PLAYER_PLAY_NEXT:
				toNext();
				break;
			case PlayController.ACTION_PLAYER_MAIN_ACTIVITY:
				startMainActivity( this );
				break;
			case PlayController.ACTION_PLAYER_EMPTY:
				// Just for init
				break;
			default:
				Log.w( "Unsupported action type", intent );
				break;
		}
	}

	private void startMainActivity(Context context) {
		Intent intent = new Intent( context, MainActivity.class );
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity(intent);
	}

	static public Notification buildNotification(Context context, PlayInfo playInfo ) {
		Uri currentPlaying = playInfo.refreshCurrentPlaying();
		String mediaTitle = "";
		if( currentPlaying != null ) {
			MediaInfo mi = new MediaInfo( context, currentPlaying );
			mediaTitle = mi.getTitle();
		}
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi
			= PendingIntent.getActivity(context, 0, intent,
			                			PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder
			= new NotificationCompat.Builder( context );
		builder.setSmallIcon( R.drawable.player )
			   .setContentTitle( context.getText( R.string.app_name ) )
			   .setContentText( mediaTitle )
			   .setContentIntent(pi)
			   .setStyle( new NotificationCompat.MediaStyle() );
		Notification nf = builder.build();
		nf.flags |= Notification.FLAG_ONGOING_EVENT;
		
		return nf;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		Log.d(focusChange);
		switch (focusChange) {
	        case AudioManager.AUDIOFOCUS_GAIN:
	            if (player == null) {
	            	initPlayer();
	            }
	            break;
	        case AudioManager.AUDIOFOCUS_LOSS:
	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	            player.pause( true );
	            break;

	        default:
	        	Log.d( focusChange );
	        	break;
	    }
	}

}
