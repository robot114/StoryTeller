package com.zsm.storyteller.play;

import com.zsm.log.Log;

public class PauseAudioDataListener implements AudioDataListener {

	private PlayController player;
	private boolean soundPlayed = false;
	private int silenceTimes = 0;
	private int maxSilenceTimes = 3;
	private int silenceToilence = 5;

	public PauseAudioDataListener( PlayController player ) {
		this.player = player;
	}

	@Override
	public void updateData(byte[] data) {
		int i = 0;
		for( ; i < data.length && Math.abs( data[i] ) < silenceToilence; i++ ) {
		}
		if( i >= data.length ) {
			if( soundPlayed && silenceTimes > maxSilenceTimes ) {
				player.pause( true );
				soundPlayed = false;
			}
			silenceTimes++;
		} else {
			soundPlayed = true;
			silenceTimes = 0;
		}
		Log.d( i, soundPlayed, silenceTimes );
	}

	public void setMaxSilenceTimes(int maxSilenceTimes) {
		this.maxSilenceTimes = maxSilenceTimes;
	}

	public void setSilenceToilence(int silenceToilence) {
		this.silenceToilence = silenceToilence;
	}

}
