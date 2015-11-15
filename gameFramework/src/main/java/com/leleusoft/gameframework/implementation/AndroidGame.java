/**
 * Copyright 2013 James Cho
 * Copyright 2015 João Leonardo Pereira

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.leleusoft.gameframework.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.leleusoft.gameframework.Audio;
import com.leleusoft.gameframework.FileIO;
import com.leleusoft.gameframework.Game;
import com.leleusoft.gameframework.Graphics;
import com.leleusoft.gameframework.Input;
import com.leleusoft.gameframework.Screen;

/**
 * Class defining a generic AndroidGame.
 * You only need to set a few static fields in order to configure the game properly
 * @author James Cho
 * @author João Leonardo Pereira
 *
 */
public abstract class AndroidGame extends Activity implements Game {
	
	
	private static final int FRAMEBUFFER_WIDTH = 1280; 
	private static final int FRAMEBUFFER_HEIGHT = 720;
	
	
	private static final String FIRST_TIME_CREATE_KEY = "FIRSTTIMECREATED";
	AndroidFastRenderView renderView;	
	Graphics graphics;
	Audio audio;
	Input input;
	FileIO fileIO;
	protected AndroidScreen screen = null;
	WakeLock wakeLock;
	protected boolean firstTimeCreate = true;
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i("DEBUG", "onRestoreInstanceState");
		boolean resuming = savedInstanceState.getBoolean(FIRST_TIME_CREATE_KEY);
		firstTimeCreate = !resuming;
		if(AndroidScreenRetain.getRetainedScreen()!=null)
		{
			screen = (AndroidScreen) AndroidScreenRetain.getRetainedScreen();
			screen.updateGame(this);
			screen.updateParams();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i("DEBUG", "onSaveInstanceState");
		outState.putBoolean(FIRST_TIME_CREATE_KEY, firstTimeCreate);
		AndroidScreenRetain.retainScreen(screen);
		super.onSaveInstanceState(outState);

	}

	/**
	 * Called by Android when creating the game screen. For further information, see {@link android.app.Activity#onCreate Activity.onCreate(Bundle savedInstanceState)} 
	 * @see Activity
	 */	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);



		Point dimens = getProperDimens(isPortrait);


		int frameBufferWidth = isPortrait ? dimens.y: dimens.x;
		//int frameBufferWidth = 512;
		int frameBufferHeight = isPortrait ? dimens.x: dimens.y;
		//int frameBufferHeight = 512;
		Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
				frameBufferHeight, Config.ARGB_8888);

		float scaleX = (float) frameBufferWidth
				/ getWindowManager().getDefaultDisplay().getWidth();
		float scaleY = (float) frameBufferHeight
				/ getWindowManager().getDefaultDisplay().getHeight();

		renderView = new AndroidFastRenderView(this, frameBuffer);
		graphics = new AndroidGraphics(getAssets(), frameBuffer);
		fileIO = new AndroidFileIO(this);
		audio = new AndroidAudio(this);
		input = new AndroidInput(this, renderView, scaleX, scaleY);
		if(screen == null)
		{
			screen = (AndroidScreen) getInitScreen();
		}

		setContentView(renderView);

		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyGame");
	}

	
	
	
	@Override
	public void onResume() {
		super.onResume();		
		wakeLock.acquire();
		screen.resume();
		renderView.resume();
	}

	@Override
	public void onPause() {
		super.onPause();        
		renderView.pause();
		screen.pause();
		wakeLock.release();

		if (isFinishing())
			screen.dispose();
	}

	@Override
	public Input getInput() {
		return input;
	}

	@Override
	public FileIO getFileIO() {
		return fileIO;
	}

	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	@Override
	public Audio getAudio() {
		return audio;
	}

	@Override
	public void setScreen(Screen screen) {
		if (screen == null)
			throw new IllegalArgumentException("Screen must not be null");

		this.screen.pause();
		this.screen.dispose();
		screen.resume();
		screen.update(0);
		this.screen = (AndroidScreen) screen;
	}

	public Screen getCurrentScreen() {
		return screen;
	}

	private int getNavigationBarHeight(Context context, boolean isPortrait) {
		Resources resources = context.getResources();

		int id = resources.getIdentifier(
				isPortrait ? "navigation_bar_height" : "navigation_bar_height_landscape",
						"dimen", "android");
		Log.i("DEBUG","+id:"+id);

		if (id > 0) {
			Log.i("DEBUG", "barHeight:"+resources.getDimensionPixelSize(id));
			return resources.getDimensionPixelSize(id);
		}
		else{
			id = resources.getIdentifier("navigation_bar_height","dimen", "android");
			return resources.getDimensionPixelSize(id);
		}
		
	}	

	private Point getProperDimens(boolean isPortrait) {
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int largerDimension;  
		int smallerDimension;
		
		smallerDimension = FRAMEBUFFER_HEIGHT;
		if(isPortrait)
		{			
			largerDimension =(int)((float)dm.heightPixels*FRAMEBUFFER_HEIGHT/dm.widthPixels);			
		}
		else
		{
			largerDimension =(int)((float)dm.widthPixels*FRAMEBUFFER_HEIGHT/dm.heightPixels);		
		}

		return new Point(largerDimension, smallerDimension);
	}
}