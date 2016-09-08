package com.zsm.storyteller.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsm.storyteller.R;
import com.zsm.storyteller.play.audio.listener.AudioDataListener;
import com.zsm.storyteller.ui.visualizer.BarGraphVisualizer;
import com.zsm.storyteller.ui.visualizer.VisualizerView;

public class VisualizerFragment extends Fragment implements AudioDataListener {

	private View view;
	private VisualizerView mVisualizerView;

	public VisualizerFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if( view == null ) {
			view
				= inflater.inflate( R.layout.visualizer_fragment, 
									container, false );
			mVisualizerView
				= (VisualizerView)view.findViewById( R.id.visualizerView );
			mVisualizerView.setViualizerDrawer( 
					new BarGraphVisualizer( getActivity() ) );
			mVisualizerView.setWillNotDraw( false );
		}
		return view;
	}

	@Override
	public void onDestroy() {
		view = null;
		super.onDestroy();
	}

	@Override
	public void updateData(DATA_FORMAT format, int samplingRate, byte[] data) {
		mVisualizerView.updateData(format, samplingRate, data);
	}

	@Override
	public void setCaptureRate(int captureRate) {
		// Nothing need to be done
	}
}
