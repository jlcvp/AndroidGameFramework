package com.leleusoft.gameframework.implementation;

import android.util.Log;

import com.leleusoft.gameframework.FPSCounter;

public class AndroidFPSCounter extends FPSCounter {

	@Override
	protected void logOutput() {
		Log.i("FPSCounter", "fps: " + frames);
	}

}
