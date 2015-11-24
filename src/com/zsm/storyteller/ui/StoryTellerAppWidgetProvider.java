package com.zsm.storyteller.ui;

import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayerView;
import com.zsm.storyteller.play.StoryPlayer.PLAYER_STATE;

public class StoryTellerAppWidgetProvider extends AppWidgetProvider
					implements PlayerView {

	private PlayerViewReceiver playerViewReceiver;
	private RemoteViews remoteViews;

	public StoryTellerAppWidgetProvider() {
		playerViewReceiver = new PlayerViewReceiver( this );
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
						 int[] appWidgetIds) {
		
        addClickEvent(context, appWidgetManager, appWidgetIds,
        			  PlayController.ACTION_PLAYER_PLAY,
        			  R.id.imageViewWidgetPlay);
        addClickEvent(context, appWidgetManager, appWidgetIds,
        			  PlayController.ACTION_PLAYER_PLAY_NEXT,
        			  R.id.imageViewWidgetNext);
        addClickEvent(context, appWidgetManager, appWidgetIds,
  			  		  PlayController.ACTION_PLAYER_MAIN_ACTIVITY,
  			  		  R.id.layoutInfo);
        
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void addClickEvent(Context context, AppWidgetManager appWidgetManager,
							   int[] appWidgetIds, String action,
							   int viewResId) {
		
		// Perform this loop procedure for each App Widget that belongs to this provider
        Intent intent = new Intent(action);
        PendingIntent pi
        	= PendingIntent.getBroadcast(context, 0, intent,
        								 PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views
        	= new RemoteViews(context.getPackageName(), R.layout.main_widget);
        views.setOnClickPendingIntent(viewResId, pi);

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetIds, views);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_widget);
		AppWidgetManager appWidgetManager
			= AppWidgetManager.getInstance(context.getApplicationContext());
		ComponentName widgets = new ComponentName(context, StoryTellerAppWidgetProvider.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widgets);

		
		if( playerViewReceiver.onReceive(context, intent) ) {
	        appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
			remoteViews = null;
			return;
		}
		
		super.onReceive(context, intent);
	}

	@Override
	public void setDuration(int duration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTime(long curretPosition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePlayerState(PLAYER_STATE state) {
    	switch( state ) {
			case STARTED:
				remoteViews.setImageViewResource( R.id.imageViewWidgetPlay, R.drawable.widget_pause);
				break;
			case PREPARED:
			case STOPPED:
			case PAUSED:
				remoteViews.setImageViewResource( R.id.imageViewWidgetPlay, R.drawable.widget_play);
				break;
			default:
				break;
    	}
	}

	@Override
	public void setDataSource(Context context, Uri uri) {
		MediaInfo mi = new MediaInfo( context, uri );
		remoteViews.setTextViewText( R.id.textViewTitle, mi.getTitle() );
	}

	@Override
	public void updatePlayList(List<Uri> playList) {
		// TODO Auto-generated method stub
		
	}

}
