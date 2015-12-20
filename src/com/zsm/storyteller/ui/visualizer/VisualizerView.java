package com.zsm.storyteller.ui.visualizer;

import com.zsm.log.Log;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {

	private VisualizerDrawer<Canvas> visualizerDrawer;
	private int mSpectrumNum = 48;
	private byte[] data;
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

	public void update(byte[] fft) {
		int length = Math.min( fft.length / 2 + 1, mSpectrumNum );
        byte[] model = new byte[length];  
        
        model[0] = (byte) Math.abs(fft[0]);  
        for (int i = 2, j = 1; j < length ;)  
        {  
            model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);  
            i += 2;  
            j++;
        }
        data = model;
        postInvalidate();  
	}

	public void setViualizerDrawer( VisualizerDrawer<Canvas> drawer ) {
		Log.d( drawer );
		visualizerDrawer = drawer;
	}

    @Override  
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectOfView.set( getLeft(), getTop(), getRight(), getBottom() );
    	visualizerDrawer.draw(canvas, rectOfView, data);
    }
}
