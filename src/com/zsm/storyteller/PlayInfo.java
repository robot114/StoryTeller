package com.zsm.storyteller;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.content.SharedPreferences;
import android.net.Uri;

import com.zsm.storyteller.preferences.Preferences;

public class PlayInfo {

	private final class FolderOrExtFilter implements FileFilter {
		private String[] extension;

		private FolderOrExtFilter( String[] ext ) {
			this.extension = ext;
		}
		
		@Override
		public boolean accept(File pathname) {
			int extIndex = pathname.getName().lastIndexOf( '.' );
			boolean inc = false;
			if( extIndex >= 0 ) {
				String ext = pathname.getName().substring( extIndex );
				for( String e : extension ) {
					if( e.equalsIgnoreCase( ext ) ) {
						inc = true;
						break;
					}
				}
			}
			return pathname.isDirectory() || inc;
		}
	}

	private LIST_TYPE listType;
	// When the type is SINGLE, listInfo should be the uri of the file. 
	// When the type is FOLDER, it should be the uri of the folder.
	// When the type is LSIT, it should be the uri of the file containing the list.
	private Uri listInfo;
	private Uri currentPlaying;
	private List<Uri> playList;
	private int currentPlayingIndex;
	private long currentPlayingPosition;

	public enum LIST_TYPE {
		SINGLE, FOLDER, LIST
	}

	public PlayInfo( LIST_TYPE type, Uri listInfo, Uri currentPlaying,
					 long currentPosition ) {
		
		this.listType = type;
		this.listInfo = listInfo;
		this.currentPlaying = currentPlaying;
		this.currentPlayingPosition = currentPosition;
		playList = null;
	}
	
	public void toPreferences( SharedPreferences p ) {
		p.edit().putString( Preferences.KEY_LIST_TYPE, listType.name() )
				.putString( Preferences.KEY_LIST_INFO, listInfo.toString() )
				.putString( Preferences.KEY_CURRENT_PLAYING, currentPlaying.toString() )
				.putLong( Preferences.KEY_CURRENT_POSITION, currentPlayingPosition )
				.apply();
	}
	
	public static PlayInfo fromPreferences( SharedPreferences p ) {
		LIST_TYPE lt 
			= LIST_TYPE.valueOf( p.getString(Preferences.KEY_LIST_TYPE,
											 LIST_TYPE.SINGLE.name() ) );
		
		String lis = p.getString(Preferences.KEY_LIST_INFO, null );
		Uri li = null;
		if( lis != null ) {
			li = Uri.parse(lis);
		}
		
		String cps = p.getString(Preferences.KEY_CURRENT_PLAYING, null );
		long cpos = 0;
		Uri cp = null;
		if( cps != null ) {
			cp = Uri.parse( cps );
			cpos = p.getLong( Preferences.KEY_CURRENT_POSITION, 0 );
		}
		
		return new PlayInfo( lt, li, cp, cpos );
	}
	
	public void setCurrentPlaying(Uri uri) {
		int index = playList.indexOf( uri );
		if( index < 0 ) {
			throw new IllegalArgumentException( "The uri to be played is not in "
												+ "the list! uri: " + uri );
		}
		currentPlaying = uri;
		currentPlayingIndex = index;
		Preferences.getInstance().setCurrentPlaying( currentPlaying );
	}
	
	public long getCurrentPlayingPosition() {
		return currentPlayingPosition;
	}
	
	public void setCurrentPlayingPosition( long position ) {
		currentPlayingPosition = position;
		Preferences.getInstance().setCurrentPlayingPosition( position );
	}
	public List<Uri> getPlayList( String[] ext, boolean forceRefresh ) {
		
		if( listInfo == null ) {
			return null;
		}
		
		if( playList != null && !forceRefresh ) {
			return playList;
		}
		playList = new ArrayList<Uri>();
		File f = new File( listInfo.getPath() );
		if( listType == LIST_TYPE.FOLDER ) {
			scanFolder( f, playList, new FolderOrExtFilter( ext ) );
		} else {
			if( !f.exists() || !f.isFile() ) {
				return null;
			}
			playList.add( listInfo );
		}

		return playList;
	}

	private void scanFolder( File folder, List<Uri> list,
							 FileFilter fileFilter ) {
		
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
				scanFolder( f, list, fileFilter );
			} else {
				list.add( Uri.fromFile( f ) );
			}
		}
	}
	
	public Uri refreshCurrentPlaying() {
		if( isListValid() ) {
			return null;
		}
		
		if( currentPlaying == null || !playList.contains( currentPlaying ) ) {
			currentPlaying = playList.get( 0 );
			currentPlayingIndex = 0;
		} else {
			currentPlayingIndex = playList.indexOf( currentPlaying );
		}
		
		return currentPlaying;
	}

	public Uri nextOne() {
		if( isListValid() ) {
			return null;
		}
		
		if( currentPlayingIndex >= playList.size()-1 ) {
			return null;
		}
		
		currentPlayingIndex++;
		currentPlaying = playList.get( currentPlayingIndex );
		
		return currentPlaying;
	}

	public Uri previousOne() {
		if( isListValid() ) {
			return null;
		}
		
		if( currentPlayingIndex <= 0 ) {
			return null;
		}
		
		currentPlayingIndex--;
		currentPlaying = playList.get( currentPlayingIndex );
		
		return currentPlaying;
	}

	private boolean isListValid() {
		return playList == null || playList.size() == 0;
	}

}
