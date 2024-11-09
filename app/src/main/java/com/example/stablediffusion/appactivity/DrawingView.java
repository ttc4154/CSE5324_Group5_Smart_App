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
    private Canvas bitmapCanvas;
    private OnDrawingCompleteListener listener;
    private float brushSize = 10f; // Default brush size

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnDrawingCompleteListener(OnDrawingCompleteListener listener) {
        this.listener = listener;
    }

    private void init() {
        drawPaint = new Paint();
        drawPaint.setColor(0xFFFFFFFF);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(10);
        drawPaint.setAntiAlias(true);
        drawPath = new Path();
    }

    // Method to set the brush size
    public void setBrushSize(float size) {
        brushSize = size;
        drawPaint.setStrokeWidth(brushSize);
        invalidate(); // Refresh the view with the new brush size
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
            bitmapCanvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        }
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
                bitmapCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                if (listener != null) {
                    listener.onDrawingComplete();
                }
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    // Method to retrieve the masked bitmap
    public Bitmap getMaskedBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap newBitmap) {
        if (newBitmap != null) {
            bitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true);
            bitmapCanvas = new Canvas(bitmap);
            invalidate();
        }
    }

    public void clearDrawing() {
        if (bitmapCanvas != null) {
            bitmapCanvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        }
        drawPath.reset();
        invalidate();
    }

    public interface OnDrawingCompleteListener {
        void onDrawingComplete();
    }
    public Bitmap getDrawingBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas); // Call draw to capture the drawing on canvas
        return bitmap;
    }
}
