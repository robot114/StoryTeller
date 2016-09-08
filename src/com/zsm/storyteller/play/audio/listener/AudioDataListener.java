package com.zsm.storyteller.play.audio.listener;

public interface AudioDataListener {
	
	enum DATA_FORMAT { WAVEFORM, FFT };

	void updateData(DATA_FORMAT format, int samplingRate, byte[] data);

	void setCaptureRate(int captureRate);

}
