package com.zsm.storyteller.play;

import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.END;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.IDLE;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.INITIALIZED;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.PAUSED;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.PLAYBACKCOMPLETED;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.PREPARED;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.PREPARING;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.STARTED;
import static com.zsm.storyteller.play.AbstractPlayer.PLAYER_STATE.STOPPED;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import com.zsm.log.Log;
import com.zsm.storyteller.MediaInfo;

public class DecodingPlayer implements AbstractPlayer {

	PLAYER_STATE[][] STATE_MATRIX = new PLAYER_STATE[][]{
			{IDLE, IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, STOPPED,
		    	PLAYBACKCOMPLETED, null },
		    {INITIALIZED, IDLE, null, null, null, null, null, null, null, null },
			{PREPARING, null, INITIALIZED, null, null, null, null, STOPPED, null, null },
			{PREPARED, null, null, PREPARING, PREPARED, null, null, STOPPED, null, null },
			{STARTED, null, null, null, PREPARED, STARTED, PAUSED, null, PLAYBACKCOMPLETED, null },
			{PAUSED, null, null, null, null, STARTED, PAUSED, null, null, null },
			{STOPPED, null, null, null, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACKCOMPLETED, null },
			{PLAYBACKCOMPLETED, null, null, null, null, STARTED, null, null, null, null },
			{END, IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, STOPPED,
		    	PLAYBACKCOMPLETED, END },
		    };
	private MediaExtractor mMediaExtractor;
	private PLAYER_STATE mState;
	
	private OnPlayerPreparedListener mOnPreparedListener;
	private OnPlayerErrorListener mOnErrorListener;
	private OnPlayerCompletionListener mOnCompletionListener;
	
	private Uri mCurrentPlaying;
	private MediaInfo mMediaInfo;
	private int mTrackIndex;
	private MediaCodec mDecoder;
	private MediaFormat mInputFormat;
	private MediaFormat mOutputFormat;
	private AudioTrack mAudioTrack;

	private Thread mPlayThread;
	private boolean mReleased;
	
	private BufferInfo mBufferInfo = new BufferInfo();
	private boolean mMoreStreamData;
	private int mSeekToMS;
	
	public DecodingPlayer() {
		mState = IDLE;
		
		mPlayThread = new Thread( new Runnable(){
			@Override
			public void run() {
				while (!mReleased) {
					if( mState == STARTED ) {
						handleDecoderBuffer();
					}
				}
			}
		} );
	}
	
	@Override
	public void reset() {
		checkState( IDLE );
		mState = IDLE;
		mCurrentPlaying = null;
		mMediaInfo = null;
	}

	@Override
	public void setDataSource(Context context, Uri currentPlaying)
			throws IOException, IllegalStateException {
		
		checkState( INITIALIZED );
		mMediaInfo = new MediaInfo( context, currentPlaying );
		mMediaExtractor = new MediaExtractor();
		mCurrentPlaying = currentPlaying;
		mMediaExtractor.setDataSource(context, currentPlaying, null);
		mSeekToMS = 0;
		mState = INITIALIZED;
	}

	@Override
	public void prepareAsync() throws IllegalStateException {
		checkState( PREPARING );

		mState = PREPARING;
		new Thread( new Runnable(){

			@Override
			public void run() {
				Log.i( "AudioPlayer begins to asyncprepare" );
				int numTracks = mMediaExtractor.getTrackCount();
				mTrackIndex = -1;
				String mime = null;
				mInputFormat = null;
				for (int i = 0; i < numTracks; ++i) {
					mInputFormat = mMediaExtractor.getTrackFormat(i);
					mime = mInputFormat.getString(MediaFormat.KEY_MIME);
					Log.d( "Exract audio from media.", i, "mime", mime );
					if (mime.startsWith("audio")) {
						mMediaExtractor.selectTrack(i);
						mTrackIndex = i;
						Log.d( "Audio track found.", "track id", mTrackIndex, "format", mInputFormat );
						break;
					}
				}
				
				if( mTrackIndex < 0 ) {
					Log.e( "No audio track found!" );
					handlePrepareError();
					return;
				}
				
				try {
					mDecoder = MediaCodec.createDecoderByType( mime );
				} catch (IOException e) {
					Log.e( e, "Create decoder failed!", "mime type", mime );
					handlePrepareError();
					return;
				}
				
				mDecoder.configure(mInputFormat, null, null, 0);
				
				mOutputFormat = mDecoder.getOutputFormat();

				AudioAttributes attr
					= new AudioAttributes.Builder()
							.setUsage(AudioAttributes.USAGE_MEDIA)
							.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
							.build();

				int sampleRate
					= mOutputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
				int channelMask = AudioFormat.CHANNEL_OUT_STEREO;
				if( mOutputFormat.containsKey( MediaFormat.KEY_CHANNEL_MASK ) ) {
					channelMask
						= mOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_MASK);
				} else if( mOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 1 ) {
					channelMask = AudioFormat.CHANNEL_OUT_MONO;
				}
				
				AudioFormat audioFormat
					= new AudioFormat.Builder()
							.setSampleRate(sampleRate)
							.setChannelMask(channelMask)
							.setEncoding(AudioFormat.ENCODING_DEFAULT)
							.build();

				int bufferSize
					= AudioTrack.getMinBufferSize( sampleRate,
												   channelMask,	// to support smth like 5., 7.1
												   AudioFormat.ENCODING_PCM_16BIT );
				mAudioTrack
					= new AudioTrack(attr, audioFormat, bufferSize,
									 AudioTrack.MODE_STREAM,
									 AudioManager.AUDIO_SESSION_ID_GENERATE);

				Log.i( "AudioPlayer asyncprepared", "sample rate", sampleRate,
						"channelMask", channelMask, "audioFormat", audioFormat,
						"audioTrack bufferSize", bufferSize );
				mState = PLAYER_STATE.PREPARED;
				if (mOnPreparedListener != null) {
					mOnPreparedListener.onPrepared(DecodingPlayer.this);
				}
				if( mSeekToMS != 0 ) {
					mMediaExtractor.seekTo(mSeekToMS*1000L,  MediaExtractor.SEEK_TO_CLOSEST_SYNC );
				}
				mMoreStreamData = true;
			}
		} ).start();
	}

	@Override
	public int getCurrentPosition() {
		return (int)( mMediaExtractor.getSampleTime() /1000L);
	}

	@Override
	public int getDuration() {
		if( mState == IDLE || mState == END ) {
			throw new IllegalStateException( "Invalid state: " + mState );
		}
		
		return mMediaInfo.getDuration();
	}

	@Override
	public void setWakeMode(Context context, int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	synchronized public void start() throws IllegalStateException {
		Log.d( "Start to play.", "state", mState );
		checkState( STARTED );

		mReleased = false;
		if( mState == STARTED ) {
			return;
		} else if( mState == PREPARED ) {
			mDecoder.start();
		} else if( mState == PAUSED ) {
			
		} else if( mState == PLAYBACKCOMPLETED ) {
			mMediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
			mDecoder.configure(mInputFormat, null, null, 0);
			mDecoder.start();
		} else {
			throw new IllegalStateException( "Invalid state: " + mState );
		}
		mAudioTrack.play();
		mState = STARTED;
		if( mPlayThread.getState() == Thread.State.NEW ) {
			mPlayThread.start();
		}
		Log.i( "AudioPlayer started" );
	}

	@Override
	synchronized public void pause() throws IllegalStateException {
		checkState( PAUSED );
		mAudioTrack.pause();
		mState = PAUSED;
		Log.i( "AudioPlayer paused" );
	}

	@Override
	synchronized public void stop() throws IllegalStateException {
		checkState( STOPPED );
		
		mState = STOPPED;
		mMediaExtractor.seekTo( 0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC );
		mDecoder.flush();
		mAudioTrack.flush();
		mAudioTrack.stop();
		mDecoder.stop();
		Log.i( "AudioPlayer stopped" );
	}

	@Override
	synchronized public void release() {
		checkState( END );
		mMediaInfo = null;
		mReleased = true;
		if( mAudioTrack != null ) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}
		
		if( mMediaExtractor != null ) {
			mMediaExtractor.release();
			mMediaExtractor = null;
		}
		
		if( mDecoder != null ) {
			mDecoder.stop();
			mDecoder.release();
			mDecoder = null;
		}
	}

	@Override
	synchronized public void seekTo(int msec) {
		if( mState == IDLE || mState == END ) {
			throw new IllegalStateException( "Invalid state: " + mState );
		}
		if( mState == STARTED || mState == PAUSED ) {
			seekToInner(msec);
		} else {
			mSeekToMS = msec;
		}
	}

	private void seekToInner(int msec) {
		mAudioTrack.pause();
		mMediaExtractor.seekTo(msec*1000L,  MediaExtractor.SEEK_TO_CLOSEST_SYNC );
		mAudioTrack.flush();
		mDecoder.flush();
		mAudioTrack.play();
	}

	@Override
	public boolean isPlaying() {
		return mAudioTrack != null && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
	}

	@Override
	public int getAudioSessionId() {
		if( mAudioTrack == null ) {
			throw new IllegalStateException( "Invalid state: " + mState );
		}
		return mAudioTrack.getAudioSessionId();
	}

	@Override
	public void setOnErrorListener(OnPlayerErrorListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnCompletionListener( OnPlayerCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	@Override
	public void setOnPreparedListener(OnPlayerPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	private void handlePrepareError() {
		mState = IDLE;
		if( mOnErrorListener != null ) {
			mOnErrorListener.onError( DecodingPlayer.this, 0, 0 );
		}
	}

	/**
	 * Fill the decoder's input buffer with the data from the media extractor.
	 * 
	 * @param inputBufferId input buffer id of the decoder
	 * @return true, have more data from the extractor; false reach end of the stream
	 */
	private boolean fillDecoderInputBuffer(int inputBufferId) {
		Log.d( "Input buffer is available for decoder.",
				"buffer id", inputBufferId );
		ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputBufferId);
		int size = mMediaExtractor.readSampleData(inputBuffer, 0);
		mMediaExtractor.advance();
		boolean ret = true;
		int flag = 0;
		if( size < 0 ){
			flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
			size = 0;
			ret = false;
		}
		mDecoder.queueInputBuffer(inputBufferId, 0, size, 0, flag);
		Log.d( "Queued input buffer.", "data size from extractor", size,
				"flag", flag );
		
		return ret;
	}

	private void handleDecoderOutputBuffer(int outputBufferId, boolean moreStreamData) {
		Log.d( "Output buffer is available fom decoder.", "buffer id",
				outputBufferId );
		ByteBuffer outputBuffer = mDecoder.getOutputBuffer(outputBufferId);
		// bufferFormat is identical to outputFormat
		// outputBuffer is ready to be processed or rendered.
		int size
			= mAudioTrack.write(outputBuffer, outputBuffer.remaining(),
								AudioTrack.WRITE_BLOCKING);
		mDecoder.releaseOutputBuffer(outputBufferId, false);
		Log.d( "Written data to the audiotrack, and buffer released.", 
				"data size to audio track", size );
	}

	synchronized private void handleDecoderBuffer() {
		int inputBufferId = mDecoder.dequeueInputBuffer(0);
		if (inputBufferId >= 0) {
			mMoreStreamData = fillDecoderInputBuffer(inputBufferId);
		}
		int outputBufferId = mDecoder.dequeueOutputBuffer(mBufferInfo, 0);
		if (outputBufferId >= 0) {
			handleDecoderOutputBuffer(outputBufferId, mMoreStreamData);
		} else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			// Subsequent data will conform to new format.
			// Can ignore if using getOutputFormat(outputBufferId)
			mOutputFormat = mDecoder.getOutputFormat(); // option B
		} else if( !mMoreStreamData && mOnCompletionListener != null ) {
			mOnCompletionListener.onCompletion( this );
			mState = PLAYBACKCOMPLETED;
		}

	}
	
	private void checkState( PLAYER_STATE newState ) {
		if( STATE_MATRIX[newState.ordinal()][0] != newState ) {
			throw new RuntimeException( 
						"STATE_MATRIX is incorrect, new state: " + newState );
		}
		if( STATE_MATRIX[newState.ordinal()][mState.ordinal()+1] == null ) {
			throw new IllegalStateException( 
							"Invalid state. from state: " + mState
							+ ", to state: " + newState ); 
		}
	}
}
