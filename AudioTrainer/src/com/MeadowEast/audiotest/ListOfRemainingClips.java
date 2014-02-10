package com.MeadowEast.audiotest;

public class ListOfRemainingClips {
	private RemainingClips[] mRemainingClipsArr;
	
	public ListOfRemainingClips(){
		
	}
	
	public ListOfRemainingClips(int mSize){
		mRemainingClipsArr = new RemainingClips[mSize];
		setInitArr(mSize);
	}
	
	private void setInitArr(int mSize) {
		for (int i = 0; i < mSize; i++ ){
			mRemainingClipsArr[i] =  new RemainingClips();
		}
	}
	
	public String getStringInArr(int mIndex) {
		return mRemainingClipsArr[mIndex].getRemainingClips();
	}
	
	public void setStringInArr(int mIndex, String mString){
		mRemainingClipsArr[mIndex].setRemainingClips(mString);
	}
	
	public boolean getBoolInArr(int mIndex) {
		return mRemainingClipsArr[mIndex].isClipsUsed();
	}
	
	public void setBoolInArr(int mIndex, boolean mBool){
		mRemainingClipsArr[mIndex].setClipsUsed(mBool);
	}
	
	public int returnSizeOfArr() {
		return mRemainingClipsArr.length;
	}
}
