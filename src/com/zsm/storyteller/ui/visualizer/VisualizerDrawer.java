package com.zsm.storyteller.ui.visualizer;

import com.zsm.storyteller.play.audio.listener.AudioDataListener.DATA_FORMAT;

import android.graphics.Rect;

public interface VisualizerDrawer<T> {

	void draw( T canvas, Rect rectOfView, DATA_FORMAT format, byte[] data );
}
