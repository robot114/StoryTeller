package com.zsm.storyteller.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.android.ui.fileselector.FileOperation;
import com.zsm.android.ui.fileselector.FileSelector;
import com.zsm.android.ui.fileselector.OnHandleFileListener;
import com.zsm.log.Log;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.preferences.Preferences;

public class MainActivity extends Activity
				implements OnHandleFileListener, OnChildClickListener, Playable {

	private enum PLAYER_STATE { 
		IDLE, INITIALIZED, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACKCOMPLETED };
		
	private final static String[] EXTENSION = {
		".3gp", ".aac", ".flac", ".m4a", ".mp4", ".mid", ".mp3", ".xmf",
		".mxmf", ".rtx", ".rtttl", ".ota", ".imy", ".ogg", ".mkv", ".wav"
	};
	
	private ImageView playPause;
	private TextView playingText;
	private ExpandableListView playListView;
	private MediaInfoListAdapter playListAdapter;
	private MediaInfoView mediaInfoView;
	
	private MediaPlayer mediaPlayer;
	private PLAYER_STATE playerState;
	
	private Drawable playIcon;
	private Drawable pauseIcon;
	
	private List<Uri> playList;

	private PlayInfo playInfo;

	public MainActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		playPause = (ImageView)findViewById( R.id.imageViewPlay );
		playingText = (TextView)findViewById( R.id.textViewPlayingFile );
		
		playListView = (ExpandableListView)findViewById( R.id.listPlayList );
		playListAdapter = new MediaInfoListAdapter( this, this, playListView );
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
		
		mediaPlayer = new MediaPlayer();
		
		playInfo = Preferences.getInstance().readPlayListInf();
		playList = playInfo.getPlayList(EXTENSION, true);
		playListAdapter.setData(playList);
		playListAdapter.notifyDataSetChanged();
		Uri currentPlaying = playInfo.refreshCurrentPlaying();
		if( currentPlaying != null ) {
			selectOneToPlay( currentPlaying );
		}
	}
	
	public void onOpenOne( View v ) {
		new FileSelector(this, FileOperation.LOAD, this, EXTENSION ).show();
	}
	
	public void onOpenFolder( View v ) {
		new FileSelector(this, FileOperation.FOLDER, this, null ).show();
	}
	
	public void onPlayPause( View v ) {
		if( playInfo.refreshCurrentPlaying() == null ) {
			Toast.makeText( this, R.string.openPlayFileFirst, Toast.LENGTH_LONG )
				 .show();
			return;
		}
		
		if( mediaPlayer.isPlaying() ) {
			mediaPlayer.pause();
			playerState = PLAYER_STATE.PAUSED;
			playPause.setImageDrawable(playIcon);
		} else {
			play();
		}
	}

	private void play() {
		if( playerState == PLAYER_STATE.STOPPED ) {
			try {
				mediaPlayer.prepare();
			} catch (IllegalArgumentException | SecurityException
					| IllegalStateException | IOException e) {
				
				Log.e( e, "Cannot make the file to be played: "
							+ playInfo.refreshCurrentPlaying() );
				Toast.makeText( this, R.string.openFileFailed, Toast.LENGTH_LONG )
					 .show();
				return;
			}
			mediaPlayer.seekTo( 0 );
		}
		mediaPlayer.start();
		playerState = PLAYER_STATE.STARTED;
		playPause.setImageDrawable(pauseIcon);
	}

	public void onStop( View v ) {
		stop();
	}

	public void onNext( View v ) {
		Uri uri = playInfo.nextOne();
		if( uri != null ) {
			selectOneToPlay(uri);
		} else {
			stop();
		}
	}
	
	public void onPrevious( View v ) {
		Uri uri = playInfo.previousOne();
		if( uri != null ) {
			selectOneToPlay(uri);
		} else {
			stop();
		}
	}
	
	@Override
	public void handleFile(FileOperation operation, String filePath) {
		File f = new File( filePath );
		if( operation == FileOperation.FOLDER ) {
			playInfo
				= new PlayInfo( PlayInfo.LIST_TYPE.FOLDER, Uri.fromFile(f), null );
		} else {
			playInfo
				= new PlayInfo( PlayInfo.LIST_TYPE.SINGLE, Uri.fromFile(f), null );
		}

		playListAdapter.setData( playInfo.getPlayList(EXTENSION, true) );
		playListAdapter.notifyDataSetChanged();
		Uri currentPlaying = playInfo.refreshCurrentPlaying();
		if( currentPlaying == null ) {
			Toast.makeText( this, R.string.noMediaToPlay, Toast.LENGTH_LONG )
			 	 .show();
			return;
		} else {
			selectOneToPlay( currentPlaying );
		}
		
		Preferences.getInstance().savePlayListInfo( playInfo );
	}

	@Override
	public void selectOneToPlay(Uri uri) {
		playInfo.setCurrentPlaying( uri );
		mediaPlayer.reset();
		playerState = PLAYER_STATE.IDLE;
		
		try {
			mediaInfoView.setDataSource(uri);
			mediaPlayer.setDataSource(this, uri );
			mediaPlayer.prepare();
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			
			Log.e( e, "Cannot make the file to be played: " + uri );
			Toast.makeText( this, R.string.openFileFailed, Toast.LENGTH_LONG )
				 .show();
			return;
		}
		mediaInfoView.setVisibility( View.VISIBLE );
		playerState = PLAYER_STATE.PREPARED;
		playingText.setText( uri.getLastPathSegment() );
		playPause.setImageDrawable(playIcon);
		play();
	}

	private void stop() {
		mediaPlayer.stop();
		playerState = PLAYER_STATE.STOPPED;
		playPause.setImageDrawable(playIcon);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if( mediaPlayer != null ) {
			mediaPlayer.release();
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
								int groupPosition, int childPosition, long id) {
		selectOneToPlay( playList.get( groupPosition ) );
		return true;
	}
}
