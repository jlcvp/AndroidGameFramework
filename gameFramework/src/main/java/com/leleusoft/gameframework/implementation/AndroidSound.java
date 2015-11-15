/**
 * Copyright 2013 James Cho
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

import android.media.SoundPool;

import com.leleusoft.gameframework.Sound;

public class AndroidSound implements Sound {
    int soundId;
    int playingId;
    SoundPool soundPool;

    public AndroidSound(SoundPool soundPool, int soundId) {
        this.soundId = soundId;
        this.soundPool = soundPool;
    }

    @Override
    public void play(float volume) {
       playingId= soundPool.play(soundId, volume, volume, 0, 0, 1);

    }

    @Override
    public void dispose() {
        soundPool.unload(soundId);
    }

    
    
	@Override
	public void stop() { 
		soundPool.stop(playingId);
	}

}
