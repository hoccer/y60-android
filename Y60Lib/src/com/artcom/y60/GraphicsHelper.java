package com.artcom.y60;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class GraphicsHelper {

    public static ImageView scaleView(View pView, float pScaleFactor, Context pContext) {

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap screenshot = Bitmap.createBitmap(pView.getWidth(), pView.getHeight(), config);
        Canvas drawingArea = new Canvas(screenshot);
        pView.draw(drawingArea);
        return scaleBitmap(screenshot, pScaleFactor, pContext);
    }

    public static ImageView scaleBitmap(Bitmap pSource, float pScaleFactor, Context pContext) {

        int width = pSource.getWidth();
        int height = pSource.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(pScaleFactor, pScaleFactor);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(pSource, 0, 0, width, height, matrix, true);
        Drawable scaledDrawable = new BitmapDrawable(resizedBitmap);

        ImageView scaledImageView = new ImageView(pContext);
        scaledImageView.setImageDrawable(scaledDrawable);

        return scaledImageView;
    }

    public static Bitmap rotateBitmap(Bitmap pSrc) {
        int width = pSrc.getWidth();
        int height = pSrc.getHeight();
        Bitmap dst = Bitmap.createBitmap(height, width, pSrc.getConfig());
        int[] pixels = new int[height];
        for (int j = 0; j < height; ++j) {
            pSrc.getPixels(pixels, 0, width, 0, j, width, 1);
            dst.setPixels(pixels, 0, 1, height - 1 - j, 0, 1, width);
        }
        return dst;
    }
}
