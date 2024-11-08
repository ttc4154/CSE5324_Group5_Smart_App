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
    private Canvas canvas; // Canvas to draw on the bitmap
    private OnDrawingCompleteListener listener;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public void onDrawingCompleteListener(OnDrawingCompleteListener listener) {
        this.listener = listener;
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
            canvas.drawBitmap(bitmap, 0, 0, null); // Draw the current bitmap
        }
        canvas.drawPath(drawPath, drawPaint); // Draw the active path
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
                // Draw the path on the bitmap, making it persistent
                canvas.drawPath(drawPath, drawPaint);
                drawPath.reset(); // Reset the path for new strokes
                break;
            default:
                return false;
        }
        invalidate(); // Redraw the view
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Notify completion of drawing
            /*if (onDrawingCompleteListener != null) {
                onDrawingCompleteListener.onDrawingComplete();
            }*/
        }
        return true;
    }

    // Add method to retrieve the drawn bitmap
    public Bitmap getDrawingBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas); // Call draw to capture the drawing on canvas
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true); // Copy the bitmap to make it mutable
        canvas = new Canvas(this.bitmap); // Set the canvas to draw on this bitmap
        invalidate();
    }

    public Bitmap getMaskedBitmap() {
        // Return the bitmap with the mask applied
        return bitmap;
    }
    // Add this interface
    public interface OnDrawingCompleteListener {
        void onDrawingComplete();
    }
    public void clearDrawing() {
        // Clear both the path and the bitmap to start fresh
        if (canvas != null) {
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR); // Clear the bitmap
        }
        drawPath.reset();
        invalidate();
    }
}
