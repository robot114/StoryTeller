package com.zsm.storyteller.ui;

import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsm.storyteller.R;
import com.zsm.storyteller.app.StoryTellerApp;
import com.zsm.storyteller.ui.visualizer.BarGraphVisualizer;
import com.zsm.storyteller.ui.visualizer.VisualizerView;

public class VisualizerFragment extends Fragment {

	private View view;
	private VisualizerView mVisualizerView;
	private Visualizer mVisualizer;

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
			
			new Thread( new Runnable() {
				@Override
				public void run() {
					initVisualizer( view );
				}
			}, "InitVisualizer" ).start();
		}
		return view;
	}

	private void initVisualizer( View view ) {
		int maxCR = Visualizer.getMaxCaptureRate();
		StoryTellerApp app = (StoryTellerApp)getActivity().getApplication();
        int audioSessionId = app.getPlayer().getAudioSessionId();
		mVisualizer = new Visualizer(audioSessionId);
		mVisualizer.setEnabled( false );
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);  
		mVisualizer.setDataCaptureListener(
			new Visualizer.OnDataCaptureListener() {
				public void onWaveFormDataCapture(Visualizer visualizer,
						byte[] bytes, int samplingRate) {
					mVisualizerView.update(bytes);
				}

				public void onFftDataCapture(Visualizer visualizer,
						byte[] fft, int samplingRate) {
					mVisualizerView.update(fft);
				}
			}, maxCR / 2, false, true);
	}

	@Override
	public void onDestroy() {
		mVisualizer.release();
		mVisualizer = null;
		view = null;
		super.onDestroy();
	}

	public Visualizer getVisualizer() {
		return mVisualizer;
	}
	
}
