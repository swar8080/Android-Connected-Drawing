package swar8080.collaborativedrawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Steven on 2017-02-20.
 */

public class DrawingView extends View {

    private int mWidth, mHeight;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private onUserDrawEventListener mDrawEventListener;

    public DrawingView(Context context) {
        super(context);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface onUserDrawEventListener {
        void onUserDrawAt(Pair<Float, Float>[] pointsDrawnAt);
    }

    public void registerOnDrawEventListener(onUserDrawEventListener listener){
        this.mDrawEventListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //cache the width/height values
        mWidth = w;
        mHeight = h;

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Pair<Float,Float>[] pointsTouched;
        Log.d("drawing",event.toString());
        int action = event.getAction();
        if (MotionEvent.ACTION_DOWN == action
                || MotionEvent.ACTION_MOVE == action){

            pointsTouched = new Pair[1 + event.getHistorySize()];
            pointsTouched[0] = new Pair<>(event.getX(), event.getY());

            for(int move = 0; move < event.getHistorySize(); move++){
                pointsTouched[move+1] = new Pair<>(event.getHistoricalX(move), event.getHistoricalY(move));
            }


            if (mDrawEventListener != null)
                mDrawEventListener.onUserDrawAt(pointsTouched);
        }


        return true;
    }

    public void drawAt(Drawer drawer, float x, float y, boolean redraw){
        drawer.drawIntoCanvas(mCanvas, x, y);
        if (redraw)
            invalidate();

    }

    public void drawBulkAt(Drawer drawer, Pair<Float,Float>[] points, boolean redrawAtEnd){
        for (Pair<Float,Float> pair : points)
            drawAt(drawer, pair.first, pair.second, false);

        if (redrawAtEnd)
            invalidate();
    }

    public void reset(){
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        invalidate();
    }

}
