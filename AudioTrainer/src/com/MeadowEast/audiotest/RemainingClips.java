package com.MeadowEast.audiotest;

public class RemainingClips {
	private String mRemainingClips;
	private boolean mClipsUsed;
	
	public RemainingClips() {
		mRemainingClips = null;
		mClipsUsed = false;
	}
	
	public String getRemainingClips() {
		return mRemainingClips;
	}
	public void setRemainingClips(String remainingClips) {
		mRemainingClips = remainingClips;
	}
	public boolean isClipsUsed() {
		return mClipsUsed;
	}
	public void setClipsUsed(boolean clipsUsed) {
		mClipsUsed = clipsUsed;
	}	
	
}
