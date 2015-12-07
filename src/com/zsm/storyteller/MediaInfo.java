package com.zsm.storyteller;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.zsm.util.TextUtil;

public class MediaInfo {

	private MediaMetadataRetriever metaRetriver;
	private String title;
	private String album;
	private String artist;
	private int duration = -1;
	private Bitmap image;
	private int imageHeight;
	
	public MediaInfo( Context context, Uri uri ) {
		init(context, uri);
	}

	public MediaInfo( Context context, File file ) {
		init( context, Uri.fromFile(file) );
	}

	private void init(Context context, Uri uri) {
		String name = uri.getLastPathSegment();
		metaRetriver = new MediaMetadataRetriever();
		title = titleWithoutExt( name );
		try {
			metaRetriver.setDataSource( context, uri );
			title = getMetaData(MediaMetadataRetriever.METADATA_KEY_TITLE,
								title);
		} catch( IllegalArgumentException | SecurityException  e) {
		}
	}

	private String titleWithoutExt( String name ) {
		int index = name.lastIndexOf( '.' );
		if( index > 0 ) {
			name = name.substring( 0, index);
		}
		
		return name;
	}
	
	public Bitmap getImage( int targetHeight ) {
		if( image == null || this.imageHeight != targetHeight ) {
			image = null;
			this.imageHeight = targetHeight;
			byte[] pic = metaRetriver.getEmbeddedPicture();
			if( pic != null ) {
				BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
				bmpOptions.inJustDecodeBounds = true;
				bmpOptions.inSampleSize = 1;
				BitmapFactory.decodeByteArray(pic, 0, pic.length, bmpOptions );
				int height = bmpOptions.outHeight;
				bmpOptions.inJustDecodeBounds = false;
				bmpOptions.inSampleSize = height/targetHeight;
				image = BitmapFactory.decodeByteArray(pic, 0, pic.length, bmpOptions );
			}
		}
		
		return image;
	}
	
	public String getTitle() {
		return title;
	}

	public String getAlbum() {
		if( album == null ) {
			album = getMetaData( MediaMetadataRetriever.METADATA_KEY_ALBUM );
		}
		return album;
	}
	
	public String getArtist() {
		if( artist == null ) {
			artist = getMetaData( MediaMetadataRetriever.METADATA_KEY_ARTIST );
		}
		return artist;
	}
	
	public int getDuration() {
		if( duration < 0 ) {
			String durationStr
				= getMetaData( MediaMetadataRetriever.METADATA_KEY_DURATION,
							   null );
			
			if( durationStr != null ) {
		        try {
					duration = Integer.parseInt(durationStr);
		        } catch ( NumberFormatException e ) {
		        }
			}
		}
		
		return duration;
	}
	
	public StringBuilder getDurationText() {
		StringBuilder builder = new StringBuilder();
		int duration = getDuration();
		if( duration >= 0 ) {
			TextUtil.appendDurationText(builder, duration);
		}
		
		return builder;
	}
	
	private String getMetaData(int keycode, String defaultValue) {
		if( metaRetriver == null ) {
			return defaultValue;
		}
		String value = metaRetriver.extractMetadata( keycode );
		return value == null ? defaultValue : value;
	}

	private String getMetaData(int keycode) {
		return getMetaData( keycode, "" );
	}
}
