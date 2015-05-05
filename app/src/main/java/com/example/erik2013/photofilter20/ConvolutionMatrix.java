package com.example.erik2013.photofilter20;

/**
 * Created by Erik2013 on 2015-05-03.
 */
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

public class ConvolutionMatrix
{

    private static final int MATRIX_SIZE = 3;

    private static int cap(int color)
    {
        if (color > 255)
            return 255;
        else if (color < 0)
            return 0;
        else
            return color;
    }

    public static Bitmap convolute(Bitmap bmp, Matrix mat, float factor, int offset)
    {

        // get matrix values
        float [] mxv = new float[MATRIX_SIZE * MATRIX_SIZE];
        mat.getValues(mxv);

        // cache source pixels
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int [] scrPxs = new int[width * height];
        bmp.getPixels(scrPxs, 0, width, 0, 0, width, height);

        // clone source pixels in an array
        // here we’ll store results
        int [] rtPxs = scrPxs.clone();
        int r, g, b;
        int rSum, gSum, bSum;

        int idx;	// current pixel index
        int pix;	// current pixel
        float mv;	// current matrix value

        for(int x = 0, w = width - MATRIX_SIZE + 1; x < w; ++x)
        {
            for(int y = 0, h = height - MATRIX_SIZE + 1; y < h; ++y)
            {
                idx = (x + 1) + (y + 1) * width;
                rSum = gSum = bSum = 0;
                for(int mx = 0; mx < MATRIX_SIZE; ++mx)
                {
                    for(int my = 0; my < MATRIX_SIZE; ++my)
                    {
                        pix = scrPxs[(x + mx) + (y + my) * width];
                        mv = mxv[mx + my * MATRIX_SIZE];
                        rSum += (Color.red(pix) * mv);
                        gSum += (Color.green(pix) * mv);
                        bSum += (Color.blue(pix) * mv);
                    }
                }
                r = cap((int)(rSum / factor + offset));
                g = cap((int)(gSum / factor + offset));
                b = cap((int)(bSum / factor + offset));

                // store computed pixel
                rtPxs[idx] = Color.argb(Color.alpha(scrPxs[idx]), r, g, b);
            }
        }

        // return bitmap with transformed pixels
        return Bitmap.createBitmap(rtPxs, width, height, bmp.getConfig());
    }


    public static Bitmap pixelate(Bitmap bitmap)
    {
        Bitmap tempBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getHeight()/4, bitmap.getWidth()/4, false);
        Bitmap returnBitmap = Bitmap.createScaledBitmap(tempBitmap, bitmap.getHeight(), bitmap.getWidth(), false);

        return returnBitmap;
    }
}
