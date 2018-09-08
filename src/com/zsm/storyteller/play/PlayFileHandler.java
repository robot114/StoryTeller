package com.zsm.storyteller.play;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.zsm.android.ui.documentSelector.DocumentHandler;
import com.zsm.android.ui.documentSelector.DocumentOperation;
import com.zsm.android.ui.documentSelector.DocumentSelector;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.preferences.Preferences;


public class PlayFileHandler implements DocumentHandler {

	private PlayController player;

	public PlayFileHandler( Context context ) {
		player = new RemotePlayer( context );
	}

	@Override
	public void handleDocument(DocumentOperation operation,
							   DocumentFile document, String name) {
		
		PlayInfo.LIST_TYPE lt = PlayInfo.LIST_TYPE.SINGLE;
		
		if( operation == DocumentOperation.FOLDER ) {
			lt = PlayInfo.LIST_TYPE.FOLDER;
		}
		PlayInfo pi = new PlayInfo( lt, document.getUri(), null, 0 );
		Preferences.getInstance().savePlayListInfo(pi);
		player.setPlayInfo( pi );
		if( Preferences.getInstance().autoStartPlaying() ) {
			player.playPause();
		}
	}

//	public void openFolder( Activity activity, String currentPath, PlayController player ) {
//		this.player = player;
//		FileSelector fileSelector
//			= new FileSelector( activity, FileOperation.FOLDER, currentPath, this,
//						    StoryTellerApp.getAudioFileFilterArray( activity ) );
//		fileSelector.show();
//	}
//
//	public void openOne( Activity activity, String currentPath, PlayController player ) {
//		this.player = player;
//		FileSelector fileSelector
//			= new FileSelector( activity, FileOperation.LOAD, currentPath, this,
//							StoryTellerApp.getAudioFileFilterArray( activity ) );
//		fileSelector.show();
//	}
//	
	public void openOne( Activity activity, Uri currentPath, PlayController player ) {
		this.player = player;
		DocumentSelector documentSelector
			= new DocumentSelector( activity, DocumentOperation.LOAD, currentPath, this,
					    StoryTellerApp.getAudioFileFilterArray( activity ) );
		documentSelector.show();
	}
	
	public void openFolder( Activity activity, Uri currentPath, PlayController player ) {
		this.player = player;
		DocumentSelector documentSelector
			= new DocumentSelector( activity, DocumentOperation.FOLDER, currentPath, this,
						    StoryTellerApp.getAudioFileFilterArray( activity ) );
		documentSelector.show();
	}
}
