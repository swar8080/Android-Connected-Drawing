package swar8080.collaborativedrawing.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import swar8080.collaborativedrawing.drawing.ScaledShapeDrawer;

/**
 *
 */

public class ScaledCircleDrawer extends ScaledShapeDrawer {

    private float mRadius;

    public ScaledCircleDrawer(int canvasHeight, int canvasWidth, float radiusScaleFactor) {
        super(canvasHeight, canvasWidth, radiusScaleFactor);
        recalculateRadius(mScaleFactor, mCanvasWidth);
    }

    @Override
    public void drawScaledShapeAt(Canvas canvas, float x, float y, Paint paint) {
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawCircle(x, y, mRadius, paint);
    }

    @Override
    public void setScaleFactor(float scaleFactor) {
        super.setScaleFactor(scaleFactor);
        recalculateRadius(mScaleFactor, mCanvasWidth);
    }

    private void recalculateRadius(float scaleFactor, float canvasWidth){
        mRadius = scaleFactor * canvasWidth;
    }
}
