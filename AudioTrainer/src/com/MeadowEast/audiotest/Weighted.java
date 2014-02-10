package com.MeadowEast.audiotest;

import android.util.Log;

public class Weighted {
	private HitRepeat[] weightOfClips;
	private int sumOfWeights;
	//private int size;

	private static final String WEIGHT_TAG = "weight";
	/*
	public void setSize(int s) {
		size = s;
	}
	*/
	public int getSize() {
		return weightOfClips.length;
	}
	
	public Weighted () {
	}
	
	public Weighted (int size) {
		//setSize(size);
		weightOfClips = new HitRepeat[size];
		setInitArr(size);
		sumOfWeights = 0;
	}

	public void updateTimesClipsPlayed(int mIndex) {
		weightOfClips[mIndex].setTimesClipsPlayed(weightOfClips[mIndex].getTimesClipsPlayed() + 1);
	}
	public int getClipsWeight(int index) {
		return weightOfClips[index].getWeight();
	}
	
	private void setInitArr(int mSize) {
		for (int i = 0; i < mSize; i++) {
			//weightOfClips[i] = new HitRepeat();
			createNewIndex(i);
		}
	}
	
	public void createNewIndex(int mIndex) {
		weightOfClips[mIndex] = new HitRepeat();
	}

	public void doubleProb(int index) {
		weightOfClips[index].setTotNumOfRepeat();
		if (weightOfClips[index].getTotNumOfRepeat() >= 4) {
			weightOfClips[index].setWeight(weightOfClips[index].getWeight() * 2);
		}
		/*
		else {
			weightOfClips[index].setWeight(1);
		}*/
	}

	public void setSumOfWeights() {
		sumOfWeights = 0;
		
		for (int i = 0; i < weightOfClips.length; i++) {
			Log.d(WEIGHT_TAG, "weightclips[" + i + "]: " + weightOfClips[i].getWeight());
			sumOfWeights = sumOfWeights + weightOfClips[i].getWeight();
		}
		Log.d (WEIGHT_TAG, "After loop sumOfWeights: " + sumOfWeights);
	}
	public int getSumOfWeights() {
		//setSumOfWeights();
		return sumOfWeights;
	}
	
	private void updateRepeatArr(int counter, int index){
		weightOfClips[index].setRepeatArr(counter);
	}
	
	public void setProbabilities(int counter, int index, int totalClipsPlayed) {
		if (totalClipsPlayed > 0) {
			updateRepeatArr(counter, index);
			if (weightOfClips[index].getTimesClipsPlayed() >= 3) {
				doubleProb(index);
			}
		}
		setSumOfWeights();
	}
}

