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

import android.util.Log;

import com.leleusoft.gameframework.Game;
import com.leleusoft.gameframework.Screen;

public abstract class AndroidScreen extends Screen {
	protected static int FRAMES_PER_SECOND = 30;
    private static int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
    private long nextTick; //variavel usada pra controle e sync de FPS
    private int syncErrorCount =0;
    private long errorThreshold =0;
	public AndroidScreen(Game game) {
		super(game);
		nextTick = 0;
	}
	
	/**
	 * Called to inform that the game field needs to be updated
	 * @param game the new instance of AndroidGame (Commonly related to activity)
	 */
	public void updateGame(AndroidGame game)
	{
		this.game = game;
	}
	/**
	 * YOU MUST CALL THIS METHOD AT THE END OF YOUR 
	 * SUBCLASS' update METHOD TO SYNC FPS IN YOUR SCREEN
	 */
	@Override
	public void update(float deltaTime) { 
		
		
		nextTick += (long) deltaTime;
		if(nextTick>=SKIP_TICKS){   
			long sleepTime = 2*SKIP_TICKS - nextTick;
			nextTick = 0;
			errorThreshold+=deltaTime;
			try {
				if(sleepTime>0){
					Thread.sleep(sleepTime);
				}
				else
				{
					syncErrorCount++;					
					if(syncErrorCount>50 && errorThreshold<=10000){
						Log.e("DEBUG", "System constantly out of sync consider lowering the desired FPS");
						errorThreshold=0;
						syncErrorCount=0;
					}
					
					if(errorThreshold > 10000)
					{
						errorThreshold=0;
						syncErrorCount=0;
						//TODO Your may want to inform some synchronization errors here
					}
				}
			} catch (InterruptedException e) {				
				Log.e("ERROR","ERROR SYNCING FRAMERATE");
			}
		}		
	}
	
	/**
	 * Called to inform screen that game variable was changed (call after updateGame)
	 */
	public abstract void updateParams();
}
