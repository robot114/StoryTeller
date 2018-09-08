package com.zsm.storyteller.play;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.zsm.android.util.IntentUtil;
import com.zsm.log.Log;
import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE;
import com.zsm.storyteller.play.audio.listener.AudioDataListener.DATA_FORMAT;
import com.zsm.storyteller.play.audio.listener.PauseAudioDataListener;
import com.zsm.storyteller.preferences.Preferences;
import com.zsm.storyteller.ui.MainActivity;
import com.zsm.storyteller.ui.PlayerView;
import com.zsm.storyteller.ui.StoryTellerAppWidgetProvider;

public class PlayService extends Service
				implements PlayController, OnAudioFocusChangeListener, PlayerNotifier {

	private static final int MINUTE_TO_MILLIS = 60*1000;

	private static final int NOTIFICATION_REQUEST_CODE = 1;

	public static final int NOTIFICATION_ID = 1;
	
	private IBinder binder = null;
	private StoryPlayer player;
	private PlayInfo playInfo;
	
	private PlayControllerReceiver receiver;
	private AudioManager audioManager;
	private ComponentName buttonReceiverCompName;
	private AudioDataReceiver audioDataReceiver;

	private PauseAudioDataListener mPauseDataListener;

	private Runnable mSleepRunner;

	private Handler mSleepHandler;

	public final class ServiceBinder extends Binder {
		public PlayService getService() {
		    return PlayService.this;
		}
	}

	public PlayService() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initPlayer();
		Notification notification
			= buildNotification( this, playInfo.refreshCurrentPlaying(),
							     player.getState() );
		
		startForeground(NOTIFICATION_ID, notification);
		
		receiver = new PlayControllerReceiver( );
		IntentFilter filter
			= new IntentFilter( AudioManager.ACTION_AUDIO_BECOMING_NOISY );
		registerReceiver( receiver, filter);
		filter
			= new IntentFilter( Intent.ACTION_HEADSET_PLUG );
		registerReceiver( receiver, filter);
		filter
			= new IntentFilter( PlayController.ACTION_UPDATE_PLAY_PAUSE_TYPE );
		registerReceiver( receiver, filter);
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		buttonReceiverCompName
			= new ComponentName( getPackageName(), 
								 MediaButtonReceiver.class.getName() );
		audioManager.registerMediaButtonEventReceiver(buttonReceiverCompName);

		allowToPlay();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		RemoteViews remoteViews
			= new RemoteViews(this.getPackageName(), R.layout.main_widget);
		AppWidgetManager appWidgetManager
			= AppWidgetManager.getInstance(this.getApplicationContext());
		ComponentName widgets
			= new ComponentName(this, StoryTellerAppWidgetProvider.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widgets);
		remoteViews.setImageViewResource( R.id.imageViewWidgetPlay,
				  						  R.drawable.widget_play);
		appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
		player.disableAudioListener();
	}

	private void initPlayer() {
		playInfo = Preferences.getInstance().readPlayListInfo();
		player = new StoryPlayer( this, this );
		// Do not need to set play info at this time. The service will update it later.
//		setPlayInfo( playInfo );
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
	synchronized public void onDestroy() {
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
	public void play(Uri uri, int startPosition) {
		if( allowToPlay() ) {
			player.play(uri, startPosition);
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
	public void pause(boolean updateView) {
		player.pause(updateView);
		if( updateView ) {
			audioManager.abandonAudioFocus( this );
		}
	}

	@Override
	public void start(boolean updateView) {
		if( updateView ) {
			if( allowToPlay() ) {
				player.start(updateView);
			} else {
				promptNotAllowed();
			}
		} else {
			player.start(updateView);
		}
	}

	@Override
	public void playPause() {
		if( getState() == PLAYER_STATE.STARTED || allowToPlay() ) {
			player.playPause();
			if( isPlayToSleep() ) {
				startSleepTimer();
			}
		} else {
			promptNotAllowed();
		}
		
		if( !inPlayingState() ) {
			audioManager.abandonAudioFocus(this);
		}
	}

	private void startSleepTimer() {
		if( mSleepHandler == null ) {	// No sleep runner running
			mSleepHandler = new Handler( Looper.getMainLooper() );
			if( mSleepRunner == null ) {
				mSleepRunner = new Runnable() {
					@Override
					public void run() {
						if( isPlayToSleep() ) {
							pause( true );
						}
						mSleepHandler = null;
					}
				};
			}
			
			long time = Preferences.getInstance().getPlaySleepTime() * MINUTE_TO_MILLIS;
			mSleepHandler.postDelayed( mSleepRunner, time );
		}
	}
	
	private void stopSleepTimer() {
		if( mSleepHandler != null ) {
			mSleepHandler.removeCallbacks(mSleepRunner);
		}
		mSleepHandler = null;
	}

	private boolean isPlayToSleep() {
		return Preferences.getInstance().getPlayPauseType()
				== PLAY_PAUSE_TYPE.TO_SLEEP;
	}
	
	private boolean inPlayingState() {
		PLAYER_STATE playerState = player.getState();
		return playerState  == PLAYER_STATE.PAUSED 
				|| playerState == PLAYER_STATE.STARTED
				|| playerState == PLAYER_STATE.PREPARED;
	}
	
	@Override
	public void setPlayInfo(PlayInfo playInfo) {
		if( playInfo == null ) {
			return;
		}
		
		this.playInfo = playInfo;
		player.setPlayInfo(playInfo);
		showNotification(player.getState());
	}

	private void handleIntent(Intent intent) {
		Log.d(intent);
		if( intent == null ) {
			return;
		}
		switch( intent.getAction() ) {
			case ACTION_PLAYER_PLAY_PAUSE:
				playPause();
				break;
			case ACTION_PLAYER_PLAY:
				Uri uri = intent.getParcelableExtra( KEY_PLAY_ITEM );
				int sp = intent.getIntExtra( KEY_MEDIA_POSITION, 0 );
				play( uri, sp );
				break;
			case ACTION_PLAYER_START:
				start( shouldUpdateView( intent ) );
				break;
			case ACTION_PLAYER_PAUSE:
				pause( shouldUpdateView( intent ) );
				break;
			case ACTION_PLAYER_STOP:
				stop();
				break;
			case ACTION_PLAYER_PLAY_PREVIOUS:
				toPrevious();
				break;
			case ACTION_PLAYER_PLAY_NEXT:
				toNext();
				break;
			case ACTION_PLAYER_PLAY_FAST_FORWARD:
				forward();
				break;
			case ACTION_PLAYER_PLAY_REWIND:
				rewind();
				break;
			case ACTION_PLAYER_SEEK_TO:
				if( intent.hasExtra( KEY_MEDIA_POSITION ) ) {
					int position
						= intent.getIntExtra( KEY_MEDIA_POSITION, 0 );
					seekTo( position );
				}
				break;
			case ACTION_PLAYER_SET_PLAY_INFO:
				if( intent.hasExtra( KEY_PLAYER_PLAY_INFO ) ) {
					PlayInfo pi
						= intent.getParcelableExtra( KEY_PLAYER_PLAY_INFO );
					setPlayInfo( pi );
				}
				break;
			case ACTION_PLAYER_MAIN_ACTIVITY:
				startMainActivity( this );
				break;
			case ACTION_PLAYER_EMPTY:
				// Just for init
				break;
			case ACTION_GET_PLAYER_STATE:
				giveStateBack( intent );
				break;
			case ACTION_ENABLE_CAPTURE:
				if( intent.hasExtra(KEY_ENABLE_CAPTURE) ) {
					boolean enabled
						= intent.getBooleanExtra(KEY_ENABLE_CAPTURE, false );
					String source = intent.getStringExtra( KEY_CAPTURE_SOURCE );
					player.enableAudioListener( source, enabled );
				}
				break;
			default:
				Log.w( "Unsupported action", intent );
				break;
		}
	}

	private boolean shouldUpdateView( Intent intent ) {
		return intent.getBooleanExtra( KEY_PLAYER_UPDATE_VIEW, true );
	}
	
	private void startMainActivity(Context context) {
		Intent intent = new Intent( context, MainActivity.class );
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity(intent);
	}

	private void giveStateBack(Intent intent) {
		ResultReceiver rr = extractResultReceiver(intent);
		Bundle b = new Bundle();
		b.putString( KEY_PLAYER_STATE, player.getState().name() );
		rr.send( REQUEST_RETRIEVE_CODE, b );
	}

	private ResultReceiver extractResultReceiver(Intent intent) {
		ResultReceiver rr
			= intent.getParcelableExtra( 
						KEY_PLAYER_RESULT_RECEIVER );
		return rr;
	}

	static public Notification buildNotification(Context context, Uri currentPlaying,
												 PLAYER_STATE state ) {
		String mediaTitle = "";
		if( currentPlaying != null ) {
			MediaInfo mi = new MediaInfo( context, currentPlaying );
			mediaTitle = mi.getTitle();
		}
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi
			= PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, intent,
			                			PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder
			= new NotificationCompat.Builder( context );
		NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
		builder.setSmallIcon( R.drawable.player )
			   .setContentTitle( context.getText( R.string.app_name ) )
			   .setContentText( mediaTitle )
			   .setContentIntent(pi)
			   .setStyle( style );

        builder.addAction( generateAction( context, android.R.drawable.ic_media_previous,
        				   				   ACTION_PLAYER_PLAY_PREVIOUS ) );
        builder.addAction( generateAction( context, android.R.drawable.ic_media_rew,
        				   				   ACTION_PLAYER_PLAY_REWIND ) );
        builder.addAction( generatePlayPauseAction( context, state ) );
        builder.addAction( generateAction( context, android.R.drawable.ic_media_ff,
        				   				   ACTION_PLAYER_PLAY_FAST_FORWARD ) );
        builder.addAction( generateAction( context, android.R.drawable.ic_media_next,
        				   				   ACTION_PLAYER_PLAY_NEXT ) );
        
        style.setShowActionsInCompactView(0,1,2,3,4);
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

	@Override
	public PLAYER_STATE getState() {
		return player.getState();
	}

	@Override
	public void enableAudioListener(String source, boolean enabled) {
		player.enableAudioListener(source, enabled);
	}
	
	private class PlayControllerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			switch( intent.getAction() ) {
				case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
					Log.d( "Audio became noisy" );
					if( Preferences.getInstance().getPauseWhenNoisy() ) {
						
						player.pause( true );
					}
					saveVolume();
					break;
				case Intent.ACTION_HEADSET_PLUG:
					plugHeadset( context, intent );
					break;
				case PlayController.ACTION_UPDATE_PLAY_PAUSE_TYPE:
					changePlayType(intent);
					break;
			}
		}
	}
	
	private void plugHeadset(Context context, Intent intent) {
        int state = intent.getIntExtra("state", -1);
		
        switch (state) {
	        case 0:
	        	// The volume is saved when the audio becomes noisy
	            Log.d( "Headset is unplugged" );
	            break;
	        case 1:
	        	int volume = restoreVolume( context );
	            Log.d("Headset is plugged, volume set", volume);
	            break;
	        default:
	            Log.w("Invalid headset state", state);
        }
    }

	private int restoreVolume(Context context) {
		int volume = -1;
		StoryTellerApp app = (StoryTellerApp) context.getApplicationContext();
		if( ( app != null && app.getMainActivityInForeground() ) 
			|| player.getState() == PLAYER_STATE.STARTED ) {
			
			AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			Preferences pref = Preferences.getInstance();
			volume = pref.getHeadsetMusicVolume();
			audio.setStreamVolume( AudioManager.STREAM_MUSIC, volume, 0);
		}
		
		return volume;
	}

	private int saveVolume() {
		int volume;
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Preferences pref = Preferences.getInstance();
		volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		pref.setHeadsetMusicVolume( volume );
		return volume;
	}

	private void changePlayType(Intent intent) {
		PLAY_PAUSE_TYPE type
			= IntentUtil.getEnumValueIntent(intent,
											PlayController.KEY_PLAY_PAUSE_TYPE,
											PLAY_PAUSE_TYPE.class,
											PLAY_PAUSE_TYPE.CONTINUOUS );
		PLAYER_STATE state = player.getState();
		
		changePlayPauseType(type, state);
	}

	public void changePlayPauseType(PLAY_PAUSE_TYPE type, PLAYER_STATE state) {
		switch( type ) {
			case CONTINUOUS:
				playContinously();
				break;
			case TO_PAUSE:
				enableCapture( state );
				break;
			case TO_SLEEP:
				startSleepTimer();
				break;
			default:
				Log.w("Invalid play pause type", type );
				playContinously();
				break;
		}
	}
	
	private void playContinously() {
		disableCapture();
		stopSleepTimer();
	}

	private void enableCapture( PLAYER_STATE newState ) {
		if( player == null || newState.ordinal() < PLAYER_STATE.STARTED.ordinal()
				|| newState.ordinal() >= PLAYER_STATE.END.ordinal() ) {
			return;
		}
		
		if( audioDataReceiver == null ) {
			mPauseDataListener = new PauseAudioDataListener( player );
			mPauseDataListener.setCaptureRate( player.getAudioCaptureRate() );
			Preferences preferences = Preferences.getInstance();
			mPauseDataListener
				.setSilenceTimeToPause( preferences.getSilenceTimeToPause() );
			mPauseDataListener
				.setSilenceToSilence( preferences.getSilenceToilence() );
			audioDataReceiver = new AudioDataReceiver( mPauseDataListener );
		}
		
		audioDataReceiver.registerMe(this);
		player.enableAudioListener( getClass().getName(), true );
	}

	private void disableCapture() {
		if( audioDataReceiver != null ) {
			audioDataReceiver.unregisterMe( this );
		}
		if( player != null ) {
			player.enableAudioListener( getClass().getName(), false );
		}
	}

    private static Action generateAction( Context context, int icon, String intentAction ) {
        Intent intent = new Intent( context, PlayService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent
        	 = PendingIntent.getService(context, NOTIFICATION_REQUEST_CODE, intent, 0);
        return new NotificationCompat.Action.Builder( icon, "", pendingIntent ).build();
    }
    
    private static Action generatePlayPauseAction( Context context, PLAYER_STATE state ) {
    	int icon;
    	if( state == PLAYER_STATE.STARTED ) {
    		icon = android.R.drawable.ic_media_pause;
    	} else {
    		icon = android.R.drawable.ic_media_play;
    	}
        return generateAction( context, icon, ACTION_PLAYER_PLAY_PAUSE );
    }
    
    private void showNotification(PLAYER_STATE state) {
		Notification notification
			= buildNotification( this, playInfo.refreshCurrentPlaying(), state );
		
	    NotificationManagerCompat
	    	.from(this).notify(PlayService.NOTIFICATION_ID, notification);
    }

	@Override
	public void stateChanged(PLAYER_STATE newState) {
		PLAY_PAUSE_TYPE playPauseType
			= Preferences.getInstance().getPlayPauseType();
		
		changePlayPauseType( playPauseType, newState );
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_PLAYER_STATE );
		intent.putExtra( PlayerView.KEY_PLAYER_STATE, newState.name() );
		sendBroadcast(intent);
		showNotification( newState );
	}

	@Override
	public void newAudioData(DATA_FORMAT format, int samplingRate, byte[] data) {
		Intent intent = new Intent( AudioDataReceiver.ACTION_UPDATE_AUDIO_DATA );
		intent.putExtra( AudioDataReceiver.KEY_AUDIO_DATA, data );
		intent.putExtra( AudioDataReceiver.KEY_AUDIO_DATA_FORMAT, format );
		sendBroadcast(intent);
	}

	@Override
	public void updateTime(int ellapsed, int duration) {
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_ELLAPSED_TIME );
		intent.putExtra( PlayerView.KEY_ELLAPSED_TIME, ellapsed );
		intent.putExtra( PlayerView.KEY_DURATION, duration );
		sendBroadcast(intent);
	}

	@Override
	public void updateDataSource(Uri uri) {
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_DATA_SOURCE );
		intent.putExtra( PlayerView.KEY_DATA_SOURCE, uri );
		sendBroadcast(intent);
	}

	@Override
	public void updatePlayList(PlayInfo pi) {
		Intent intent = new Intent( PlayerView.ACTION_UPDATE_PLAY_INFO );
		intent.putExtra(PlayerView.KEY_PLAY_INFO, pi);
		sendBroadcast(intent);
	}

	@Override
	public void notifyCannotPlay(int promptId) {
		Toast.makeText( this, promptId, Toast.LENGTH_LONG ).show();
	}
}
