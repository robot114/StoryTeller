package com.zsm.android.util;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;

import com.zsm.log.Log;

public class IntentUtil {

	public static <E extends Enum<E>> E getEnumValueIntent(
						Intent intent, String key, Class<E> enumClass,
						E defaultValue ) {
		
		String name = intent.getStringExtra( key );
		E e = null; 
		try {
			e = E.valueOf( enumClass, name );
		} catch ( Exception ex ) {
			Log.e( ex, "Invalid value", name );
		}

		return e;
	}
	
	public static void sendActionToService(Context context, String action,
										   int requestCode) {
		
		if( action != null ) {
        	Intent intent = new Intent( action );
        	PendingIntent pi
        		= PendingIntent.getService(context, requestCode, intent, 0);
        	try {
				pi.send();
			} catch (CanceledException e) {
				Log.e( "Action from media button does not sent", "action", action );
			}
        }
	}
}
