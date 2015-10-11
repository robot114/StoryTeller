package com.zsm.storyteller.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsm.storyteller.R;

class MediaInfoView extends LinearLayout {

	private ImageView imageView;
	private TextView textViewTitle;
	private TextView textViewAlbum;
	private TextView textViewArtist;
	private TextView textViewDuration;
	private TextView textViewPath;

	private MediaMetadataRetriever metaRetriver;
	
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

	private void init( ) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate(R.layout.media_info, this, true);
		
		metaRetriver = new MediaMetadataRetriever();
		
		imageView = (ImageView)findViewById( R.id.imageViewImage );
		textViewTitle = (TextView)findViewById(R.id.textViewTitle);
		textViewAlbum = (TextView)findViewById( R.id.textViewAlbum );
		textViewArtist = (TextView)findViewById( R.id.TextViewArtist );
		textViewDuration = (TextView)findViewById( R.id.TextViewDuration );
		textViewPath = (TextView)findViewById( R.id.textViewPath );
	}
	
	void setDataSource( Uri uri ) {
		metaRetriver.setDataSource(getContext(), uri);
		byte[] pic = metaRetriver.getEmbeddedPicture();
		if( pic != null ) {
			Bitmap image = BitmapFactory.decodeByteArray(pic, 0, pic.length);
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(image);
		} else {
			imageView.setVisibility( View.GONE );
		}
		
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_TITLE,
							 textViewTitle);
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_ALBUM,
							 textViewAlbum);
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_ARTIST,
							 textViewArtist);
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_DURATION,
							 textViewDuration);
		textViewPath.setText(uri.toString());
	}

	private void fillViewWithMetaData(int keycode, TextView textView) {
		String value
			= metaRetriver.extractMetadata( keycode );
		value = value == null ? "" : value;
		textView.setText(value);
	}
}
