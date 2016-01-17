package com.zsm.storyteller.play;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.zsm.android.ui.fileselector.FileOperation;
import com.zsm.android.ui.fileselector.FileSelector;
import com.zsm.android.ui.fileselector.OnHandleFileListener;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.preferences.Preferences;


public class PlayFileHandler implements OnHandleFileListener {

	private PlayController player;
	private FileSelector fileSelector;

	public PlayFileHandler( Context context ) {
		player = new RemotePlayer( context );
	}
	
	@Override
	public void handleFile(FileOperation operation, String filePath) {
		File f = new File( filePath );
		PlayInfo.LIST_TYPE lt = PlayInfo.LIST_TYPE.SINGLE;
		
		if( operation == FileOperation.FOLDER ) {
			lt = PlayInfo.LIST_TYPE.FOLDER;
		}
		PlayInfo pi = new PlayInfo( lt, Uri.fromFile(f), null, 0 );
		Preferences.getInstance().savePlayListInfo(pi);
		player.setPlayInfo( pi );
		if( Preferences.getInstance().autoStartPlaying() ) {
			player.playPause();
		}
	}

	public void openOne( Activity activity, String currentPath, PlayController player ) {
		this.player = player;
		fileSelector
			= new FileSelector( activity, FileOperation.LOAD, currentPath, this,
								StoryTellerApp.getAudioFileFilterArray( activity ) );
		fileSelector.show();
	}
	
	public void openFolder( Activity activity, String currentPath, PlayController player ) {
		this.player = player;
		fileSelector
			= new FileSelector( activity, FileOperation.FOLDER, currentPath, this,
							    StoryTellerApp.getAudioFileFilterArray( activity ) );
		fileSelector.show();
	}
}
