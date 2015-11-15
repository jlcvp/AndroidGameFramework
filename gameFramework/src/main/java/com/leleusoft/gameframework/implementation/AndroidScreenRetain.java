package com.leleusoft.gameframework.implementation;

import com.leleusoft.gameframework.Screen;

public class AndroidScreenRetain {
	static Screen retainedScreen = null;	
	
	public static void retainScreen(Screen screen)
	{
		retainedScreen = screen;
	}
	
	public static Screen getRetainedScreen()
	{
		return retainedScreen;
	}
}
