package com.zsm.storyteller.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.zsm.driver.android.preference.PreferenceUtil;
import com.zsm.driver.android.preference.PreferenceUtil.ExtrasActionAfterChange;
import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController.PLAY_ORDER;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;

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
						Preferences.sendPlayTypeChangeMessage( getActivity(), type );
					}
				});
		
		Preference decoder
			= findPreference( Preferences.KEY_SYSTEM_DEFAULT_DECODER );
		decoder.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
											  Object newValue) {
				
				Toast
					.makeText(getActivity(),
							  R.string.prefPromptSystemDefaultDecoderChanger,
							  Toast.LENGTH_LONG)
					.show();
				
				return true;
			}
		} );
	}
}
