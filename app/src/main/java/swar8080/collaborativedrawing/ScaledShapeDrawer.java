package swar8080.collaborativedrawing;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Steven on 2017-02-26.
 */

public abstract class ScaledShapeDrawer {

    protected int mCanvasHeight, mCanvasWidth;
    protected float scaleFactor;

    public ScaledShapeDrawer(int canvasHeight, int canvasWidth, float scaleFactor){
        this.mCanvasHeight = canvasHeight;
        this.mCanvasWidth = canvasWidth;
        this.scaleFactor = scaleFactor;
    }

    public abstract void drawScaledShapeAt(Canvas canvas, float x, float y, Paint paint);

    public void setScaleFactor(float scaleFactor){
        this.scaleFactor = scaleFactor;
    }

    public float getScaleFactor(){
        return scaleFactor;
    }
}
