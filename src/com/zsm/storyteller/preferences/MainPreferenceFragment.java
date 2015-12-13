package com.zsm.storyteller.preferences;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController;

public class MainPreferenceFragment extends PreferenceFragment {

	public static final int[] PLAY_ORDER_ICONS
				= { R.drawable.play_inrorder, R.drawable.play_random };
	private static final int[] PLAY_ORDER_SUMMARY
				= { R.string.playOrderSumByName, R.string.playOrderSumRandom };
	
	private ListPreference prefPlayOrder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceChangeListener onChangeListener = new PreferenceChangeListener();
		initPlayOrder(onChangeListener);
	}

	private void initPlayOrder(PreferenceChangeListener onChangeListener) {
		prefPlayOrder = (ListPreference) findPreference( Preferences.KEY_PLAY_ORDER );
		prefPlayOrder.setOnPreferenceChangeListener(onChangeListener);
		PlayController.PLAY_ORDER enums[] = PlayController.PLAY_ORDER.values();
		String values[] = new String[enums.length];
		for( PlayController.PLAY_ORDER order : enums ) {
			values[order.ordinal()] = order.name();
		}
		prefPlayOrder.setEntryValues( values );
		changePlayOrder(prefPlayOrder.getValue());
	}

	private void changePlayOrder( String newOrder ) {
		PlayController.PLAY_ORDER order
			= PlayController.PLAY_ORDER.valueOf( newOrder );
		order = order == null ? PlayController.PLAY_ORDER.BY_NAME : order;
		prefPlayOrder.setIcon( PLAY_ORDER_ICONS[order.ordinal()] );
		prefPlayOrder.setSummary( PLAY_ORDER_SUMMARY[order.ordinal()] );
	}

	private final class PreferenceChangeListener
							implements OnPreferenceChangeListener {
		
		@Override
		public boolean onPreferenceChange(Preference preference,
										  Object newValue) {
			
			String key = preference.getKey();
			if( key.equals( prefPlayOrder.getKey() )) {
				changePlayOrder( (String)newValue );
			} else {
				return false;
			}
			
			return true;
		}
	}

}
