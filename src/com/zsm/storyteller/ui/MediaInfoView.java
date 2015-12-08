package com.zsm.storyteller.ui;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
			int titleAppearance = android.R.style.TextAppearance_DeviceDefault_Medium;
			int defaultTextAppearance = android.R.style.TextAppearance_DeviceDefault;
			textViewTitle.setTextAppearance(context, titleAppearance);
			textViewAlbum.setTextAppearance(context, defaultTextAppearance);
			textViewArtist.setTextAppearance(context, defaultTextAppearance);
			textViewDuration.setTextAppearance(context, defaultTextAppearance);
			textViewPath.setTextAppearance(context, defaultTextAppearance);
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
		
	}
	
	void setDataSource( Uri uri ) {
		mediaInfo = new MediaInfo( getContext(), uri );
		fillMediaInfo( uri.getLastPathSegment() );
		textViewPath.setText(uri.toString());
	}

	public void setDataSource(File file) {
		mediaInfo = new MediaInfo( getContext(), file );
		fillMediaInfo( file.getName() );
		textViewPath.setText(file.getAbsolutePath());
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
