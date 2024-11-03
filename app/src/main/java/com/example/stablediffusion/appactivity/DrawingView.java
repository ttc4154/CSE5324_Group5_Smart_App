package com.example.stablediffusion.appactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    private Paint drawPaint;
    private Path drawPath;
    private Bitmap bitmap;
    private Canvas canvas;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        drawPaint = new Paint();
        drawPaint.setColor(0xFFFFFFFF); // White color for masking
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(10);
        drawPaint.setAntiAlias(true);
        drawPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                return true;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (canvas == null) {
            canvas = new Canvas(bitmap);
        }
        invalidate();
    }

    public Bitmap getMaskedBitmap() {
        // Create a new bitmap with the original size
        Bitmap maskedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(maskedBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(drawPath, drawPaint); // Draw the mask on the new bitmap
        return maskedBitmap; // Return the masked bitmap
    }

    public void clearDrawing() {
        drawPath.reset();
        invalidate();
    }
}
