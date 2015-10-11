package com.zsm.storyteller.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.android.ui.fileselector.FileOperation;
import com.zsm.android.ui.fileselector.FileSelector;
import com.zsm.android.ui.fileselector.OnHandleFileListener;
import com.zsm.log.Log;
import com.zsm.storyteller.R;
import com.zsm.storyteller.R.drawable;
import com.zsm.storyteller.R.id;
import com.zsm.storyteller.R.layout;
import com.zsm.storyteller.R.string;

public class MainActivity extends Activity implements OnHandleFileListener {

	private final class FolderOrExtFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			int extIndex = pathname.getName().lastIndexOf( '.' );
			boolean inc = false;
			if( extIndex >= 0 ) {
				String ext = pathname.getName().substring( extIndex );
				for( String e : EXTENSION ) {
					if( e.equalsIgnoreCase( ext ) ) {
						inc = true;
						break;
					}
				}
			}
			return pathname.isDirectory() || inc;
		}
	}

	private enum PLAYER_STATE { 
		IDLE, INITIALIZED, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACKCOMPLETED };
		
	private enum LIST_TYPE {
		SINGLE, FOLDER, LIST
	}
	
	private final static String[] EXTENSION = {
		".3gp", ".aac", ".flac", ".m4a", ".mp4", ".mid", ".mp3", ".xmf",
		".mxmf", ".rtx", ".rtttl", ".ota", ".imy", ".ogg", ".mkv", ".wav"
	};
	
	private ImageView playPause;
	private TextView playingText;
	private ListView playListView;
	private ArrayAdapter<File> playListAdpater;
	private MediaInfoView mediaInfoView;
	
	private MediaPlayer mediaPlayer;
	private PLAYER_STATE playerState;
	
	private Drawable playIcon;
	private Drawable pauseIcon;
	
	private LIST_TYPE listType;
	private File playingFile;
	private File playingFolder;
	private List<File> playList;

	private FolderOrExtFilter fileFilter;

	public MainActivity() {
		fileFilter = new FolderOrExtFilter();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		playPause = (ImageView)findViewById( R.id.imageViewPlay );
		playingText = (TextView)findViewById( R.id.textViewPlayingFile );
		
		playListView = (ListView)findViewById( R.id.listViewPlayList );
		playListAdpater
			= new ArrayAdapter<File>( this, android.R.layout.simple_list_item_1 );
		playListView.setAdapter(playListAdpater);
		
		mediaInfoView = (MediaInfoView)findViewById( R.id.viewMediaInfo );
		mediaInfoView.setVisibility( View.INVISIBLE );
		
		playIcon = getResources().getDrawable( R.drawable.play );
		pauseIcon = getResources().getDrawable( R.drawable.pause );
		
		mediaPlayer = new MediaPlayer();
		
		playList = new ArrayList<File>();
	}
	
	public void onOpenOne( View v ) {
		new FileSelector(this, FileOperation.LOAD, this, EXTENSION ).show();
	}
	
	public void onOpenFolder( View v ) {
		new FileSelector(this, FileOperation.FOLDER, this, null ).show();
	}
	
	public void onPlayPause( View v ) {
		if( playingFile == null ) {
			Toast.makeText( this, R.string.openPlayFileFirst, Toast.LENGTH_LONG )
				 .show();
			return;
		}
		
		if( mediaPlayer.isPlaying() ) {
			mediaPlayer.pause();
			playerState = PLAYER_STATE.PAUSED;
			playPause.setImageDrawable(playIcon);
		} else {
			if( playerState != PLAYER_STATE.PREPARED && playerState != PLAYER_STATE.PAUSED ) {
				try {
					mediaPlayer.prepare();
					mediaPlayer.seekTo( 0 );
				} catch (IllegalStateException | IOException e) {
					Log.e( e, "Cannot prepare the source: " + playingFile );
					Toast.makeText( this, R.string.openFileFailed, Toast.LENGTH_LONG )
					 	 .show();
					return;
				}
			}
			mediaPlayer.start();
			playerState = PLAYER_STATE.STARTED;
			playPause.setImageDrawable(pauseIcon);
		}
	}

	public void onStop( View v ) {
		mediaPlayer.stop();
		playerState = PLAYER_STATE.STOPPED;
		playPause.setImageDrawable(playIcon);
	}
	
	@Override
	public void handleFile(FileOperation operation, String filePath) {
		File f = new File( filePath );
		if( operation == FileOperation.FOLDER ) {
			playingFolder = new File( filePath );
			listType = LIST_TYPE.FOLDER;
			playList.clear();
			scanFolder( f, playList );
		} else {
			playingFile = f;
			playList.clear();
			playList.add( playingFile );
			listType = LIST_TYPE.SINGLE;
		}
		
		playListAdpater.clear();
		playListAdpater.addAll(playList);
		playListAdpater.notifyDataSetChanged();
		if( playList.size() > 0 ) {
			playingFile = playList.get( 0 );
		} else {
			return;
		}
		
		try {
			mediaPlayer.reset();
			playerState = PLAYER_STATE.IDLE;
			Uri uriFromFile = Uri.fromFile(playingFile);
			mediaInfoView.setDataSource(uriFromFile);
			mediaInfoView.setVisibility( View.VISIBLE );
			mediaPlayer.setDataSource(this, uriFromFile );
			mediaPlayer.prepare();
			playerState = PLAYER_STATE.PREPARED;
			playingText.setText( playingFile.getName() );
			playPause.setImageDrawable(playIcon);
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			
			Log.e( e, "Cannot make the file to be played: " + filePath );
			Toast.makeText( this, R.string.openFileFailed, Toast.LENGTH_LONG )
				 .show();
		}
	}

	private void scanFolder( File folder, List<File> fileList ) {
		if( !folder.exists() || !folder.isDirectory() ) {
			return;
		}
		
		File[] fs = folder.listFiles( fileFilter );
		Arrays.sort( fs, new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				return lhs.getName().compareToIgnoreCase( rhs.getName() );
			}
		});
		
		for( File f : fs ) {
			if( f.isDirectory() ) {
				scanFolder( f, fileList );
			} else {
				fileList.add( f );
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if( mediaPlayer != null ) {
			mediaPlayer.release();
		}
	}
}
