package com.MeadowEast.audiotest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Took out OnLongClickListener
 */
public class MainActivity extends Activity implements OnClickListener,
		/*OnLongClickListener,*/ OnGesturePerformedListener {
	
	private GestureLibrary gestureLib;
	private static final String GESTURE_TAG = "complex gestures";
	
	private static final String DEBUG_TAG = "gestures";
	private MediaPlayer mp;
	private String[] cliplist;
	private File sample;
	private File mainDir;
	private File clipDir;
	private Random rnd;
	private Button mbutton1;
	private Handler clockHandler;
	private Runnable updateTimeTask;
	private boolean clockRunning;
	private boolean clockWasRunning;
	private boolean nightModeOn;
	private Long elapsedMillis;
	private Long start;
	private Map<String, String> hanzi;
	private Map<String, String> instructions;
	private String key;
	static final String TAG = "CAT";

	private void readClipInfo() {
		hanzi = new HashMap<String, String>();
		instructions = new HashMap<String, String>();
		File file = new File(mainDir, "clipinfo.txt");
		Log.d(TAG, "before");
		Log.d(TAG, "after");
		try {
			FileReader fr = new FileReader(file);
			BufferedReader in = new BufferedReader(fr);
			String line;
			while ((line = in.readLine()) != null) {
				String fixedline = new String(line.getBytes(), "utf-8");
				String[] fields = fixedline.split("\\t");
				if (fields.length == 3) {
					hanzi.put(fields[0], fields[1]);
					instructions.put(fields[0], fields[2]);
				} else {
					Log.d(TAG, "Bad line: " + fields.length + " elements");
					Log.d(TAG, fixedline);
				}
			}
			in.close();
		} catch (Exception e) {
			Log.d(TAG, "Problem reading clipinfo");
		}
	}

	private String getInstruction(String key) {
		String instructionCodes = instructions.get(key);
		int n = instructionCodes.length();
		if (n == 0) {
			return "No instruction codes for " + key;
		}
		int index = rnd.nextInt(n);
		switch (instructionCodes.charAt(index)) {
		case 'C':
			return "continue the conversation";
		case 'A':
			return "answer the question";
		case 'R':
			return "repeat";
		case 'P':
			return "paraphrase";
		case 'Q':
			return "ask questions";
		case 'V':
			return "create variations";
		default:
			return "Bad instruction code " + instructionCodes.charAt(index)
					+ " for " + key;
		}
	}

	private void toggleNightMode() {
		if (nightModeOn) {

		} else {

		}
	}

	/*
	 * public void myClickHandler(View view){ case R.id.button1: mlayout =
	 * findViewById(R.id.LinearLayout1);
	 * mlayout.setBackgroundColor(color.black); break; }
	 */

	private void toggleClock() {
		if (clockRunning) {
			elapsedMillis += System.currentTimeMillis() - start;
			setHanzi("");
		} else
			start = System.currentTimeMillis();
		clockRunning = !clockRunning;
		clockHandler.removeCallbacks(updateTimeTask);
		if (clockRunning)
			clockHandler.postDelayed(updateTimeTask, 200);
	}

	private void showTime(Long totalMillis) {
		int seconds = (int) (totalMillis / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		TextView t = (TextView) findViewById(R.id.timerTextView);
		if (seconds < 10)
			t.setText("" + minutes + ":0" + seconds);
		else
			t.setText("" + minutes + ":" + seconds);
	}

	private void createUpdateTimeTask() {
		updateTimeTask = new Runnable() {
			public void run() {
				Long totalMillis = elapsedMillis + System.currentTimeMillis()
						- start;
				showTime(totalMillis);
				clockHandler.postDelayed(this, 1000);
			}
		};
	}

	private void setHanzi(String s) {
		TextView t = (TextView) findViewById(R.id.hanziTextView);
		t.setText(s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "testing only");
		// File filesDir = getFilesDir(); // Use on virtual device

		File sdCard = Environment.getExternalStorageDirectory();

		mainDir = new File(sdCard.getAbsolutePath()
				+ "/Android/data/com.MeadowEast.audiotest/files");

		// old
		// File filesDir = new File (sdCard.getAbsolutePath() +
		// "/Android/data/com.MeadowEast.audiotest/files");
		// mainDir = new File(filesDir, "ChineseAudioTrainer");

		clipDir = new File(mainDir, "clips");
		cliplist = clipDir.list();
		readClipInfo();
		rnd = new Random();
		setContentView(R.layout.activity_main);	

		/*
		 * Gestures begins -- Vinson
		 */
		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.activity_main, null);
		gestureOverlayView.addView(inflate);
		gestureOverlayView.setGestureVisible(false);
		gestureOverlayView.addOnGesturePerformedListener(this);
		
		/*
		 * NEED THE RAW FOLD WITH GESTURES IN PACKAGE EXPLORER
		 */
		gestureLib = GestureLibraries.fromRawResource(this,R.raw.gestures);
		if (!gestureLib.load()) {
			//finish();
			Log.i(GESTURE_TAG, "FAILED");
		}
		else {
			Log.i(GESTURE_TAG, "LOADED");
		}
		setContentView(gestureOverlayView);
		
		findViewById(R.id.playButton).setOnClickListener(this);
		findViewById(R.id.repeatButton).setOnClickListener(this);
		findViewById(R.id.hanziButton).setOnClickListener(this);
		findViewById(R.id.timerTextView).setOnClickListener(this);
		//findViewById(R.id.hanziTextView).setOnLongClickListener(this);
		findViewById(R.id.hanziTextView).setOnTouchListener(new OnSwipeTouchListener(this) {
			@Override
			public void onTap() {
					if (!clockRunning)
						toggleClock();
					if (sample != null)
						setHanzi(hanzi.get(key));
					
					Toast.makeText(MainActivity.this, "Tap", Toast.LENGTH_SHORT).show();
			}
			
		    @Override
		    public void onSwipeBottom() {
		    	toggleClock();
		        Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT).show();
		    }
		    @Override
		    public void onLongPressClick() {
		    	Toast.makeText(MainActivity.this, "Clip: " + key, Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Long clicked");
		    }
		});
		/*
		 * Gesture ends
		 */
		
		mbutton1 = (Button) findViewById(R.id.button1);
		mbutton1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(nightModeOn){
					findViewById(R.id.LinearLayout1).setBackgroundColor(getResources().getColor(R.color.white));
					findViewById(R.id.button1).setBackgroundColor(getResources().getColor(R.color.silver));
					((TextView) findViewById(R.id.button1)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.playButton).setBackgroundColor(getResources().getColor(R.color.silver));
					((TextView) findViewById(R.id.playButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.hanziButton).setBackgroundColor(getResources().getColor(R.color.silver));
					((TextView) findViewById(R.id.hanziButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.repeatButton).setBackgroundColor(getResources().getColor(R.color.silver));
					((TextView) findViewById(R.id.repeatButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.pauseButton).setBackgroundColor(getResources().getColor(R.color.silver));
					((TextView) findViewById(R.id.pauseButton)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.timerTextView)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.hanziTextView)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.instructionTextView)).setTextColor(getResources().getColor(R.color.black));
					Toast.makeText(MainActivity.this, R.string.dayMode_toast, Toast.LENGTH_SHORT).show();
					nightModeOn=false;
				}
				else{
					findViewById(R.id.LinearLayout1).setBackgroundColor(getResources().getColor(R.color.black));
					findViewById(R.id.button1).setBackgroundColor(getResources().getColor(R.color.darkGray));
					((TextView) findViewById(R.id.button1)).setTextColor(getResources().getColor(R.color.mediumGray));
					findViewById(R.id.playButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
					((TextView) findViewById(R.id.playButton)).setTextColor(getResources().getColor(R.color.mediumGray));
					findViewById(R.id.hanziButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
					((TextView) findViewById(R.id.hanziButton)).setTextColor(getResources().getColor(R.color.mediumGray));
					findViewById(R.id.repeatButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
					((TextView) findViewById(R.id.repeatButton)).setTextColor(getResources().getColor(R.color.mediumGray));
					findViewById(R.id.pauseButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
					((TextView) findViewById(R.id.pauseButton)).setTextColor(getResources().getColor(R.color.mediumGray));
					((TextView) findViewById(R.id.timerTextView)).setTextColor(getResources().getColor(R.color.mediumGray));
					((TextView) findViewById(R.id.hanziTextView)).setTextColor(getResources().getColor(R.color.mediumGray));
					((TextView) findViewById(R.id.instructionTextView)).setTextColor(getResources().getColor(R.color.mediumGray));
					Toast.makeText(MainActivity.this, R.string.nightMode_toast, Toast.LENGTH_SHORT).show();
					nightModeOn=true;
				}
			}
		});
		
		clockHandler = new Handler();
		start = System.currentTimeMillis();
		elapsedMillis = 0L;
		clockRunning = false;
		createUpdateTimeTask();
		findViewById(R.id.pauseButton).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						toggleClock();
					}
				});
		if (savedInstanceState != null) {
			elapsedMillis = savedInstanceState.getLong("elapsedMillis");
			Log.d(TAG, "elapsedMillis restored to" + elapsedMillis);
			key = savedInstanceState.getString("key");
			String sampleName = savedInstanceState.getString("sample");
			if (sampleName.length() > 0)
				sample = new File(clipDir, sampleName);
			if (savedInstanceState.getBoolean("running"))
				toggleClock();
			else
				showTime(elapsedMillis);
			Log.d(TAG, "About to restore instruction");
			String instruction = savedInstanceState.getString("instruction");
			if (instruction.length() > 0) {
				Log.d(TAG, "Restoring instruction value of " + instruction);
				TextView t = (TextView) findViewById(R.id.instructionTextView);
				t.setText(instruction);
			}
		}
	}

	
	/*
	 * Gesture begin
	 */
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
		if  (predictions.size() > 0 ) {
			Prediction prediction = predictions.get(0);
			if (prediction.score > 1.0) {
				String s = prediction.name;
				
				if (s.equals("SwipeLeft")) {
					Integer index = rnd.nextInt(cliplist.length);
					sample = new File(clipDir, cliplist[index]);
					key = sample.getName();
					key = key.substring(0, key.length() - 4);
					TextView t = (TextView) findViewById(R.id.instructionTextView);
					t.setText(getInstruction(key));
					
					if (!clockRunning)
						toggleClock();
					if (sample != null) {
						setHanzi("");
						if (mp != null) {
							mp.stop();
							mp.release();
						}
						mp = new MediaPlayer();
						mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
						try {
							mp.setDataSource(getApplicationContext(),
									Uri.fromFile(sample));
							mp.prepare();
							mp.start();
						} catch (Exception e) {
							Log.d(TAG, "Couldn't get mp3 file");
						}
					}
			        Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
				}
				else if (s.equals("NorthLeftCircle") || s.equals("NorthRightCircle") || s.equals("SouthLeftCircle") || s.equals("SouthRightCircle") || s.equals("WestLeftCircle") || s.equals("WestRightCircle") || s.equals("EastLeftCircle") || s.equals("EastRightCircle")) {
					if (!clockRunning)
						toggleClock();
					if (sample != null) {
						setHanzi("");
						if (mp != null) {
							mp.stop();
							mp.release();
						}
						mp = new MediaPlayer();
						mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
						try {
							mp.setDataSource(getApplicationContext(),
									Uri.fromFile(sample));
							mp.prepare();
							mp.start();
						} catch (Exception e) {
							Log.d(TAG, "Couldn't get mp3 file");
						}
					}
					
					Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();
				}
				
			}
		}
	}
	/*
	 * Gestures end
	 */
	
	public void onPause() {
		super.onPause();
		Log.d(TAG, "!!!! onPause is being run");
		clockWasRunning = clockRunning;
		if (clockRunning)
			toggleClock();
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String sampleName = "";
		if (sample != null)
			sampleName = sample.getName();
		outState.putString("sample", sampleName);
		// onPause has stopped the clock if it was running, so we just save
		// elapsedMillis
		outState.putLong("elapsedMillis", elapsedMillis);
		TextView t = (TextView) findViewById(R.id.instructionTextView);
		outState.putString("instruction", t.getText().toString());
		outState.putString("key", key);
		outState.putBoolean("running", clockWasRunning);
	}

	public void reset() {
		TextView t;
		if (clockRunning)
			toggleClock();
		start = 0L;
		elapsedMillis = 0L;
		sample = null;
		t = (TextView) findViewById(R.id.timerTextView);
		t.setText("0:00");
		setHanzi("");
		t = (TextView) findViewById(R.id.instructionTextView);
		t.setText("");
	}

	public void toggleNight() {

	}

	/*
	 * COMMENT OUT
	 */
	/*
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.hanziTextView:
			Toast.makeText(this, "Clip: " + key, Toast.LENGTH_LONG).show();
			Log.d(TAG, "Long clicked");
			break;
		}
		return true;
	}
	*/
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playButton:
			Integer index = rnd.nextInt(cliplist.length);
			sample = new File(clipDir, cliplist[index]);
			key = sample.getName();
			key = key.substring(0, key.length() - 4);
			TextView t = (TextView) findViewById(R.id.instructionTextView);
			t.setText(getInstruction(key));		
		case R.id.repeatButton:
			if (!clockRunning)
				toggleClock();
			if (sample != null) {
				setHanzi("");
				if (mp != null) {
					mp.stop();
					mp.release();
				}
				mp = new MediaPlayer();
				mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					mp.setDataSource(getApplicationContext(),
							Uri.fromFile(sample));
					mp.prepare();
					mp.start();
				} catch (Exception e) {
					Log.d(TAG, "Couldn't get mp3 file");
				}
			}
			break;
		case R.id.hanziButton:
			if (!clockRunning)
				toggleClock();
			if (sample != null)
				setHanzi(hanzi.get(key)); // Should add default value: error
											// message if no hanzi for key
			break;
		case R.id.timerTextView:
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.reset)
					.setMessage(R.string.reallyReset)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									MainActivity.this.reset();
								}
							}).setNegativeButton(R.string.no, null).show();
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d(TAG, "llkj");
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.quit)
					.setMessage(R.string.reallyQuit)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									MainActivity.this.finish();
								}
							}).setNegativeButton(R.string.no, null).show();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
