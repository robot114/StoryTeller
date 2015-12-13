package com.zsm.storyteller.ui;

import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.zsm.log.Log;
import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayController.PLAYER_STATE;
import com.zsm.storyteller.play.PlayService;
import com.zsm.storyteller.play.RemotePlayer;
import com.zsm.storyteller.preferences.Preferences;

public class MainActivity extends Activity
				implements PlayerView, OnChildClickListener {

	private ImageView playPause;
	private TextView playingText;
	private ExpandableListView playListView;
	private MediaInfoListAdapter playListAdapter;
	private MediaInfoView mediaInfoView;
	
	private Drawable playIcon;
	private Drawable pauseIcon;
	
	private TimedProgressBar progressBar;

	private List<Uri> playList;
	private PlayController player;
	private BroadcastReceiver receiver;
	private PLAYER_STATE playerState;
	private Uri currentPlaying;

	public MainActivity() {
		super();
		player = new RemotePlayer( this );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		playPause = (ImageView)findViewById( R.id.imageViewPlay );
		playingText = (TextView)findViewById( R.id.textViewPlayingFile );
		
		playListView = (ExpandableListView)findViewById( R.id.listPlayList );
		playListAdapter = new MediaInfoListAdapter( this, playListView );
		playListView.setAdapter(playListAdapter);
		playListView.setOnChildClickListener( this );
		playListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
										int groupPosition, long id) {
				
				playListView.expandGroup(groupPosition);
				return true;
			}
		});
		
		mediaInfoView = (MediaInfoView)findViewById( R.id.viewMediaInfo );
		mediaInfoView.setVisibility( View.INVISIBLE );
		
		playIcon = getResources().getDrawable( R.drawable.play );
		pauseIcon = getResources().getDrawable( R.drawable.pause );
		
		playListAdapter.setData(playList);
		playListAdapter.notifyDataSetChanged();
		
		progressBar = (TimedProgressBar)findViewById( R.id.timedProgressBar );
		
		progressBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {

			private boolean trackingDragging;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				if (fromUser ) {
					progressBar.updateTime( progress );
		        }
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if ( playerState == PlayController.PLAYER_STATE.STARTED ) {
					player.pause( false );
					trackingDragging = true;
				}
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				player.seekTo(seekBar.getProgress());
				if ( trackingDragging ) {
					player.start( false );
				}
				trackingDragging = false;
			}
			
		} );
		
		registerPlayerViewReceiver();
		
		new Thread( new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				player.setPlayInfo( Preferences.getInstance().readPlayListInfo() );
				playListAdapter.setPlayer(player);
				PLAYER_STATE playerStateNow = player.getState();
				Log.d( playerStateNow );
				if( ( playerStateNow == null 
						|| playerStateNow == PlayController.PLAYER_STATE.IDLE ) 
					&& Preferences.getInstance().autoStartPlaying() ) {
					
					player.playPause();
				}
			}
		}, "PlayerInit" ).start();
	}

	private void registerPlayerViewReceiver() {
		final PlayerViewReceiver pvr = new PlayerViewReceiver(this);
		
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				pvr.onReceive(context, intent);
			}
		};
		
		IntentFilter filter = new IntentFilter(PlayerView.ACTION_UPDATE_DATA_SOURCE);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(PlayerView.ACTION_UPDATE_PLAYER_STATE);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(PlayerView.ACTION_UPDATE_ELLAPSED_TIME);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(PlayerView.ACTION_UPDATE_PLAY_INFO);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		NotificationManagerCompat.from(this).cancel(PlayService.NOTIFICATION_ID);
	}

	@Override
	protected void onPause() {
		Notification notification
			= PlayService.buildNotification( this, currentPlaying );
	    NotificationManagerCompat
	    	.from(this).notify(PlayService.NOTIFICATION_ID, notification);
			
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater mi = getMenuInflater();
		mi.inflate( R.menu.main, menu);
		return true;
	}
	
	public void onOpenOne( View v ) {
		((StoryTellerApp)getApplication()).getPlayFileHandler().openOne(this, player);
	}
	
	public void onOpenFolder( View v ) {
		((StoryTellerApp)getApplication()).getPlayFileHandler().openFolder( this, player );
	}
	
	public void onPlayPause( View v ) {
		player.playPause();
	}

	public void onStop( View v ) {
		player.stop();
	}

	public void onNext( View v ) {
		player.toNext();
	}

	public void onPrevious( View v ) {
		player.toPrevious();
	}

	public void onForward( View v ) {
		player.forward();
	}

	public void onRewind( View v ) {
		player.rewind();
	}

	public void onPreferences( MenuItem item ) {
		Intent intent = new Intent( this, MainPreferenceActivity.class );
		startActivity( intent );
	}
	
	public void selectOneToPlay(Uri uri) {
		player.play(uri, 0 );
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
								int groupPosition, int childPosition, long id) {
		selectOneToPlay( playList.get( groupPosition ) );
		return true;
	}

	@Override
	public void updateTime(int currentPosition, int duration) {
		progressBar.updateTime( currentPosition );
	}

	@Override
	public void updatePlayerState(PlayController.PLAYER_STATE state) {
		playerState = state;
		switch( state ) {
			case STARTED:
				playPause.setImageDrawable(pauseIcon);
				break;
			case PREPARED:
			case STOPPED:
			case PAUSED:
				playPause.setImageDrawable(playIcon);
				break;
			default:
				break;
		}
	}

	@Override
	public void setDataSource(Context context, Uri uri) {
		this.currentPlaying = uri;
		mediaInfoView.setDataSource(uri);
		mediaInfoView.setVisibility( View.VISIBLE );
		playingText.setText( uri.getLastPathSegment() );
		progressBar.setDuration( (int) mediaInfoView.getMediaDuration() );
	}

	@Override
	public void updatePlayList(List<Uri> playList) {
		playListAdapter.setData( playList );
		playListAdapter.notifyDataSetChanged();
	}

}
