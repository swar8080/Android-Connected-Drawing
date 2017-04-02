package swar8080.collaborativedrawing.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 *
 */

public class DrawingBrush implements Drawer {

    private Paint mPaint;
    private ScaledShapeDrawer mShapeDrawer;

    public DrawingBrush(Paint paint, ScaledShapeDrawer shapeDrawer){
        mPaint = paint;
        mShapeDrawer = shapeDrawer;
    }


    public void drawIntoCanvas(Canvas canvas, float x, float y){
        mShapeDrawer.drawScaledShapeAt(canvas, x, y, mPaint);
    }

    public int getPaintColour(){ return mPaint.getColor(); }
    public void setPaintColour(int colour){ mPaint.setColor(colour); }
    public void setPaint(Paint paint){ this.mPaint = paint;}
    public Paint getPaint(){ return mPaint; }

    public float getScaledShapeScaleFactor(){ return mShapeDrawer.getScaleFactor();}
    public void setScaledShapeScaleFactor(float factor){
        mShapeDrawer.setScaleFactor(factor);
    }

    public void setScaledShapeDrawer(ScaledShapeDrawer shapeDrawer){mShapeDrawer = shapeDrawer; }

}
