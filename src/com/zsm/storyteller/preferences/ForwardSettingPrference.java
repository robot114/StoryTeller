package com.zsm.storyteller.preferences;

import java.util.Hashtable;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.zsm.storyteller.R;
import com.zsm.storyteller.preferences.Preferences.FORWARD_SKIP_TYPE;

public class ForwardSettingPrference extends Preference {

	private enum COMPONENT_KEY {
		RADIO_PERCENT, RADIO_SECOND, SEEKBAR_PERCENT, SEEKBAR_SECOND,
		TEXTVIEW_SKIPHEADER, CHECKBOX_SKIPHEADER, SEEKBAR_SKIPHEADER
	}

	private RadioButton radioSkipTypePercent;
	private RadioButton radioSkipTypeSecond;
	private SeekBar seekBarPercent;
	private SeekBar seekBarSecond;
	private String percentFormatter;
	private String secondFormatter;
	private String skipFormatter;

	private Hashtable<View, Hashtable<COMPONENT_KEY, View>> views;
	private CheckBox checkBoxSkipHeader;
	private TextView textViewSkipHeader;
	private SeekBar seekBarSkipHeader;
	
	private ForwardRewindListener forwardRewindListener;
	private SkipHeaderListener skipHeaderListener;
	
	public ForwardSettingPrference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWidgetLayoutResource( R.layout.forward_setting );
		
		Resources resources = context.getResources();
		percentFormatter
			= resources.getString( R.string.prefForwardRewindByPercent );
		secondFormatter
			= resources.getString(R.string.prefForwardRewindBySecond);
		skipFormatter
			= resources.getString( R.string.prefSkipHeader );
		
		views = new Hashtable<View, Hashtable<COMPONENT_KEY, View>>();
		
		skipHeaderListener = new SkipHeaderListener();
		forwardRewindListener = new ForwardRewindListener();
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		LayoutInflater li
		  	= (LayoutInflater)getContext()
		  		.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View view = li.inflate( R.layout.forward_setting, parent, false);
		return view;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		initButtons( view );
	}

	private Hashtable<COMPONENT_KEY, View> findButtons(View view) {
		Hashtable<COMPONENT_KEY, View> vs = new Hashtable<COMPONENT_KEY, View>();
		RadioButton rsp
			= (RadioButton) view.findViewById( R.id.radioButtonSkipPercent );
		RadioButton rss
			= (RadioButton) view.findViewById( R.id.radioButtonSkipSecond );
		SeekBar sbp = (SeekBar) view.findViewById( R.id.seekBarSkipPercent );
		SeekBar sbs = (SeekBar) view.findViewById( R.id.seekBarSkipSecond );
		
		TextView tvsh = (TextView)view.findViewById( R.id.textViewSkipHeader );
		CheckBox cbsh = (CheckBox)view.findViewById( R.id.checkBoxSkipHeader );
		SeekBar sbsh = (SeekBar)view.findViewById( R.id.seekBarSkipHeader );
		
		vs.put( COMPONENT_KEY.RADIO_PERCENT, rsp );
		vs.put( COMPONENT_KEY.RADIO_SECOND, rss );
		vs.put( COMPONENT_KEY.SEEKBAR_PERCENT, sbp );
		vs.put( COMPONENT_KEY.SEEKBAR_SECOND, sbs );
		vs.put( COMPONENT_KEY.TEXTVIEW_SKIPHEADER, tvsh );
		vs.put( COMPONENT_KEY.CHECKBOX_SKIPHEADER, cbsh );
		vs.put( COMPONENT_KEY.SEEKBAR_SKIPHEADER, sbsh );
		
		initButtonsValue(vs);
		return vs;
	}

	private void initButtonsValue(Hashtable<COMPONENT_KEY, View> vs) {
		Preferences pref = Preferences.getInstance();
		FORWARD_SKIP_TYPE type = pref.getForwardSkipType();
		int progressValueOfPercent = pref.getForwardSkipPercentProgressValue();
		int progressValueOfSecond = pref.getForwardSkipSecondProgressValue();
		
		RadioButton rsp = (RadioButton) vs.get( COMPONENT_KEY.RADIO_PERCENT );
		RadioButton rss = (RadioButton) vs.get( COMPONENT_KEY.RADIO_SECOND );
		rsp.setChecked( type == FORWARD_SKIP_TYPE.BY_PERCENT );
		rss.setChecked( type == FORWARD_SKIP_TYPE.BY_SECOND );
		
		((ProgressBar)vs.get(COMPONENT_KEY.SEEKBAR_PERCENT))
			.setProgress(progressValueOfPercent);
		((ProgressBar)vs.get(COMPONENT_KEY.SEEKBAR_SECOND))
			.setProgress(progressValueOfSecond);
		
		setPercentText( rsp, progressValueOfPercent );
		setSecondText( rss, progressValueOfSecond );
		
		boolean sha = pref.getSkipHeaderAuto();
		((CheckBox)vs.get( COMPONENT_KEY.CHECKBOX_SKIPHEADER )).setChecked( sha );
		int shs = pref.getSkipHeaderValue();
		TextView tvsh = (TextView)vs.get(COMPONENT_KEY.TEXTVIEW_SKIPHEADER);
		setSkipHeaderText( tvsh, shs );
		((SeekBar)vs.get( COMPONENT_KEY.SEEKBAR_SKIPHEADER ) ).setProgress( shs );
	}

	private void initButtons(View view) {
		Hashtable<COMPONENT_KEY, View> vs = findButtons(view);
		
		vs.get( COMPONENT_KEY.RADIO_PERCENT)
				.setOnClickListener(forwardRewindListener);
		vs.get( COMPONENT_KEY.RADIO_SECOND )
				.setOnClickListener(forwardRewindListener);
		
		((SeekBar)(vs.get( COMPONENT_KEY.SEEKBAR_PERCENT )))
				.setOnSeekBarChangeListener(forwardRewindListener);
		((SeekBar)(vs.get( COMPONENT_KEY.SEEKBAR_SECOND )))
				.setOnSeekBarChangeListener(forwardRewindListener);
		
		vs.get( COMPONENT_KEY.TEXTVIEW_SKIPHEADER )
				.setOnClickListener( skipHeaderListener );
		((SeekBar)vs.get( COMPONENT_KEY.SEEKBAR_SKIPHEADER ))
				.setOnSeekBarChangeListener( skipHeaderListener );
		CheckBox cbsh = (CheckBox) vs.get( COMPONENT_KEY.CHECKBOX_SKIPHEADER );
		cbsh.setOnCheckedChangeListener( skipHeaderListener );
		
		storeViews( vs );
	}

	private void storeViews( Hashtable<COMPONENT_KEY, View> vs ) {
		Set<COMPONENT_KEY> ks = vs.keySet();
		for( COMPONENT_KEY key : ks ) {
			views.put( vs.get(key), vs );
		}
	}

	private void restoreViews(View view) {
		Hashtable<COMPONENT_KEY, View> vs = views.get(view);
		if( vs == null ) {
			vs = findButtons(view.getRootView() );
		} else {
			radioSkipTypePercent
				= (RadioButton) vs.get( COMPONENT_KEY.RADIO_PERCENT );
			radioSkipTypeSecond
				= (RadioButton) vs.get( COMPONENT_KEY.RADIO_SECOND );
			seekBarPercent = (SeekBar) vs.get( COMPONENT_KEY.SEEKBAR_PERCENT );
			seekBarSecond = (SeekBar) vs.get( COMPONENT_KEY.SEEKBAR_SECOND );
			checkBoxSkipHeader
				= (CheckBox)vs.get( COMPONENT_KEY.CHECKBOX_SKIPHEADER );
			textViewSkipHeader
				= (TextView)vs.get( COMPONENT_KEY.TEXTVIEW_SKIPHEADER );
			seekBarSkipHeader
				= (SeekBar)vs.get( COMPONENT_KEY.SEEKBAR_SKIPHEADER );
		}
		views.clear();
		storeViews( vs );
	}

	private void setSecondText(RadioButton secondButton, int factoredValue) {
		secondButton
			.setText( String.format(secondFormatter, 
					  Preferences.forwardSkipSecondProgressToRealValue(
							  		factoredValue) ) );
	}

	private void setPercentText(RadioButton percentButton, int factoredValue) {
		percentButton
			.setText( String.format(percentFormatter,
					  Preferences.forwardSkipPercentProgressToRealValue(
							  		factoredValue) ) );
	}

	private void setSkipHeaderText(TextView tv, int value) {
		tv.setText( String.format(skipFormatter, value ) );
	}
	
	private void storeForwardRewind() {
		FORWARD_SKIP_TYPE type
			= radioSkipTypePercent.isChecked()
			  ? FORWARD_SKIP_TYPE.BY_PERCENT
			  : FORWARD_SKIP_TYPE.BY_SECOND;
		
		Preferences.getInstance()
			.setForwardSkipValue( type,
								  seekBarPercent.getProgress(),
								  seekBarSecond.getProgress() );
	}
	
	private void storeSkipHeader() {
		Preferences.getInstance()
			.setSkipHeader( checkBoxSkipHeader.isChecked(),
						    seekBarSkipHeader.getProgress() );
	}

	private class SkipHeaderListener 
					implements OnSeekBarChangeListener, OnClickListener,
								OnCheckedChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			
			setSkipHeaderText(textViewSkipHeader, progress );
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if( seekBar != seekBarSkipHeader ) {
				restoreViews(seekBar);
			}
			
			checkBoxSkipHeader.setChecked( true );
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			storeSkipHeader();
		}

		@Override
		public void onClick(View v) {
			if( v != textViewSkipHeader ) {
				restoreViews( v );
			}
			
			checkBoxSkipHeader.setChecked(!checkBoxSkipHeader.isChecked());
			storeSkipHeader();
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
									 boolean isChecked) {
			
			if( buttonView != checkBoxSkipHeader ) {
				restoreViews( buttonView );
			}
			storeSkipHeader();
		}
		
	}
	
	private class ForwardRewindListener 
		implements OnClickListener, OnSeekBarChangeListener {

		@Override
		public void onClick(View v) {
			if( v != radioSkipTypePercent && v != radioSkipTypeSecond ) {
				restoreViews(v);
			}
			radioSkipTypePercent.setChecked( v == radioSkipTypePercent );
			radioSkipTypeSecond.setChecked( v == radioSkipTypeSecond );
			storeForwardRewind();
		}

		@Override
		public void onProgressChanged(final SeekBar seekBar, final int progress,
									  boolean fromUser) {
			
			switch( seekBar.getId() ) {
				case R.id.seekBarSkipPercent:
					setPercentText(radioSkipTypePercent, progress);
					break;
				case R.id.seekBarSkipSecond:
					setSecondText(radioSkipTypeSecond, progress);
					break;
				default:
					break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if( seekBar != seekBarPercent && seekBar != seekBarSecond ) {
				restoreViews(seekBar);
			}
			radioSkipTypePercent
				.setChecked( seekBar.getId() == seekBarPercent.getId() );
			radioSkipTypeSecond
				.setChecked( seekBar.getId() == seekBarSecond.getId() );
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			storeForwardRewind();
		}

	}
}
