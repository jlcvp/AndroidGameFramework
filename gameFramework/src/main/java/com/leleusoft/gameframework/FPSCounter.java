package com.leleusoft.gameframework;

public abstract class FPSCounter {

	long startTime = System.nanoTime();
	protected int frames = 0;

	public void logFrame() {
		frames++;
		if(System.nanoTime() - startTime >= 1000000000) {
			logOutput();
			frames = 0;
			startTime = System.nanoTime();
		}
	}

	protected abstract void logOutput();

}
