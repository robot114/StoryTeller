package com.zsm.storyteller.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.zsm.storyteller.R;

public class MainPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_preference);
	}

}
