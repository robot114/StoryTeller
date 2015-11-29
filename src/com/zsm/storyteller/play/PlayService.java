package com.zsm.storyteller.play;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController.PLAYER_STATE;
import com.zsm.storyteller.preferences.Preferences;
import com.zsm.storyteller.ui.MainActivity;

public class PlayService extends IntentService implements PlayController {

	private static final int NOTIFICATION_ID = 0;

	public PlayService() {
		super(PlayService.class.getName());
		setIntentRedelivery(true);
		
		Log.d( this );
	}

	public final class ServiceBinder extends Binder {
		public PlayService getService() {
		    return PlayService.this;
		}
	}

	private IBinder binder = null;
	private PlayController player;
	private Notification notification;
	
	@Override
	public void onCreate() {
		super.onCreate();
		player = new StoryPlayer( this );
		updatePlayInfo( Preferences.getInstance().readPlayListInfo() );
//		notification = buildNotification();
//
//	    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//	    notificationManager.notify(NOTIFICATION_ID, notification);
//	    NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);
//		startForeground(NOTIFICATION_ID, notification);
	}

	private Notification buildNotification() {
		Context applicationContext = getApplicationContext();
		PendingIntent pi
		= PendingIntent.getActivity(applicationContext, 0,
		                			new Intent(applicationContext, MainActivity.class),
		                			PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder
			= new NotificationCompat.Builder( applicationContext );
		builder.setSmallIcon( R.drawable.player )
			   .setContentTitle( applicationContext.getText( R.string.app_name ) )
			   .setContentText( "" )
			   .setContentIntent(pi)
			   .setStyle( new NotificationCompat.MediaStyle() );
		Notification nf = builder.build();
		nf.flags |= Notification.FLAG_ONGOING_EVENT;
		
		return nf;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if( binder == null ) {
			binder = new ServiceBinder();
		}
		return binder;
	}

	@Override
	public void onDestroy() {
		player.onDestroy();
		super.onDestroy();
	}

	@Override
	public void selectOneToPlay(Uri uri, long startPosition) {
		player.selectOneToPlay(uri, startPosition);
	}

	@Override
	public void stop() {
		player.stop();
	}

	@Override
	public void toNext() {
		player.toNext();
	}

	@Override
	public void toPrevious() {
		player.toPrevious();
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
	}

	@Override
	public void start(boolean updateView) {
		player.start(updateView);
	}

	@Override
	public boolean inPlayingState() {
		return player.inPlayingState();
	}

	@Override
	public void playPause() {
		player.playPause();
	}

	@Override
	public void updatePlayInfo(PlayInfo playInfo) {
		player.updatePlayInfo(playInfo);
	}

	@Override
	public PlayInfo getPlayInfo() {
		return player.getPlayInfo();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(intent);
		switch( intent.getAction() ) {
			case PlayController.ACTION_PLAYER_PLAY:
				player.playPause();
				break;
			case PlayController.ACTION_PLAYER_PLAY_NEXT:
				player.toNext();
				break;
			case PlayController.ACTION_PLAYER_MAIN_ACTIVITY:
				startMainActivity( this );
				break;
			case PlayController.ACTION_PLAYER_EMPTY:
				// Just for init
				break;
			default:
				Log.w( "Invalid action type", intent );
				break;
		}
	}

	private void startMainActivity(Context context) {
		Intent intent = new Intent( context, MainActivity.class );
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
						| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
