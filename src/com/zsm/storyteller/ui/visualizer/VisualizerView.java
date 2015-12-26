package com.zsm.storyteller.ui.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {

	private VisualizerDrawer<Canvas> visualizerDrawer;
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
	
	public void update(byte[] data) {
        this.data = data;
        postInvalidate();  
	}

	public void setViualizerDrawer( VisualizerDrawer<Canvas> drawer ) {
		visualizerDrawer = drawer;
	}

    @Override  
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectOfView.set( getLeft(), getTop(), getRight(), getBottom() );
    	visualizerDrawer.draw(canvas, rectOfView, data);
    }
}
