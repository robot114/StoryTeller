package com.zsm.storyteller.ui.visualizer;

import android.graphics.Rect;

public interface VisualizerDrawer<T> {

	void draw( T canvas, Rect rectOfView, byte[] fft );
}
