package com.zsm.storyteller.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsm.log.Log;
import com.zsm.storyteller.R;

public class MediaInfoFragment extends Fragment {

	private View view;
	private MediaInfoView mediaInfoView;
	private Uri mDataSource = null;

	public MediaInfoFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(view, this);
		if( view == null ) {
			view
				= inflater.inflate( R.layout.media_info_fragment, 
									container, false );
			mediaInfoView = (MediaInfoView)view.findViewById( R.id.viewMediaInfo );
			if( mDataSource != null ) {
				mediaInfoView.setDataSource( mDataSource );
			}
		}
		return view;
	}

	public void setDataSource(Uri uri) {
		if(mediaInfoView != null ) {
			mediaInfoView.setDataSource( uri );
		}
		mDataSource = uri;
	}

	@Override
	public void onDestroy() {
		view = null;
		mediaInfoView = null;
		super.onDestroy();
	}

}
