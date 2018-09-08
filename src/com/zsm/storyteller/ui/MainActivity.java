package com.zsm.storyteller.ui;

import java.security.InvalidParameterException;
import java.util.List;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.android.ui.TimedProgressBar;
import com.zsm.android.ui.quickAction.QuickAction;
import com.zsm.android.ui.quickAction.QuickAction.OnActionItemClickListener;
import com.zsm.log.Log;
import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE;
import com.zsm.storyteller.play.AudioDataReceiver;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayController.PLAY_ORDER;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;
import com.zsm.storyteller.play.PlayService;
import com.zsm.storyteller.play.RemotePlayer;
import com.zsm.storyteller.play.audio.listener.AudioDataListener;
import com.zsm.storyteller.preferences.MainPreferenceFragment;
import com.zsm.storyteller.preferences.Preferences;

public class MainActivity extends FragmentActivity 
				implements PlayerView, OnChildClickListener {

	private ImageView playPause;
	private TextView playingText;
	private ExpandableListView playListView;
	private MediaInfoListAdapter playListAdapter;
	private ImageView playOrderView;
	private TimedProgressBar progressBar;
	
	private Drawable playIcon;
	private Drawable pauseIcon;
	
	private PlayController player;
	private BroadcastReceiver receiver;
	private PLAYER_STATE playerState;
	private Uri currentPlaying;
	private MainFragmentPagerAdapter adapterViewPager;
	private ViewPager viewPager;
	private AudioDataReceiver visualizeReceiver;

	public MainActivity() {
		super();
		player = new RemotePlayer( this );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		playPause = (ImageView)findViewById( R.id.imageViewPlay );
		playPause.setOnLongClickListener( new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showPlayPopupMenu( v );
				return true;
			}
			
		} );
		
		playingText = (TextView)findViewById( R.id.textViewPlayingFile );
		
		initListView();
		initPlayIcons();
		initProgressBar();
		
		playOrderView = (ImageView)findViewById( R.id.imageViewPlayOrder );
		playOrderView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLAY_ORDER order = Preferences.getInstance().getPlayOrder();
				PLAY_ORDER enums[] = PLAY_ORDER.values();
				order = enums[ (order.ordinal()+1)%enums.length ];
				Preferences.getInstance().setPlayOrder(order);
				setPlayOrderIcon( order );
			}
		} );
		
		registerPlayerViewReceiver();
		
		viewPager = (ViewPager) findViewById(R.id.viewInfoViewPager);
		adapterViewPager
			= new MainFragmentPagerAdapter(getSupportFragmentManager(),
										   this,
										   savedInstanceState );
		
		viewPager.setAdapter(adapterViewPager);
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			// This method will be invoked when a new page becomes selected.
			@Override
			public void onPageSelected(int position) {
				setVisualizerEnabled( isVisualizerFragment( position ) );
				adapterViewPager.setDataSource(position, currentPlaying);
			}
			
			// This method will be invoked when the current page is scrolled
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			
			// Called when the scroll state changes: 
			// SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		
		new Thread( new Runnable() {
			@Override
			public void run() {
				initPlayer();
			}
		}, "PlayerInit" ).start();
		
		setVolumeControlStream( AudioManager.STREAM_MUSIC );
	}

	private void initPlayer() {
		Looper.prepare();
		player.setPlayInfo( Preferences.getInstance().readPlayListInfo() );
		playListAdapter.setPlayer(player);
		PLAYER_STATE playerStateNow = player.getState();
		if( ( playerStateNow == null || playerStateNow == PLAYER_STATE.IDLE ) 
			&& Preferences.getInstance().autoStartPlaying() ) {
			
			player.playPause();
		}
	}

	private void initPlayIcons() {
		Resources r = getResources();
		playIcon = getPlayIcon(Preferences.getInstance().getPlayPauseType());
		pauseIcon = r.getDrawable( R.drawable.pause );
	}

	private void initListView() {
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
		playListAdapter.notifyDataSetChanged();
	}

	private void initProgressBar() {
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
				if ( playerState == PLAYER_STATE.STARTED ) {
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
		filter = new IntentFilter(PlayController.ACTION_UPDATE_PLAY_PAUSE_TYPE);
		registerReceiver(receiver, filter);
	}
	
	private void showPlayPopupMenu(View v) {
		//create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout 
        //orientation
		final QuickAction quickAction = new QuickAction(this, QuickAction.VERTICAL);
		quickAction.setRootViewId( R.layout.play_pause_menu_vertical );
		quickAction.addActionItems(this, false,
				new int[]{PLAY_PAUSE_TYPE.CONTINUOUS.ordinal(),
						  PLAY_PAUSE_TYPE.TO_PAUSE.ordinal(),
						  PLAY_PAUSE_TYPE.TO_SLEEP.ordinal()},
				new int[]{-1, -1, -1},
				new int[]{R.drawable.play,
						  R.drawable.play_to,
						  R.drawable.timer_play});
		
		quickAction.setOnActionItemClickListener( new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				PLAY_PAUSE_TYPE[] typeValues = PLAY_PAUSE_TYPE.values();
				if( actionId >= typeValues.length ) {
					Log.w( "Invalid action id: ", actionId );
					return;
				}
				
				PLAY_PAUSE_TYPE type = typeValues[actionId];
				Preferences.getInstance().setPlayTypeToPause(type);
				Preferences.sendPlayTypeChangeMessage(MainActivity.this, type);
			}
		});
		
		quickAction.show(v);
	}

	@Override
	protected void onResume() {
		super.onResume();
		NotificationManagerCompat.from(this).cancel(PlayService.NOTIFICATION_ID);
		Preferences preferences = Preferences.getInstance();
		setPlayOrderIcon(preferences.getPlayOrder());
		if( isVisualizerFragment(viewPager.getCurrentItem()) ) {
			setVisualizerEnabled( true );
		}
		
		((StoryTellerApp)getApplication()).setMainActivityInForeground( true );
	}

	private boolean isVisualizerFragment(int currentPosition) {
		return currentPosition == MainFragmentPagerAdapter.VISUALIZER_POSITION;
	}

	private void setPlayOrderIcon(PLAY_ORDER order) {
		playOrderView.setImageResource( 
				MainPreferenceFragment.PLAY_ORDER_ICONS[ order.ordinal() ] );
	}

	@Override
	protected void onPause() {
		updateNotification();
		setVisualizerEnabled( false );
		((StoryTellerApp)getApplication()).setMainActivityInForeground( false );
		super.onPause();
	}

	private void updateNotification() {
		Notification notification
			= PlayService.buildNotification( this, currentPlaying, playerState );
	    NotificationManagerCompat
	    	.from(this).notify(PlayService.NOTIFICATION_ID, notification);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		receiver = null;
		if( visualizeReceiver != null && visualizeReceiver.isRegistered() ) {
			visualizeReceiver.unregisterMe( this );
		}
		visualizeReceiver = null;
		if( isVisualizerFragment(viewPager.getCurrentItem()) ) {
			setVisualizerEnabled( false );
		}
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
		((StoryTellerApp)getApplication())
			.getPlayFileHandler().openOne(this, currentPlaying, player);
	}
	
	public void onOpenFolder( View v ) {
		((StoryTellerApp)getApplication())
			.getPlayFileHandler().openFolder( this, currentPlaying, player );
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
	
	public void onChangeMode( final MenuItem item ) {
		new AlertDialog.Builder(this)
			.setTitle( R.string.app_name )
			.setMessage(R.string.modeChangeConfirm)
        	.setCancelable (false)
            .setPositiveButton( R.string.toContinue,
                new DialogInterface.OnClickListener () {
            		public void onClick (DialogInterface dialog, int buttonId) {
            			changeMode( item.getItemId() );
            		} })
        	.setNegativeButton( android.R.string.cancel, null )
        	.setIcon (android.R.drawable.ic_dialog_alert)
        	.show();
	}
	
	private void changeMode(int menuId) {
		Preferences pref = Preferences.getInstance();
		PLAY_PAUSE_TYPE ppt;
		switch( menuId ) {
			case R.id.itemModeMusic:
				ppt = PLAY_PAUSE_TYPE.CONTINUOUS;
				pref.setSkipHeader( false, pref.getSkipHeaderValue() );
				pref.setScreenOnWhenPlay( false );
				break;
			case R.id.itemModeReading:
				ppt = PLAY_PAUSE_TYPE.CONTINUOUS;
				pref.setSkipHeader( true, pref.getSkipHeaderValue() );
				pref.setScreenOnWhenPlay( false );
				pref.setPlayOrder( PLAY_ORDER.BY_NAME );
				break;
			case R.id.itemModeStudy:
				ppt = PLAY_PAUSE_TYPE.TO_PAUSE;
				pref.setSkipHeader( false, pref.getSkipHeaderValue() );
				pref.setAutoPlayingAtStarting( false );
				pref.setScreenOnWhenPlay( true );
				pref.setPlayOrder( PLAY_ORDER.BY_NAME );
				break;
			default:
				Log.e( new InvalidParameterException( "invlaid menu id" ),
					   "Invlid menu id", menuId );
				return;
		}
		
		pref.setPlayTypeToPause( ppt );
		Preferences.sendPlayTypeChangeMessage( this, ppt );
		setKeepScreenOn( playerState );
		setPlayOrderIcon( pref.getPlayOrder() );
		
		Toast.makeText( this, R.string.infoOfModeChanged, Toast.LENGTH_SHORT ).show();
	}

	public void selectOneToPlay(Uri uri) {
		player.play(uri, 0 );
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
								int groupPosition, int childPosition, long id) {
		selectOneToPlay( (Uri) playListAdapter.getGroup(groupPosition) );
		return true;
	}

	@Override
	public void updateTime(int currentPosition, int duration) {
		progressBar.updateTime( currentPosition );
	}

	@Override
	public void updatePlayerState(PLAYER_STATE state) {
		playerState = state;
		updatePlayPauseIcon(state);
		setKeepScreenOn( state );
	}

	private void setVisualizerEnabled(final boolean enabled) {
		String captureSource = AudioDataReceiver.class.getName();
		if( enabled ) {
			if( visualizeReceiver == null ) {
				AudioDataListener vl
					= (AudioDataListener) adapterViewPager
							.getItem( MainFragmentPagerAdapter.VISUALIZER_POSITION );
				visualizeReceiver = new AudioDataReceiver( vl );
			}
			visualizeReceiver.registerMe( this );
		} else {
			if( visualizeReceiver != null ) {
				visualizeReceiver.unregisterMe( this );
			}
		}
		player.enableAudioListener( captureSource, enabled );
	}

	@Override
	public void setDataSource(Context context, Uri uri) {
		this.currentPlaying = uri;
		adapterViewPager.setDataSource( viewPager.getCurrentItem(), uri );
		playingText.setText( uri.getLastPathSegment() );
		MediaInfo mi = new MediaInfo( context, uri );
		progressBar.setDuration( mi.getDuration() );
		updateNotification();
		
		int positionOf = playListAdapter.getPositionOf(uri);
		positionOf
			= ( positionOf < 0 || positionOf >= playListAdapter.getGroupCount() )
				? 0 : positionOf;
		makeCurrentItemVisible( positionOf );
	}

	public void makeCurrentItemVisible(int position) {
	    int first = playListView.getFirstVisiblePosition();
	    int last = playListView.getLastVisiblePosition();

	    if (position < first || position >= last ) {
	        playListView.smoothScrollToPosition(position);
	    }
	}
	
	@Override
	public void updatePlayList(List<Uri> playList) {
		playListAdapter.setData( playList );
		playListAdapter.notifyDataSetChanged();
	}

	@Override
	public void updatePlayPauseType(Context context, PLAY_PAUSE_TYPE type) {
		updatePlayPauseType(type);
	}

	private void updatePlayPauseType(PLAY_PAUSE_TYPE type) {
		playIcon = getPlayIcon(type);
		updatePlayPauseIcon(playerState);
	}

	public Drawable getPlayIcon(PLAY_PAUSE_TYPE type) {
		int iconId;
		
		switch( type ) {
			case CONTINUOUS:
				iconId = R.drawable.play;
				break;
			case TO_PAUSE:
				iconId = R.drawable.play_to;
				break;
			case TO_SLEEP:
				iconId = R.drawable.timer_play;
				break;
			default:
				iconId = R.drawable.play;
				Log.w( "Invalid play pause type", type );
				break;
		}
		
		Drawable icon = getResources().getDrawable( iconId );
		return icon;
	}
	
	private void updatePlayPauseIcon(PLAYER_STATE state) {
		boolean changeToStart = state == PLAYER_STATE.STARTED;
		Drawable icon = changeToStart ? pauseIcon : playIcon;
		playPause.setImageDrawable(icon);
	}

	private void setKeepScreenOn(PLAYER_STATE state) {
		playPause.setKeepScreenOn( state == PLAYER_STATE.STARTED 
						&& Preferences.getInstance().getScreenOnWhenPlay() );
	}
}
