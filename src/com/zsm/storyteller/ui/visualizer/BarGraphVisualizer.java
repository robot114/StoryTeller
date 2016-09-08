package com.zsm.storyteller.ui.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.zsm.log.Log;
import com.zsm.storyteller.R;
import com.zsm.storyteller.play.audio.listener.AudioDataListener.DATA_FORMAT;

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
	public void draw(Canvas canvas, Rect rectOfView, DATA_FORMAT format, byte[] data) {
        if (data == null) {  
            return;  
        }  

        if (points == null || points.length < data.length * 4) {  
            points = new float[data.length * 4];  
        }  

        final int height = rectOfView.height();
        
        switch( format ) {
	        case WAVEFORM:
	            //»æÖÆ²¨ÐÎ  
	    		fromWaveForm(data, rectOfView.width(), height);
	    		break;
	        case FFT:
		        //»æÖÆÆµÆ×  
		        fromSpectrum(data, rectOfView.width(), height);
		        break;
		    default:
		    	Log.e( "Unsupported audio data format", format );
		    	return;
        }


        canvas.save();
        canvas.translate( rectOfView.left, rectOfView.top );
        canvas.drawLines(points, forePaint);
        
        canvas.restore();
    }

	private void fromWaveForm(byte[] data, final int width, final int height) {
		final float factor = height / 128.0f;
		final int barNum = Math.min(data.length, width)/5;
		final int numPerGroup = data.length / barNum;
		final float barStep = (float)data.length / barNum;
		final float barWidth = (float)width / barNum;
        float xi = 0;
		for (int i = 0; i < barNum; i++) {
			int y = 0;
			for( int j = 0; j < numPerGroup; j++ ) {
				int index = (int)(i*barStep);
				y += data[index];
			}
			float yi = (float)( height - y*factor/numPerGroup );
            points[i * 4] = xi;  
			points[i * 4 + 1] = yi;
            xi += barWidth;
			points[i * 4 + 2] = xi;
			points[i * 4 + 3] = yi;
		}
	}

	private void fromSpectrum(byte[] data, final int width, final int height) {
		final float factor = height / 128.0f;
        final int baseX = width/data.length;  

        for (int i = 0; i < data.length ; i++) {  
            data[i] = (byte) Math.abs( data[i]);  
            final int xi = baseX*i + baseX/2;
              
            points[i * 4] = xi;  
            points[i * 4 + 1] = height;  
              
            points[i * 4 + 2] = xi;  
            points[i * 4 + 3] = height - data[i]*factor;  
        }
	}
}
