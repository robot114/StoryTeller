package com.zsm.storyteller;

import java.io.File;

import com.zsm.util.TextUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

public class MediaInfo {

	private MediaMetadataRetriever metaRetriver;
	private String title;
	
	public MediaInfo( Context context, Uri uri ) {
		metaRetriver = new MediaMetadataRetriever();
		title = titleWithoutExt( uri.getLastPathSegment() );
		try {
			metaRetriver.setDataSource( context, uri );
			title = getMetaData(MediaMetadataRetriever.METADATA_KEY_TITLE,
								title);
		} catch( IllegalArgumentException | SecurityException  e) {
		}
	}

	public MediaInfo( Context context, File file ) {
		metaRetriver = new MediaMetadataRetriever();
		title = titleWithoutExt( file.getName() );
		try {
			metaRetriver.setDataSource( context, Uri.fromFile(file) );
			title = getMetaData(MediaMetadataRetriever.METADATA_KEY_TITLE,
								title );
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
		byte[] pic = metaRetriver.getEmbeddedPicture();
		Bitmap image = null;
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
		
		return image;
	}
	
	public String getTitle() {
		return title;
	}

	public String getAlbum() {
		return getMetaData( MediaMetadataRetriever.METADATA_KEY_ALBUM );
	}
	
	public String getArtist() {
		return getMetaData( MediaMetadataRetriever.METADATA_KEY_ARTIST );
	}
	
	public int getDuration() {
		String durationStr
			= getMetaData( MediaMetadataRetriever.METADATA_KEY_DURATION, null );
		if( durationStr != null ) {
	        try {
				return Integer.parseInt(durationStr);
	        } catch ( NumberFormatException e ) {
	        }
		}
		
		return -1;
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
