package com.zsm.storyteller.ui.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.zsm.storyteller.play.audio.listener.AudioDataListener;

public class VisualizerView extends View implements AudioDataListener {

	private VisualizerDrawer<Canvas> visualizerDrawer;
	private byte[] data;
	private DATA_FORMAT mDataFormat;
	private Rect rectOfView = new Rect();
	
	public VisualizerView(Context context) {
		super(context);
	}

	public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setViualizerDrawer( VisualizerDrawer<Canvas> drawer ) {
		visualizerDrawer = drawer;
	}

    @Override  
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectOfView.set( getLeft(), getTop(), getRight(), getBottom() );
    	visualizerDrawer.draw(canvas, rectOfView, mDataFormat, data);
    }

	@Override
	public void updateData(DATA_FORMAT format, int samplingRate, byte[] data) {
		mDataFormat = format;
        this.data = data;
        postInvalidate();  
	}

	@Override
	public void setCaptureRate(int captureRate) {
		// Nothing need to be done
	}
}
