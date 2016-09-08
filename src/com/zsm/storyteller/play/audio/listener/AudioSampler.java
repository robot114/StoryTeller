package com.zsm.storyteller.play.audio.listener;


public interface AudioSampler {

	void setAudioSession(int audioSession);

	void setEnabled(boolean enabled);

	boolean getEnabled();

	/**
	 * Set the listener to the sampler. The sampler should notify the listener 
	 * at the {@code samplingRate}.
	 *  
	 * @param l the listener to handle the data. Null to remove the listener.
	 * @param captureRate the max rate, in milliHz, the listener should be notified.
	 */
	void setDataListener(AudioDataListener l, int captureRate);
	
	/**
	 * Max length of the data the sampler can handle.
	 * @return
	 */
	int getMaxDataLength();

	/**
	 * Max capture rate of this sampler. 
	 * 
	 * @return max capture rate.
	 */
	int getMaxCaptureRate();

}
