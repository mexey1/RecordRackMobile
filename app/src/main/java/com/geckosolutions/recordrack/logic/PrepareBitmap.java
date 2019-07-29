package com.geckosolutions.recordrack.logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.geckosolutions.recordrack.R;

/**
 * Created by anthony1 on 1/7/16.
 */
public class PrepareBitmap
{
    public static Bitmap drawCircularImage(int width, int height)
    {
        Bitmap image = null;
        Bitmap defaultImage = BitmapFactory.decodeResource(UtilityClass.getContext().getResources(), R.drawable.user);
        Bitmap scaledImage = Bitmap.createScaledBitmap(defaultImage, width, height, false);
        image = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        defaultImage.recycle();

        Canvas canvas = new Canvas(image);
        Rect rect = new Rect(0,0,width,height);
        Rect dstRect = new Rect(0,0,width-10,height-10);

        canvas.drawARGB(0, 0, 0, 0); //transparent
        Paint paint = new Paint();
        paint.setColor(UtilityClass.getContext().getResources().getColor(R.color.battleship_grey));
        paint.setDither(true);
        paint.setAntiAlias(true);
        canvas.drawCircle(width / 2, height / 2, width / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledImage, rect, rect, paint);
        scaledImage.recycle();
        return image;
    }
}
