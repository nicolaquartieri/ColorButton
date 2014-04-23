package com.example.colorbutton;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.colorbutton.fft.FFT;

public class MainActivity extends Activity {

	private Button b1 = null;
	private Button b2 = null;
	private Button b3 = null;
	private Button b4 = null;
	private Button b5 = null;
	private Button b6 = null;
	
	private ColorTask ct = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		b1 = (Button) findViewById(R.id.Button01);
		b2 = (Button) findViewById(R.id.button02);
		b3 = (Button) findViewById(R.id.Button03);
		b4 = (Button) findViewById(R.id.Button04);
		b5 = (Button) findViewById(R.id.Button05);
		b6 = (Button) findViewById(R.id.Button06);
		
		ct = new ColorTask(MainActivity.this);
		ct.execute();
	}

	private void changeButton01Color(Integer c) {
		b1.setBackgroundColor(c);
	}
	
	private void changeButton02Color(Integer c) {
		b2.setBackgroundColor(c);
	}
	
	private void changeButton03Color(Integer c) {
		b3.setBackgroundColor(c);
	}
	
	private void changeButton04Color(Integer c) {
		b4.setBackgroundColor(c);
	}
	
	private void changeButton05Color(Integer c) {
		b5.setBackgroundColor(c);
	}
	
	private void changeButton06Color(Integer c) {
		b6.setBackgroundColor(c);
	}
	
	// AsyncTask
    private static class ColorTask extends AsyncTask<Void, Integer, Void> {
    	int       RECORDER_SAMPLERATE = 44100;
    	int       MAX_FREQ = RECORDER_SAMPLERATE/2;
    	final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    	final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    	final int PEAK_THRESH = 20;

    	short[]     buffer           = null;
    	int         bufferReadResult = 0;
    	AudioRecord audioRecord      = null;
    	boolean     aRecStarted      = false;
    	int         bufferSize       = 2048;
    	int         minBufferSize    = 0;
    	float       volume           = 0;
    	FFT         fft              = null;
    	float[]     fftRealArray     = null;
    	int         mainFreq         = 0;

    	float       drawScaleH       = 1.5f; // TODO: calculate the drawing scales
    	float       drawScaleW       = 1.0f; // TODO: calculate the drawing scales
    	int         drawStepW        = 2;   // display only every Nth freq value
    	float       maxFreqToDraw    = 2500; // max frequency to represent graphically
    	int         drawBaseLine     = 0;
    	
        private MainActivity mActivity;
    	
        
    	public ColorTask(MainActivity activity) {
    		attach(activity);
    		
			minBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
					RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
			// if we are working with the android emulator, getMinBufferSize()
			// does not work
			// and the only samplig rate we can use is 8000Hz
			if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
				RECORDER_SAMPLERATE = 8000; // forced by the android emulator
				MAX_FREQ = RECORDER_SAMPLERATE / 2;
				bufferSize = 2 << (int) (Math.log(RECORDER_SAMPLERATE) / Math.log(2) - 1);// buffer
																				// size
																				// must
																				// be
																				// power
																				// of
																				// 2!!!
				// the buffer size determines the analysis frequency at:
				// RECORDER_SAMPLERATE/bufferSize
				// this might make trouble if there is not enough computation
				// power to record and analyze
				// a frequency. In the other hand, if the buffer size is too
				// small AudioRecord will not initialize
			} else
				bufferSize = minBufferSize;

			buffer = new short[bufferSize];
			// use the mic with Auto Gain Control turned off!
			audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.VOICE_RECOGNITION,
					RECORDER_SAMPLERATE, RECORDER_CHANNELS,
					RECORDER_AUDIO_ENCODING, bufferSize);

			// audioRecord = new AudioRecord( MediaRecorder.AudioSource.MIC,
			// RECORDER_SAMPLERATE,
			// RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
			if ((audioRecord != null)
					&& (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)) {
				try {
					// this throws an exception with some combinations
					// of RECORDER_SAMPLERATE and bufferSize
					audioRecord.startRecording();
					aRecStarted = true;
				} catch (Exception e) {
					aRecStarted = false;
				}

				if (aRecStarted) {
					bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
					// verify that is power of two
					if (bufferReadResult % 2 != 0)
						bufferReadResult = 2 << (int) (Math.log(bufferReadResult) / Math.log(2));

					fft = new FFT(bufferReadResult, RECORDER_SAMPLERATE);
					fftRealArray = new float[bufferReadResult];					
				}
			}
		}
    	
		public void detach() {
    		mActivity = null;
    	}
    	
    	public void attach(MainActivity activity) {
    		mActivity = activity;
    	}    	    
    	
    	int c = 0;
    	
        protected void onProgressUpdate(Integer... values) {
        	int v = 1 + (int)(Math.sin(c) * ((255 - 1) + 1));
        	
        	mActivity.changeButton01Color(Color.rgb((int) (fft.getFreq(149) * 10), 0, 0));
        	mActivity.changeButton02Color(Color.rgb(0, (int) (fft.getFreq(299) * 10), 0));
        	mActivity.changeButton03Color(Color.rgb(0, 0, (int) (fft.getFreq(459) * 10)));
        	mActivity.changeButton04Color(Color.rgb((int) (fft.getFreq(299) * 10), (int) (fft.getFreq(599) * 10), 0));
        	mActivity.changeButton05Color(Color.rgb((int) (fft.getFreq(299) * 10), 0, (int) (fft.getFreq(749) * 10)));
        	mActivity.changeButton06Color(Color.rgb(0, (int) (fft.getFreq(299) * 10), (int) (fft.getFreq(899) * 10)));

        	//Log.i("INFO", "TODO " + c++ + " " + c);
        }

		@Override
		protected Void doInBackground(Void... params) {
			
			while (true) {
				if (aRecStarted) {
					bufferReadResult = audioRecord.read(buffer, 0, bufferSize);

					// After we read the data from the AudioRecord object, we
					// loop through
					// and translate it from short values to double values. We
					// can't do this
					// directly by casting, as the values expected should be
					// between -1.0 and 1.0
					// rather than the full range. Dividing the short by 32768.0
					// will do that,
					// as that value is the maximum value of short.
					volume = 0;
					for (int i = 0; i < bufferReadResult; i++) {
						fftRealArray[i] = (float) buffer[i] / Short.MAX_VALUE;// 32768.0;
						volume += Math.abs(fftRealArray[i]);
					}
					volume = (float) Math.log10(volume / bufferReadResult);

					// apply windowing
					for (int i = 0; i < bufferReadResult / 2; ++i) {
						// Calculate & apply window symmetrically around center
						// point
						// Hanning (raised cosine) window
						float winval = (float) (0.5f + 0.5f * Math.cos(Math.PI
								* (float) i / (float) (bufferReadResult / 2)));
						if (i > bufferReadResult / 2)
							winval = 0;
						fftRealArray[bufferReadResult / 2 + i] *= winval;
						fftRealArray[bufferReadResult / 2 - i] *= winval;
					}
					// zero out first point (not touched by odd-length window)
					fftRealArray[0] = 0;
					fft.forward(fftRealArray);

					for (float freq = RECORDER_SAMPLERATE / 2 - 1; freq > 0.0f; freq -= 150.0f) {
						int y = -(int) (fft.freqToIndex(freq) * drawScaleW); // which
																				// bin
																				// holds
																				// this
																				// frequency?
						// Frecuencia
//						Log.i("INFO", Math.round(freq) + " Hz");
//						text(Math.round(freq) + " Hz", 10, y); // add text label
					}

					Log.i("INFO", "Amp: " + fft.getFreq(299));
					publishProgress();
					
					float lastVal = 0;
					float val = 0;
					float maxVal = 0; // index of the bin with highest value
					int maxValIndex = 0; // index of the bin with highest value
					for (int i = 0; i < fft.specSize(); i++) {
						val += fft.getBand(i);
						if (i % drawStepW == 0) {
							val /= drawStepW; // average volume value
							int prev_i = i - drawStepW;
							// draw the line for frequency band i, scaling it up
							// a bit so we can see it

							if (val - lastVal > PEAK_THRESH) {
								if (val > maxVal) {
									maxVal = val;
									maxValIndex = i;
								}
							}

							lastVal = val;
							val = 0;
						}
					}
					if (maxValIndex - drawStepW > 0) {
//						text(" " + fft.indexToFreq(maxValIndex - drawStepW / 2) + "Hz", 25 + maxValIndex * drawScaleW, drawBaseLine - maxVal * drawScaleH);
					}
					
//					Log.i("INFO", "buffer readed: " + bufferReadResult);
//					Log.i("INFO", "fft spec size: " + fft.specSize());
//					Log.i("INFO", "volume: " + volume);
					
//					text("buffer readed: " + bufferReadResult, 20, 80);
//					text("fft spec size: " + fft.specSize(), 20, 100);
//					text("volume: " + volume, 20, 120);
				} else {
					Log.e("ERROR", "AUDIO RECORD NOT INITIALIZED!!!");
//					text("AUDIO RECORD NOT INITIALIZED!!!", 100, height / 2);
				}
//				text("sample rate: " + RECORDER_SAMPLERATE + " Hz", 20, 80);
//				text("displaying freq: 0 Hz  to  " + maxFreqToDraw + " Hz", 20,	100);
//				text("buffer size: " + bufferSize, 20, 120);
				
				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
    }
	
}
