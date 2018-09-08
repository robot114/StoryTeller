package com.zsm.storyteller.preferences;

import android.content.Context;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController.PLAY_PAUSE_TYPE;

public class PlayPausePreference extends Preference {

	private RadioGroup mPlayPauseTypeGroup;
	private EditText mTimeEdit;
	private RadioButton mPlaySleepRadio;

	public PlayPausePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setWidgetLayoutResource( R.layout.play_pause_type_setting );
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		initButtons( view );
		initValues();
	}

	private void initButtons(View view) {
		mPlayPauseTypeGroup
			= (RadioGroup)view.findViewById( R.id.radioGroupPlayPauseType );
		mPlayPauseTypeGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				persistType(checkedId);
			}
			
		} );
		
		mPlaySleepRadio
			= (RadioButton)view.findViewById( R.id.radioButtonPlaySleep );
		mPlaySleepRadio.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton view, boolean isChecked) {
				if( isChecked ) {
					mTimeEdit.requestFocus();
				}
			}
		} );
		
		mTimeEdit = (EditText)view.findViewById( R.id.editTimeToSleep );
		mTimeEdit.setSelectAllOnFocus( true );
		mTimeEdit.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mPlaySleepRadio.setChecked( true );
				persistType( mPlaySleepRadio.getId() );
				persistTime( s.toString() );
			}
			
		} );
	}
	
	private void initValues() {
		PLAY_PAUSE_TYPE type = Preferences.getInstance().getPlayPauseType();
		
		int time = Preferences.getInstance().getPlaySleepTime();
		if( time > 0 && time < 12*60 ) {
			mTimeEdit.setText( String.valueOf( time ) );
		} else {
			mTimeEdit.setText( "" );
		}
		
		int id;
		switch( type ) {
			case CONTINUOUS:
				id = R.id.radioButtonPlayContinously;
				break;
			case TO_PAUSE:
				id = R.id.radioButtonPlayToPause;
				break;
			case TO_SLEEP:
				id = R.id.radioButtonPlaySleep;
				break;
			default:
				id = R.id.radioButtonPlayContinously;
				break;
		}
		mPlayPauseTypeGroup.check(id);
	}
	
	private void persistType(int id) {
		PLAY_PAUSE_TYPE type;
		switch( id ) {
			case R.id.radioButtonPlayContinously:
			default:
				type = PLAY_PAUSE_TYPE.CONTINUOUS;
				break;
			case R.id.radioButtonPlayToPause:
				type = PLAY_PAUSE_TYPE.TO_PAUSE;
				break;
			case R.id.radioButtonPlaySleep:
				type = PLAY_PAUSE_TYPE.TO_SLEEP;
				break;
		}
		
		Preferences.getInstance().setPlayTypeToPause(type);
		Preferences.sendPlayTypeChangeMessage( getContext(), type );
	}

	private void persistTime( String timeStr ) {

		int time = 0;
		try {
			time = Integer.valueOf( timeStr.trim() );
		} catch ( Exception e ) {
		}
		
		Preferences.getInstance().setPlaySleepTime( time );
	}
}
