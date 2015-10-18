package com.zsm.storyteller.ui;

import java.io.File;

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
import android.widget.TableLayout;
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
	
	private BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
	private TableLayout tableView;
	
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
		
		metaRetriver = new MediaMetadataRetriever();
		
		imageView = (ImageView)findViewById( R.id.imageViewImage );
		textViewTitle = (TextView)findViewById(R.id.textViewTitle);
		textViewAlbum = (TextView)findViewById( R.id.textViewAlbum );
		textViewArtist = (TextView)findViewById( R.id.TextViewArtist );
		textViewDuration = (TextView)findViewById( R.id.TextViewDuration );
		textViewPath = (TextView)findViewById( R.id.textViewPath );
		
		tableView = (TableLayout)findViewById( R.id.tableView);
	}
	
	void setDataSource( Uri uri ) {
		metaRetriver.setDataSource(getContext(), uri);
		fillMediaInfo( uri.getLastPathSegment() );
		textViewPath.setText(uri.toString());
	}

	public void setDataSource(File file) {
		metaRetriver.setDataSource(getContext(), Uri.fromFile(file));
		fillMediaInfo( file.getName() );
		textViewPath.setText(file.getAbsolutePath());
	}

	private void fillMediaInfo(String defaultTitle) {
		byte[] pic = metaRetriver.getEmbeddedPicture();
		Bitmap image = null;
		if( pic != null ) {
			bmpOptions.inJustDecodeBounds = true;
			bmpOptions.inSampleSize = 1;
			BitmapFactory.decodeByteArray(pic, 0, pic.length, bmpOptions );
			int width = bmpOptions.outWidth;
			int height = bmpOptions.outHeight;
			int totalHeight
				= getTotalHeight( textViewTitle, textViewAlbum, textViewArtist,
								  textViewDuration, textViewPath );
			bmpOptions.inJustDecodeBounds = false;
			bmpOptions.inSampleSize = height/totalHeight;
			image = BitmapFactory.decodeByteArray(pic, 0, pic.length, bmpOptions );
		}
		
		if( image != null ) {
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(image);
		} else {
			imageView.setVisibility( View.GONE );
		}
		
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_TITLE,
							 defaultTitle, textViewTitle);
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_ALBUM,
							 textViewAlbum);
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_ARTIST,
							 textViewArtist);
		fillViewWithMetaData(MediaMetadataRetriever.METADATA_KEY_DURATION,
							 textViewDuration);
	}

	private void fillViewWithMetaData(int keycode, TextView textView) {
		fillViewWithMetaData(keycode, "", textView);
	}
	
	private void fillViewWithMetaData(int keycode, String defaultValue, 
									  TextView textView) {
		String value
			= metaRetriver.extractMetadata( keycode );
		value = value == null ? defaultValue : value;
		textView.setText(value);
	}
	
	private int getTotalHeight( TextView ... vs ) {
		int totalHeight = 0;
		for( TextView v : vs ) {
			totalHeight += v.getTextSize() + v.getPaddingBottom() + v.getPaddingTop();
		}
		return totalHeight;
	}
}
