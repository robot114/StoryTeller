package com.zsm.storyteller.ui;

import java.util.List;

import com.zsm.storyteller.R;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

	private static final int MEDIA_INFO_POSITION = 0;
	private static final int VISUALIZER_POSITION = 1;
	private static final int[] PAGE_TITLE_RESID
		= { R.string.pageTitleMediaInfo, R.string.pageTitleVisualizer };
	
	private Fragment[] fragments;
	private Context context;
	
	public MainFragmentPagerAdapter(FragmentManager fragmentManager,
									Context context,
									Bundle savedInstanceState ) {
		
		super(fragmentManager);
		this.context = context;
		if( savedInstanceState == null ) {
			fragments
				= new Fragment[] { new MediaInfoFragment(),
								   new VisualizerFragment() };
		} else {
			List<Fragment> l = fragmentManager.getFragments();
			fragments = new Fragment[l.size()];
			for( Fragment f : l ) {
				if( f instanceof MediaInfoFragment ) {
					fragments[MEDIA_INFO_POSITION] = f;
				} else if( f instanceof VisualizerFragment ){
					fragments[VISUALIZER_POSITION] = f;
				}
			}
		}
		
	}

	@Override
	public Fragment getItem(int position) {
		return fragments[position];
	}

	@Override
	public int getCount() {
		return fragments.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return context.getString( PAGE_TITLE_RESID[position] );
	}

	public void setDataSource(int position, Uri uri) {
		if( position == MEDIA_INFO_POSITION ) {
			MediaInfoFragment mif = (MediaInfoFragment)getItem(position);
			mif.setDataSource( uri );
		}
	}

	public Visualizer getVisualizer() {
		VisualizerFragment vf = (VisualizerFragment) getItem(VISUALIZER_POSITION);
		return vf.getVisualizer();
	}

}
