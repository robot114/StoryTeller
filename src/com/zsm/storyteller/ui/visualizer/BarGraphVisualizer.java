package com.zsm.storyteller.ui.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.zsm.storyteller.R;

public class BarGraphVisualizer implements VisualizerDrawer<Canvas> {

	private Paint forePaint = new Paint();
	private float[] points;

	public BarGraphVisualizer( Context context ) {
        forePaint.setStrokeWidth(8f);  
        forePaint.setAntiAlias(true);
        int c = context.getResources().getColor( R.color.audioVisualizerBar );
        forePaint.setColor( c );  
	}

	@Override
	public void draw(Canvas canvas, Rect rectOfView, byte[] fft) {
        if (fft == null) {  
            return;  
        }  

        if (points == null || points.length < fft.length * 4) {  
            points = new float[fft.length * 4];  
        }  

        //»æÖÆ²¨ÐÎ  
        // for (int i = 0; i < mBytes.length - 1; i++) {  
        // mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);  
        // mPoints[i * 4 + 1] = mRect.height() / 2  
        // + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;  
        // mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);  
        // mPoints[i * 4 + 3] = mRect.height() / 2  
        // + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;  
        // }  

        //»æÖÆÆµÆ×  
        final int baseX = rectOfView.width()/fft.length;  
        final int height = rectOfView.height();  

        for (int i = 0; i < fft.length ; i++) {  
            if (fft[i] < 0) {  
                fft[i] = 0;  
            }  
              
            final int xi = baseX*i + baseX/2;  
              
            points[i * 4] = xi;  
            points[i * 4 + 1] = height;  
              
            points[i * 4 + 2] = xi;  
            points[i * 4 + 3] = height - fft[i];  
        }  

        canvas.save();
        canvas.translate( rectOfView.left, rectOfView.top );
        canvas.drawLines(points, forePaint);
        
        canvas.restore();
    }
}
