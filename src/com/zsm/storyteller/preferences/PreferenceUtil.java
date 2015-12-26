package com.zsm.storyteller.preferences;

import java.security.InvalidParameterException;
import java.util.HashMap;

import com.zsm.log.Log;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class PreferenceUtil {

	public interface ExtrasActionAfterChange<E extends Enum<E>> {
		void action( E e );
	}
	
	private static HashMap<String, ListPreference> listPreferences
				= new HashMap<String, ListPreference>();

	private PreferenceUtil() {
		
	}
	
	public static <E extends Enum<E>> ListPreference
						extractEnumPreference( 
								PreferenceFragment pf, String key,
								Class<E> clazz, E[] enums, E defaultValue ) {
		return extractEnumPreference( pf, key, clazz, enums, defaultValue, null, null );
	}

	public static <E extends Enum<E>> ListPreference
						extractEnumPreference( 
								PreferenceFragment pf, String key,
								Class<E> clazz, E[] enums, E defaultValue,
								int[] iconResId, int[] summaryResId ) {
		
		return extractEnumPreference( pf, key, clazz, enums, defaultValue,
									  iconResId, summaryResId, null );
		
	}
	public static <E extends Enum<E>> ListPreference
						extractEnumPreference( 
								PreferenceFragment pf, String key,
								Class<E> clazz, E[] enums, E defaultValue,
								int[] iconResId, int[] summaryResId,
								ExtrasActionAfterChange<E> action ) {
		
		if( ( iconResId != null && iconResId.length != enums.length ) 
			|| ( summaryResId != null && summaryResId.length != enums.length ) ){
			
			throw new InvalidParameterException( "Arrays with different length!" );
		}
		ListPreference p = listPreferences.get( key );
		if( p == null ) {
			p = (ListPreference) pf.findPreference(key);
		}
		if( p != null ) {
			p.setDefaultValue(defaultValue);
			PreferenceChangeListener<E> l
				= new PreferenceChangeListener<E>(
							clazz, defaultValue, iconResId, summaryResId, action );
			
			p.setOnPreferenceChangeListener(l );
			String values[] = new String[enums.length];
			for( E e : enums ) {
				values[e.ordinal()] = e.name();
			}
			p.setEntryValues( values );
			l.changePreference(p, p.getValue() );
		}
		
		return p;
	}
	
	static public ListPreference getEnumPreference( String key ) {
		return listPreferences.get(key);
	}
	
	private final static class PreferenceChangeListener<E extends Enum<E> >
									implements OnPreferenceChangeListener {
		
		private Class<E> clazz;
		private E defaultValue;
		private int[] iconResId;
		private int[] summaryResId;
		private ExtrasActionAfterChange<E> action;

		public PreferenceChangeListener( Class<E> clazz, E defaultValue,
										 int[] iconResId, int[] summaryResId,
										 ExtrasActionAfterChange<E> action ) {
			
			this.defaultValue = defaultValue;
			this.iconResId = iconResId;
			this.summaryResId = summaryResId;
			this.clazz = clazz;
			this.action = action;
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {

			String key = preference.getKey();
			if (key.equals(preference.getKey())) {
				changePreference(preference, (String) newValue);
			} else {
				return false;
			}

			return true;
		}
		
		private void changePreference( Preference preference, String newValue ) {
			E e = null;
			try {
				e = E.valueOf( clazz, newValue );
			} catch ( Exception ex ) {
				Log.e( ex );
			}
			e = ( e == null ) ? defaultValue: e;
			if( iconResId != null ) {
				preference.setIcon( iconResId[e.ordinal()] );
			}
			if( summaryResId != null ) {
				preference.setSummary( summaryResId[e.ordinal()] );
			}
			
			if( action != null ) {
				action.action(e);
			}
		}

	}

}
