package swar8080.collaborativedrawing;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Steven on 2017-02-26.
 */

public class ScaledCircleDrawer extends ScaledShapeDrawer {

    public ScaledCircleDrawer(int canvasHeight, int canvasWidth, float radiusScaleFactor) {
        super(canvasHeight, canvasWidth, radiusScaleFactor);
    }

    @Override
    public void drawScaledShapeAt(Canvas canvas, float x, float y, Paint paint) {
        canvas.drawCircle(x, y, radius(), paint);
    }

    private float radius(){
        return scaleFactor*mCanvasWidth;
    }
}
