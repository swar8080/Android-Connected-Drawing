package swar8080.collaborativedrawing.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 *
 */

public abstract class ScaledShapeDrawer {

    protected int mCanvasHeight, mCanvasWidth;
    protected float mScaleFactor;

    public ScaledShapeDrawer(int canvasHeight, int canvasWidth, float scaleFactor){
        this.mCanvasHeight = canvasHeight;
        this.mCanvasWidth = canvasWidth;
        this.mScaleFactor = scaleFactor;
    }

    public abstract void drawScaledShapeAt(Canvas canvas, float x, float y, Paint paint);

    public void setScaleFactor(float scaleFactor){
        this.mScaleFactor = scaleFactor;
    }

    public float getScaleFactor(){
        return mScaleFactor;
    }
}
