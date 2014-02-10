package com.MeadowEast.audiotest;

import android.util.Log;

public class HitRepeat {
	private int[] repeat;
	private int totNumOfRepeat;
	private int timesClipsPlayed;
	private int weight;
	private static final String REPEAT_TAG = "REPEAT";
	
	public int getWeight() {
		Log.d(REPEAT_TAG, "weight: " + weight);
		return weight;
	}

	public void setWeight(int mWeight) {
		weight = mWeight;
	}

	public HitRepeat() {
		repeat = new int[3];
		//setInitRepeat();
		timesClipsPlayed = 0;
		weight = 1;
	}
	
	public int getTotNumOfRepeat() {
		return totNumOfRepeat;
	}
	public void setTotNumOfRepeat() {
		totNumOfRepeat = repeat[0] + repeat[1] + repeat[2];
	}
	public int getTimesClipsPlayed() {
		return timesClipsPlayed;
	}
	public void setTimesClipsPlayed(int mTimesClipsPlayed) {
		timesClipsPlayed = mTimesClipsPlayed;
	}
	public void setRepeatArr(int mCounter) {
		int index;
		index = (timesClipsPlayed - 1) % 3;
		repeat[index] = mCounter;
	}

}
