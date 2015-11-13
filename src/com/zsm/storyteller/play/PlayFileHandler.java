package com.zsm.storyteller.play;

import java.io.File;

import android.app.Activity;
import android.net.Uri;
import android.view.View;

import com.zsm.android.ui.fileselector.FileOperation;
import com.zsm.android.ui.fileselector.FileSelector;
import com.zsm.android.ui.fileselector.OnHandleFileListener;
import com.zsm.storyteller.PlayInfo;
import com.zsm.storyteller.app.StoryTellerApp;


public class PlayFileHandler implements OnHandleFileListener {

	private PlayController player;
	private FileSelector fileSelector;

	public PlayFileHandler( PlayController pc ) {
		player = pc;
	}
	
	@Override
	public void handleFile(FileOperation operation, String filePath) {
		File f = new File( filePath );
		PlayInfo.LIST_TYPE lt = PlayInfo.LIST_TYPE.SINGLE;
		
		if( operation == FileOperation.FOLDER ) {
			lt = PlayInfo.LIST_TYPE.FOLDER;
		}
		player.updatePlayInfo( new PlayInfo( lt, Uri.fromFile(f), null, 0 ) );
	}

	public void openOne( Activity activity ) {
		fileSelector
			= new FileSelector( activity, FileOperation.LOAD, this,
								StoryTellerApp.EXTENSION );
		fileSelector.show();
	}
	
	public void openFolder( Activity activity ) {
		fileSelector = new FileSelector(activity, FileOperation.FOLDER, this, null );
		fileSelector.show();
	}
}
