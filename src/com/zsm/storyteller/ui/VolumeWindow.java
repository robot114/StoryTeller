package com.zsm.storyteller.ui;

import com.zsm.android.ui.PopupWindows;
import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController;
import com.zsm.storyteller.preferences.Preferences;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class VolumeWindow extends PopupWindows {

	private static final int MIN_FACTOR = 0;
	private static final int MAX_FACTOR = 15;
	
	private PlayController mPlayer;
	private TextView mTextVolumeFactor;
	private SeekBar mSeekBar;
	private int mVolumeFactor;

	public VolumeWindow(Context context, PlayController player) {
		super(context, R.layout.volume);
		
		mPlayer = player;
		
		mTextVolumeFactor = (TextView)mRootView.findViewById( R.id.textViewVolumeValue );
		mSeekBar = (SeekBar)mRootView.findViewById( R.id.seekBarVolume );
		mSeekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if( fromUser ) {
					mVolumeFactor = progress+MIN_FACTOR;
					mTextVolumeFactor.setText( String.valueOf( mVolumeFactor ) );
				}
				
				onVolumeFactorChanged();
			}
		} );
		
		mSeekBar.setMax( MAX_FACTOR - MIN_FACTOR );
		
		final View viewUp = mRootView.findViewById( R.id.imageViewVolumeUp );
		final View viewDown = mRootView.findViewById( R.id.imageViewVolumeDown );
		final OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( v.getId() == R.id.imageViewVolumeUp && mVolumeFactor < MAX_FACTOR) {
					mVolumeFactor++;
				} else if( v.getId() == R.id.imageViewVolumeDown && mVolumeFactor > MIN_FACTOR ) {
					mVolumeFactor--;
				}
				
				changeVolumeFactor();
			}
		};
		viewUp.setOnClickListener( clickListener );
		viewDown.setOnClickListener(clickListener);
	}

	@Override
	protected void preShow(View anchor, int rootWidth, int xPos, int yPos) {
		mWindow.setWidth( mRootView.getMeasuredHeight() * 3 );
		mVolumeFactor = Preferences.getInstance().getVolumeFactor();
		changeVolumeFactor();
	}

	private void changeVolumeFactor() {
		mTextVolumeFactor.setText( String.valueOf( mVolumeFactor ) );
		mSeekBar.setProgress(mVolumeFactor-MIN_FACTOR);
	}
	
	private void onVolumeFactorChanged() {
		final Preferences instance = Preferences.getInstance();
		instance.setVolumeFactor( mVolumeFactor );
		mPlayer.changeVolumeLoudnessEnhance(
			instance.getLoudnessEnhancerValuebyVolumeFactor());
	}

}
