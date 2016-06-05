package com.zsm.storyteller.preferences;

import java.util.Hashtable;
import java.util.Set;

import android.content.Context;
import android.graphics.Paint;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.zsm.storyteller.R;
import com.zsm.storyteller.preferences.Preferences.FORWARD_SKIP_TYPE;

public class ForwardSettingPrference extends Preference {

	private interface ProgressConvertor<T> {
		T toProgress( T value );
		T fromProgress( T value );
	}
	
	private enum COMPONENT_KEY {
		RADIO_PERCENT, RADIO_SECOND, SEEKBAR_PERCENT, SEEKBAR_SECOND,
		TEXT_SECOND, EDITTEXT_SKIPHEADER, CHECKBOX_SKIPHEADER,
		SEEKBAR_SKIPHEADER
	}

	private RadioButton radioSkipTypePercent;
	private RadioButton radioSkipTypeSecond;
	private EditText editSkipSecond;
	private SeekBar seekBarPercent;
	private SeekBar seekBarSecond;

	private Hashtable<View, Hashtable<COMPONENT_KEY, View>> views;
	private CheckBox checkBoxSkipHeader;
	private EditText editSkipHeader;
	private SeekBar seekBarSkipHeader;
	
	private ForwardRewindListener forwardRewindListener;
	private SkipHeaderListener skipHeaderListener;
	
	public ForwardSettingPrference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWidgetLayoutResource( R.layout.forward_setting );
		
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
		radioSkipTypePercent
			= (RadioButton) view.findViewById( R.id.radioButtonSkipPercent );
		radioSkipTypeSecond
			= (RadioButton) view.findViewById( R.id.radioButtonSkipSecond );
		editSkipSecond = (EditText) view.findViewById( R.id.editTextSkipSecond );
		seekBarPercent = (SeekBar) view.findViewById( R.id.seekBarSkipPercent );
		seekBarSecond = (SeekBar) view.findViewById( R.id.seekBarSkipSecond );
		
		editSkipHeader = (EditText)view.findViewById( R.id.editTextSkipHeaderValue );
		checkBoxSkipHeader = (CheckBox)view.findViewById( R.id.checkBoxSkipHeader );
		seekBarSkipHeader = (SeekBar)view.findViewById( R.id.seekBarSkipHeader );
		
		vs.put( COMPONENT_KEY.RADIO_PERCENT, radioSkipTypePercent );
		vs.put( COMPONENT_KEY.RADIO_SECOND, radioSkipTypeSecond );
		vs.put( COMPONENT_KEY.TEXT_SECOND, editSkipSecond );
		vs.put( COMPONENT_KEY.SEEKBAR_PERCENT, seekBarPercent );
		vs.put( COMPONENT_KEY.SEEKBAR_SECOND, seekBarSecond );
		vs.put( COMPONENT_KEY.EDITTEXT_SKIPHEADER, editSkipHeader );
		vs.put( COMPONENT_KEY.CHECKBOX_SKIPHEADER, checkBoxSkipHeader );
		vs.put( COMPONENT_KEY.SEEKBAR_SKIPHEADER, seekBarSkipHeader );
		
		initButtonsValue(vs);
		return vs;
	}

	private void initButtonsValue(Hashtable<COMPONENT_KEY, View> vs) {
		Preferences pref = Preferences.getInstance();
		FORWARD_SKIP_TYPE type = pref.getForwardSkipType();
		int progressValueOfPercent = pref.getForwardSkipPercentProgressValue();
		int progressValueOfSecond = pref.getForwardSkipSecondProgressValue();
		
		final RadioButton rsp = (RadioButton) vs.get( COMPONENT_KEY.RADIO_PERCENT );
		final RadioButton rss = (RadioButton) vs.get( COMPONENT_KEY.RADIO_SECOND );
		rsp.setChecked( type == FORWARD_SKIP_TYPE.BY_PERCENT );
		rss.setChecked( type == FORWARD_SKIP_TYPE.BY_SECOND );
		
		((ProgressBar)vs.get(COMPONENT_KEY.SEEKBAR_PERCENT))
			.setProgress(progressValueOfPercent);
		final ProgressBar pss = (ProgressBar)vs.get(COMPONENT_KEY.SEEKBAR_SECOND);
		pss.setProgress(progressValueOfSecond);
		
		setPercentText( rsp, progressValueOfPercent );
		
		final EditText tss = (EditText) vs.get(COMPONENT_KEY.TEXT_SECOND);
		setSecondText( tss, progressValueOfSecond );
		
		setEditTextWidth(tss);
		final ProgressConvertor<Integer> skipSecConvertor = new ProgressConvertor<Integer>() {
			@Override
			public Integer toProgress(Integer value) {
				return Preferences.forwardSkipSecondRealToProgress( value );
			}

			@Override
			public Integer fromProgress(Integer value) {
				return Preferences.forwardSkipSecondProgressToRealValue(value);
			}
		};
		
		tss.setOnEditorActionListener( new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateSeekBarByEdit( tss, pss, skipSecConvertor );
				rsp.setChecked( false );
				rss.setChecked( true );
				storeForwardRewind();
				return true;
			}
		} );
		tss.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				rsp.setChecked( false );
				rss.setChecked( true );
				storeForwardRewind();
			}
		} );
		
		boolean sha = pref.getSkipHeaderAuto();
		final CheckBox skipHeaderAuto = (CheckBox)vs.get( COMPONENT_KEY.CHECKBOX_SKIPHEADER );
		skipHeaderAuto.setChecked( sha );
		int shs = pref.getSkipHeaderValue();
		final EditText tvsh = (EditText)vs.get(COMPONENT_KEY.EDITTEXT_SKIPHEADER);
		setSkipHeaderText( tvsh, shs );
		final SeekBar shaBar = (SeekBar)vs.get( COMPONENT_KEY.SEEKBAR_SKIPHEADER );
		shaBar.setProgress( shs );
		
		setEditTextWidth( tvsh );
		
		final ProgressConvertor<Integer> skipHeaderConvertor = new ProgressConvertor<Integer>() {
			@Override
			public Integer toProgress(Integer value) {
				return value;
			}

			@Override
			public Integer fromProgress(Integer value) {
				return value;
			}
		};
		
		tvsh.setOnEditorActionListener( new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateSeekBarByEdit( tvsh, shaBar, skipHeaderConvertor );
				storeSkipHeader();
				return true;
			}
		} );
		tvsh.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				skipHeaderAuto.setChecked(true);
				storeSkipHeader();
			}
		} );
	}

	private void setEditTextWidth(final EditText editor) {
		Paint textPaint = editor.getPaint();
		int width = (int) textPaint.measureText("XXXXX");
		editor.setMinWidth( width );
		editor.setMaxWidth( width );
	}
	
	private void updateSeekBarByEdit( EditText edit, ProgressBar bar,
									  ProgressConvertor<Integer> convertor ) {
		
		String text = edit.getText().toString().trim();
		int progressValue = bar.getProgress();
		try {
			int value = Integer.parseInt(text);
			progressValue = convertor.toProgress(value);
			progressValue = Math.max( Math.min( progressValue, bar.getMax() ), 0 );
			bar.setProgress(progressValue);
			
			value = convertor.fromProgress(progressValue);
			String strValue = String.valueOf( value );
			if( !text.equals( strValue ) ) {
				edit.setText( strValue );
				edit.setSelection(strValue.length());
			}
		} catch ( Exception e ) {
			Toast.makeText(getContext(), R.string.inputError, Toast.LENGTH_SHORT )
				 .show();
			edit.requestFocus();
		}
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
		
		vs.get( COMPONENT_KEY.EDITTEXT_SKIPHEADER )
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
			
			editSkipSecond  = (EditText) vs.get(COMPONENT_KEY.TEXT_SECOND);
			
			seekBarPercent = (SeekBar) vs.get( COMPONENT_KEY.SEEKBAR_PERCENT );
			seekBarSecond = (SeekBar) vs.get( COMPONENT_KEY.SEEKBAR_SECOND );
			checkBoxSkipHeader
				= (CheckBox)vs.get( COMPONENT_KEY.CHECKBOX_SKIPHEADER );
			editSkipHeader
				= (EditText)vs.get( COMPONENT_KEY.EDITTEXT_SKIPHEADER );
			seekBarSkipHeader
				= (SeekBar)vs.get( COMPONENT_KEY.SEEKBAR_SKIPHEADER );
		}
		views.clear();
		storeViews( vs );
	}

	private void setSecondText(EditText tss, int factoredValue) {
		if( tss == null ) {
			return;
		}
		int second
			= Preferences.forwardSkipSecondProgressToRealValue( factoredValue);
		tss.setText( String.valueOf( second ) );
	}

	private void setPercentText(RadioButton button, int factoredValue) {
		float percent 
			= Preferences
				.forwardSkipPercentProgressToRealValue( factoredValue);
		String text
			= getContext().getResources()
				.getString( R.string.prefForwardRewindByPercent, percent);
		button.setText( text );
	}

	private void setSkipHeaderText(EditText tv, int value) {
		tv.setText( String.valueOf(value) );
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
			
			setSkipHeaderText(editSkipHeader, progress );
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
			if( v != editSkipHeader ) {
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
					setSecondText(editSkipSecond, progress);
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
