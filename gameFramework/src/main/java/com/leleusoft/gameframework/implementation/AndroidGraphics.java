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

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

import com.leleusoft.gameframework.Graphics;
import com.leleusoft.gameframework.Image;

public class AndroidGraphics implements Graphics {
	AssetManager assets;
	Bitmap frameBuffer;
	Canvas canvas;
	Paint paint;
	Rect srcRect = new Rect();
	Rect dstRect = new Rect();

	public AndroidGraphics(AssetManager assets, Bitmap frameBuffer) {
		this.assets = assets;
		this.frameBuffer = frameBuffer;
		this.canvas = new Canvas(frameBuffer);
		this.paint = new Paint();
	}

	public Image flipImage(Image src, boolean isFlipVertical)
	{
		Matrix m = new Matrix();

		if(isFlipVertical)
		{
			m.preScale(1, -1);
		}
		else
		{
			m.preScale(-1, 1);
		}

		Bitmap source = ((AndroidImage) src).bitmap;


		Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), m, false);
		//bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

		return new AndroidImage(bitmap, ((AndroidImage) src).format);
	}

	@Override
	public Image newImage(String fileName, ImageFormat format) {
		Config config = null;
		if (format == ImageFormat.RGB565)
			config = Config.RGB_565;
		else if (format == ImageFormat.ARGB4444)
			config = Config.ARGB_4444;
		else
			config = Config.ARGB_8888;

		Options options = new Options();
		options.inPreferredConfig = config;


		InputStream in = null;
		Bitmap bitmap = null;
		try {
			in = assets.open(fileName);
			bitmap = BitmapFactory.decodeStream(in, null, options);
			if (bitmap == null)
				throw new RuntimeException("Couldn't load bitmap from asset '"
						+ fileName + "'");
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load bitmap from asset '"
					+ fileName + "'");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}

		if (bitmap.getConfig() == Config.RGB_565)
			format = ImageFormat.RGB565;
		else if (bitmap.getConfig() == Config.ARGB_4444)
			format = ImageFormat.ARGB4444;
		else
			format = ImageFormat.ARGB8888;

		return new AndroidImage(bitmap, format);
	}

	@Override
	public void clearScreen(int color) {
		canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8,
				(color & 0xff));
	}


	@Override
	public void drawLine(int x, int y, int x2, int y2, int color) {
		paint.setColor(color);
		canvas.drawLine(x, y, x2, y2, paint);
	}

	@Override
	public void drawRect(int x, int y, int width, int height, int color) {
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
	}

	public void drawRect(int x, int y, int width, int height, int color,Paint paint){
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
	}

	@Override
	public void drawARGB(int a, int r, int g, int b) {
		paint.setStyle(Style.FILL);
		canvas.drawARGB(a, r, g, b);
	}

	@Override
	public void drawString(String text, int x, int y, Paint paint){
		canvas.drawText(text, x, y, paint);    	
	}

	public void drawBorderedText(String text,int x, int y, Paint paint, int strokeColor, int strokeWidth)
	{
		Paint stroke = new Paint(paint);
		stroke.setStyle(Style.STROKE);
		stroke.setStrokeWidth(strokeWidth);
		stroke.setColor(strokeColor);

		canvas.drawText(text, x, y, paint);
		canvas.drawText(text, x, y, stroke);    	
	}


	public void drawImage(Image Image, int x, int y, int srcX, int srcY,
			int srcWidth, int srcHeight) {
		srcRect.left = srcX;
		srcRect.top = srcY;
		srcRect.right = srcX + srcWidth;
		srcRect.bottom = srcY + srcHeight;


		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + srcWidth;
		dstRect.bottom = y + srcHeight;

		canvas.drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect,
				null);
	}

	@Override
	public void drawImage(Image Image, int x, int y) {
		canvas.drawBitmap(((AndroidImage)Image).bitmap, x, y, null);
	}

	public void drawImage(Image Image, int x, int y, int alpha)
	{
		Paint p = new Paint(paint);
		if(alpha>0 && alpha <=255)
			p.setAlpha(alpha);

		canvas.drawBitmap(((AndroidImage)Image).bitmap, x, y, p);
	}

	public void drawScaledImage(Image Image, int x, int y, int width, int height, int srcX, int srcY, int srcWidth, int srcHeight){


		srcRect.left = srcX;
		srcRect.top = srcY;
		srcRect.right = srcX + srcWidth;
		srcRect.bottom = srcY + srcHeight;


		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + width;
		dstRect.bottom = y + height;



		canvas.drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect, null);

	}

	public void drawScaledImage(Image Image, int x, int y, int width, int height){


		srcRect.left = 0;
		srcRect.top = 0;
		srcRect.right = Image.getWidth();
		srcRect.bottom = Image.getHeight();


		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + width;
		dstRect.bottom = y + height;



		canvas.drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect, null);

	}
	
	public void drawScaledImage(Image Image, int x, int y, int width, int height, int alpha){


		srcRect.left = 0;
		srcRect.top = 0;
		srcRect.right = Image.getWidth();
		srcRect.bottom = Image.getHeight();


		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + width;
		dstRect.bottom = y + height;


		Paint p = new Paint(paint);
		if(alpha>0 && alpha <=255)
			p.setAlpha(alpha);
		canvas.drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect, p);

	}

	@Override
	public int getWidth() {
		return frameBuffer.getWidth();
	}

	@Override
	public int getHeight() {
		return frameBuffer.getHeight();
	}
}
