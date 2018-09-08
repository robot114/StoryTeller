package com.zsm.storyteller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.provider.DocumentFile;

import com.zsm.log.Log;
import com.zsm.storyteller.preferences.Preferences;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.android.DocumentFileUtilities;

public class PlayInfo implements Parcelable {

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
	
	private PlayInfo() {
		
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
		Editor editor
			= p.edit().putString( Preferences.KEY_LIST_TYPE, listType.name() );
		if( listInfo != null ) {
			editor.putString( Preferences.KEY_LIST_INFO, listInfo.toString() );
		} else {
			editor.remove( Preferences.KEY_LIST_INFO );
		}
		
		if( currentPlaying != null ) {
			editor.putString( Preferences.KEY_CURRENT_PLAYING,
							  currentPlaying.toString() )
				  .putLong( Preferences.KEY_CURRENT_POSITION,
						    currentPlayingPosition );
		} else {
			editor.remove( Preferences.KEY_CURRENT_PLAYING )
				  .remove( Preferences.KEY_CURRENT_POSITION );
		}
		
		editor.apply();
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
	
	public List<Uri> getCurrentPlayList() {
		if( listInfo != null && playList == null ) {
			throw new IllegalStateException( 
				"PlayList has not be refresed. Invoke getPlayList() first!" );
		}
		
		return playList;
	}
	
	public List<Uri> getPlayList( Context context, FileExtensionFilter filter,
								  boolean forceRefresh ) {
		
		if( listInfo == null ) {
			return null;
		}
		
		if( playList != null && !forceRefresh ) {
			return playList;
		}
		playList = new ArrayList<Uri>();
		DocumentFile document = DocumentFile.fromSingleUri(context, listInfo);
		if( listType == LIST_TYPE.FOLDER ) {
			scanDirectory( context, document, playList, filter );
		} else {
			if( !document.exists() || !document.isFile() ) {
				return null;
			}
			playList.add( listInfo );
		}

		return playList;
	}

	private void scanDirectory( Context context, DocumentFile folder,
								List<Uri> list, FileExtensionFilter fileFilter ) {
		
		if( !folder.exists() || !folder.isDirectory() ) {
			return;
		}
		
		DocumentFile[] docs
			= DocumentFileUtilities.listFiles( context, folder.getUri(),
											   fileFilter, true, null );
		if( docs == null ) {
			Log.w( "No file scaned!", folder );
			return;
		}
		Arrays.sort( docs, new Comparator<DocumentFile>() {
			@Override
			public int compare(DocumentFile lhs, DocumentFile rhs) {
				return lhs.getName().compareToIgnoreCase( rhs.getName() );
			}
		});
		
		for( DocumentFile doc : docs ) {
			if( doc.isDirectory() ) {
				scanDirectory( context, doc, list, fileFilter );
			} else {
				list.add( doc.getUri() );
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

	public Uri nextOne( boolean random ) {
		if( isListValid() ) {
			return null;
		}
		
		if( random ) {
			currentPlayingIndex = (int) (Math.random()*playList.size());
		} else {
			if( currentPlayingIndex >= playList.size()-1 ) {
				return null;
			}
			currentPlayingIndex++;
		}
		currentPlaying = playList.get( currentPlayingIndex );
		return currentPlaying;
	}

	public Uri previousOne( boolean random ) {
		if( isListValid() ) {
			return null;
		}

		if( random ) {
			currentPlayingIndex = (int) (Math.random()*playList.size());
		} else {
			if( currentPlayingIndex <= 0 ) {
				return null;
			}
			
			currentPlayingIndex--;
		}
		currentPlaying = playList.get( currentPlayingIndex );
		
		return currentPlaying;
	}

	private boolean isListValid() {
		return playList == null || playList.size() == 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString( listType.name() );
		dest.writeParcelable( listInfo, flags);
		dest.writeParcelable( currentPlaying, flags);
		dest.writeList(playList);
		dest.writeInt(currentPlayingIndex);
		dest.writeLong( currentPlayingPosition );
	}

    public static final Parcelable.Creator<PlayInfo> CREATOR
	    = new Parcelable.Creator<PlayInfo>() {
	
		@SuppressWarnings("unchecked")
		@Override
		public PlayInfo createFromParcel(Parcel in) {
			PlayInfo pi = new PlayInfo();
			
			pi.listType = LIST_TYPE.valueOf( in.readString() );
			pi.listInfo = (Uri)in.readParcelable(null);
			pi.currentPlaying = (Uri)in.readParcelable(null);
			pi.playList = (List<Uri>)in.readArrayList(null);
			pi.currentPlayingIndex = in.readInt();
			pi.currentPlayingPosition = in.readLong();
			
		    return pi;
		}
		
		// We just need to copy this and change the type to match our class.
		@Override
		public PlayInfo[] newArray(int size) {
		    return new PlayInfo[size];
		}
	};
}
