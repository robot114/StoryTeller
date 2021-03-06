package com.zsm.storyteller.ui;

import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.zsm.log.Log;
import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.play.AbstractPlayer;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;
import com.zsm.storyteller.play.PlayService;
import com.zsm.storyteller.preferences.Preferences;
import com.zsm.util.TextUtil;

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
		
		Log.d( "update widget" );
		startService( context );
		addAllClickEvent(context, appWidgetManager);
        
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void addAllClickEvent(Context context, AppWidgetManager appWidgetManager) {
		addClickEvent(context, appWidgetManager,
        			  PlayController.ACTION_PLAYER_PLAY_PAUSE,
        			  R.id.imageViewWidgetPlay);
        addClickEvent(context, appWidgetManager, 
        			  PlayController.ACTION_PLAYER_PLAY_NEXT,
        			  R.id.imageViewWidgetNext);
        addClickEvent(context, appWidgetManager,
		  			  PlayController.ACTION_PLAYER_PLAY_FAST_FORWARD,
		  			  R.id.imageViewWidgetForward);
        addClickEvent(context, appWidgetManager, 
  			  		  PlayController.ACTION_PLAYER_MAIN_ACTIVITY,
  			  		  R.id.layoutMainWidget);
	}
	
	private void startService( Context context ) {
		ServiceConnection serviceConnection = new ServiceConnection() {
		    @Override
		    public void onServiceConnected(ComponentName name, IBinder service) {
		    	Log.d("Service connected", "ComponentName", name, service);
		        PlayService.ServiceBinder binder = (PlayService.ServiceBinder) service;
		        binder.getService();
		    }
		 
		    @Override
		    public void onServiceDisconnected(ComponentName name) {
		    	Log.d( "Service disconnected", "ComponentName", name );
		    }

		};
		
		Intent intent = new Intent(context, PlayService.class);
		context.getApplicationContext()
					.bindService(intent, serviceConnection,
							Context.BIND_AUTO_CREATE|Context.BIND_ABOVE_CLIENT);
	}
	
	private void addClickEvent(Context context, AppWidgetManager appWidgetManager,
							   String action, int viewResId) {
		
		// Perform this loop procedure for each App Widget that belongs to this provider
        Intent intent = new Intent(action);
        PendingIntent pi
			= PendingIntent.getService(context, PlayController.REQUEST_PLAY_CODE,
									   intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views
        	= new RemoteViews(context.getPackageName(), R.layout.main_widget);
        views.setOnClickPendingIntent(viewResId, pi);

        // Tell the AppWidgetManager to perform an update on the current app widget
        ComponentName compName = new ComponentName(context, getClass().getName());
		appWidgetManager.updateAppWidget(compName, views);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if( PlayController.ACTION_UPDATE_PLAY_PAUSE_TYPE
					.equals( intent.getAction() ) ) {
			
			// updatePlayPauseType is an asychronize method
			playerViewReceiver.onReceive(context, intent);
			return;
		}
		
		AppWidgetManager appWidgetManager
			= AppWidgetManager.getInstance(context.getApplicationContext());
		if( PlayerView.ACTION_UPDATE_PLAYER_STATE
								.equals( intent.getAction() ) ) {
			
			// Sometimes the widget does not response the clicking. 
			// No idea about the reason
			addAllClickEvent(context, appWidgetManager);
		}
		
		Log.d( intent );
		remoteViews
			= new RemoteViews(context.getPackageName(), R.layout.main_widget);
		if( playerViewReceiver.onReceive(context, intent) ) {
			ComponentName widgets
				= new ComponentName(context, StoryTellerAppWidgetProvider.class);
		
	        appWidgetManager.updateAppWidget(widgets, remoteViews);
			remoteViews = null;
			
			return;
		}
		
		super.onReceive(context, intent);
	}

	@Override
	public void updateTime(int curretPosition, int duration) {
		remoteViews
			.setTextViewText( 
				R.id.textViewTimeRemain,
				TextUtil.durationToText(duration - curretPosition) );
	}

	@Override
	public void updatePlayerState(AbstractPlayer.PLAYER_STATE state) {
		setPlayPauseIcon(remoteViews, state,
						 Preferences.getInstance().getPlayPauseType());
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

	@Override
	public void updatePlayPauseType(final Context context,
									final PLAY_PAUSE_TYPE type) {
		new Handler().post( new Runnable() {
			@Override
			public void run() {
				RemoteViews rvs
					= new RemoteViews(context.getPackageName(),
									  R.layout.main_widget);
				AppWidgetManager appWidgetManager
					= AppWidgetManager.getInstance(context.getApplicationContext());
				ComponentName widgets
					= new ComponentName(context, StoryTellerAppWidgetProvider.class);
				StoryTellerApp app
					= (StoryTellerApp) context.getApplicationContext();
				setPlayPauseIcon(rvs, app.getPlayer().getState(), type );
		        appWidgetManager.updateAppWidget(widgets, rvs);
				rvs = null;
			}
		} );
	}
	
	private void setPlayPauseIcon(RemoteViews rvs, AbstractPlayer.PLAYER_STATE state,
								  PLAY_PAUSE_TYPE pauseType) {
		
		int playIconId;
		switch( pauseType ) {
			case CONTINUOUS:
				playIconId = R.drawable.widget_play;
				break;
			case TO_PAUSE:
				playIconId = R.drawable.widget_play_to;
				break;
			case TO_SLEEP:
				playIconId = R.drawable.widget_timer_play;
				break;
			default:
				playIconId = R.drawable.widget_play;
				Log.w( "Invalid play pause type", pauseType );
				break;
		}
	
		playIconId
			= (state == AbstractPlayer.PLAYER_STATE.STARTED )
				? R.drawable.widget_pause : playIconId;
		
		rvs.setImageViewResource( R.id.imageViewWidgetPlay, playIconId );
	}

}
