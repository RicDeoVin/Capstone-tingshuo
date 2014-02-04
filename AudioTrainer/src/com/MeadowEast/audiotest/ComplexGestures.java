package com.MeadowEast.audiotest;

import java.util.ArrayList;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ComplexGestures extends Activity implements OnGesturePerformedListener {
	private GestureLibrary gestureLib;
	private static final String TAG = "complex gestures";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.activity_main, null);
		gestureOverlayView.addView(inflate);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureLib = GestureLibraries.fromRawResource(this,R.raw.gestures);
		if (!gestureLib.load()) {
			//finish();
			Log.i(TAG, "FAILED");
		}
		else {
			Log.i(TAG, "LOADED");
		}
		setContentView(gestureOverlayView);
	}
	
	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
		for (Prediction prediction : predictions) {
			if (prediction.score > 1.0) {
				/*
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
				*/
				Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
