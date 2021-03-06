package com.zsm.storyteller.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsm.storyteller.MediaInfo;
import com.zsm.storyteller.R;

class MediaInfoView extends LinearLayout {

	private ImageView imageView;
	private TextView textViewTitle;
	private TextView textViewAlbum;
	private TextView textViewArtist;
	private TextView textViewDuration;
	private TextView textViewPath;

	private long duration;
	private MediaInfo mediaInfo;
	
	public MediaInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MediaInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MediaInfoView(Context context) {
		super(context);
		init();
	}

	public MediaInfoView(Context context, boolean smallTextSize ) {
		super(context);
		init();
		if( smallTextSize ) {
			smallTextSize( this );
			textViewTitle.setVisibility( View.VISIBLE );
		}
	}

	private void init( ) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate(R.layout.media_info, this, true);
		
		imageView = (ImageView)findViewById( R.id.imageViewImage );
		textViewTitle = (TextView)findViewById(R.id.textViewTitle);
		textViewAlbum = (TextView)findViewById( R.id.textViewAlbum );
		textViewArtist = (TextView)findViewById( R.id.TextViewArtist );
		textViewDuration = (TextView)findViewById( R.id.TextViewDuration );
		textViewPath = (TextView)findViewById( R.id.textViewPath );
		
		clearAll();
	}
	
	private void smallTextSize(ViewGroup view) {
		Context context = getContext();
		final int defaultTextAppearance = android.R.style.TextAppearance_DeviceDefault;
		for(  int i = 0; i < view.getChildCount(); i++ ) {
			View v = view.getChildAt(i);
			if( v.getId() == R.id.textViewTitle ) {
				final int titleAppearance = android.R.style.TextAppearance_DeviceDefault_Medium;
				textViewTitle.setTextAppearance(context, titleAppearance);
			} else if ( v instanceof TextView ) {
				TextView tv = (TextView)v;
				tv.setTextAppearance(context, defaultTextAppearance);
				
			} else if( v instanceof ViewGroup ) {
				smallTextSize((ViewGroup) v);
			}
		}
	}
	
	private void clearAll() {
		imageView.setVisibility( View.GONE );
		textViewTitle.setText( "" );
		textViewAlbum.setText( "" );
		textViewArtist.setText( "" );
		textViewDuration.setText( "" );
		textViewPath.setText( "" );
	}

	void setDataSource( Uri uri ) {
		mediaInfo = new MediaInfo( getContext(), uri );
		fillMediaInfo( mediaInfo.getTitle() );
		textViewPath.setText(mediaInfo.getUri().getPath());
	}

	public long getMediaDuration() {
		return mediaInfo.getDuration();
	}
	
	private void fillMediaInfo(String defaultTitle) {
		int totalHeight
			= getTotalHeight( textViewTitle, textViewAlbum, textViewArtist,
							  textViewDuration, textViewPath );
		Bitmap image = mediaInfo.getImage( totalHeight );
		if( image != null ) {
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(image);
		} else {
			imageView.setVisibility( View.GONE );
		}
		textViewTitle.setText( mediaInfo.getTitle() );
		textViewAlbum.setText( mediaInfo.getAlbum() );
		textViewArtist.setText( mediaInfo.getArtist() );
		textViewDuration.setText( mediaInfo.getDurationText() );
		
		duration = mediaInfo.getDuration();
	}

	private int getTotalHeight( TextView ... vs ) {
		int totalHeight = 0;
		for( TextView v : vs ) {
			totalHeight += v.getTextSize() + v.getPaddingBottom() + v.getPaddingTop();
		}
		return totalHeight;
	}
}
