package com.zsm.storyteller.ui;

import com.zsm.storyteller.R;
import com.zsm.util.TextUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TimedProgressBar extends LinearLayout {

	private SeekBar progressBar;
	private TextView textViewEllapse;
	private TextView textViewRemain;
	
	public TimedProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TimedProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TimedProgressBar(Context context) {
		super(context);
		init();
	}

	private void init() {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate(R.layout.timed_progress_bar, this, true);
		
		progressBar = (SeekBar)findViewById( R.id.seekBarProgress );
		textViewEllapse = (TextView)findViewById( R.id.textViewTimeEllapsed );
		textViewRemain = (TextView)findViewById( R.id.textViewTimeRemain );
	}

	public void setDuration(int duration) {
		progressBar.setMax(duration);
	}

	public void updateTime(long currentPosition) {
		progressBar.setProgress( (int) currentPosition );
		textViewEllapse.setText( TextUtil.durationToText(currentPosition) );
		textViewRemain.setText( TextUtil.durationToText(
									progressBar.getMax() - currentPosition ) );
	}

	public void setOnSeekBarChangeListener( OnSeekBarChangeListener l ) {
		progressBar.setOnSeekBarChangeListener(l);
	}
}
