/**
 * Copyright 2013 James Cho
 * Copyright 2015 Leonardo Pereira
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

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.leleusoft.gameframework.Input.TouchEvent;
import com.leleusoft.gameframework.Pool;
import com.leleusoft.gameframework.Pool.PoolObjectFactory;

public class SingleTouchHandler implements TouchHandler{
	private static final int HOLD_DETECTION_TOLERANCE_DISTANCE = 20;
	private static final int HOLD_THRESHOLD = ViewConfiguration.getLongPressTimeout();
	boolean isTouched;
	int touchX;
	int touchY;
	Pool<TouchEvent> touchEventPool;
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	List<TouchEvent> touchEventsBuffer = new ArrayList<TouchEvent>();
	float scaleX;
	float scaleY;
	int lastXDown,lastYDown;
	Handler mHandler;
	Runnable runnable = new Runnable() {

		@Override
		public void run() {			
			onLongClick();
		}
	};

	public SingleTouchHandler(View view, float scaleX, float scaleY) {
		PoolObjectFactory<TouchEvent> factory = new PoolObjectFactory<TouchEvent>() {
			@Override
			public TouchEvent createObject() {
				return new TouchEvent();
			}            
		};
		touchEventPool = new Pool<TouchEvent>(factory, 100);
		view.setOnTouchListener(this);      

		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		synchronized(this) {
			TouchEvent touchEvent = touchEventPool.newObject();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touchEvent.type = TouchEvent.TOUCH_DOWN;
				isTouched = true;
				lastXDown=(int)(event.getX() * scaleX);
				lastYDown=(int) (event.getY() * scaleY);
				mHandler=new Handler();
				mHandler.postDelayed(runnable, HOLD_THRESHOLD);
				break;
			case MotionEvent.ACTION_MOVE:
				touchEvent.type = TouchEvent.TOUCH_DRAGGED;
				if(((int)Math.sqrt((touchEvent.x-lastXDown)*(touchEvent.x-lastXDown) + 
						(touchEvent.y-lastYDown)*(touchEvent.y-lastYDown)))>HOLD_DETECTION_TOLERANCE_DISTANCE)
				{
					isTouched = true;
					lastXDown=-1;
				}
				break;
			case MotionEvent.ACTION_CANCEL:                
			case MotionEvent.ACTION_UP:
				touchEvent.type = TouchEvent.TOUCH_UP;
				isTouched = false;
				lastXDown=-1;
				break;
			}

			touchEvent.x = touchX = (int)(event.getX() * scaleX);
			touchEvent.y = touchY = (int)(event.getY() * scaleY);
			touchEventsBuffer.add(touchEvent);                        

			return true;
		}
	}


	public boolean onLongClick() {
		synchronized(this) {
			TouchEvent touchEvent = touchEventPool.newObject();
			if(lastXDown!=-1) //legitimate hold event
			{
				touchEvent.x = lastXDown;
				touchEvent.y = lastYDown;
				touchEvent.type = TouchEvent.TOUCH_HOLD;
				touchEventsBuffer.add(touchEvent);
			}
			return true;
		}
	}


	@Override
	public boolean isTouchDown(int pointer) {
		synchronized(this) {
			if(pointer == 0)
				return isTouched;
			else
				return false;
		}
	}

	@Override
	public int getTouchX(int pointer) {
		synchronized(this) {
			return touchX;
		}
	}

	@Override
	public int getTouchY(int pointer) {
		synchronized(this) {
			return touchY;
		}
	}

	@Override
	public List<TouchEvent> getTouchEvents() {
		synchronized(this) {     
			int len = touchEvents.size();
			for( int i = 0; i < len; i++ )
				touchEventPool.free(touchEvents.get(i));
			touchEvents.clear();
			touchEvents.addAll(touchEventsBuffer);
			touchEventsBuffer.clear();
			return touchEvents;
		}
	}
}
