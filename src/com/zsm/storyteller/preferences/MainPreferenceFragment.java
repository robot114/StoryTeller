package com.zsm.storyteller.preferences;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.zsm.log.Log;
import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.play.PlayController.PLAY_ORDER;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;
import com.zsm.storyteller.preferences.PreferenceUtil.ExtrasActionAfterChange;
import com.zsm.storyteller.ui.PlayerView;

public class MainPreferenceFragment extends PreferenceFragment {

	public static final int[] PLAY_PAUSE_TYPE_ICONS
		= { R.drawable.play_small, R.drawable.play_to_small };
	public static final int[] PLAY_ORDER_ICONS
		= { R.drawable.play_inrorder, R.drawable.play_random };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceUtil.extractEnumPreference( 
				this, Preferences.KEY_PLAY_ORDER, PLAY_ORDER.class,
				PLAY_ORDER.values(), PLAY_ORDER.BY_NAME, PLAY_ORDER_ICONS,
				new int[] {R.string.playOrderSumByName, R.string.playOrderSumRandom } );

		PreferenceUtil.extractEnumPreference( 
				this, Preferences.KEY_PLAY_TYPE_TO_PAUSE, PLAY_PAUSE_TYPE.class,
				PLAY_PAUSE_TYPE.values(), PLAY_PAUSE_TYPE.CONTINUOUS,
				PLAY_PAUSE_TYPE_ICONS,
				new int[]{ R.string.prefPlayTypeContinuously,
						   R.string.prefPlayTypeToPause },
						   
				new ExtrasActionAfterChange<PLAY_PAUSE_TYPE>() {
					@Override
					public void action(PLAY_PAUSE_TYPE type) {
						sendPlayTypeChangeMessage( type );
					}
				});
	}

	private void sendPlayTypeChangeMessage( PLAY_PAUSE_TYPE type ) {
		Intent intent = new Intent( PlayController.ACTION_UPDATE_PLAY_PAUSE_TYPE );
		intent.putExtra( PlayController.KEY_PLAY_PAUSE_TYPE, type.name() );
		PendingIntent pi
			= PendingIntent.getBroadcast( 
					getActivity(), PlayerView.PLAYER_VIEW_REQUEST_ID, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		
		try {
			pi.send();
		} catch (CanceledException ex) {
			Log.e( ex );
		}
	}
}
