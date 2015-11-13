package com.zsm.storyteller.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.zsm.storyteller.R;

public class MainPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
