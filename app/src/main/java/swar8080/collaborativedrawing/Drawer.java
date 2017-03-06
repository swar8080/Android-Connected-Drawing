package swar8080.collaborativedrawing;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Steven on 2017-03-06.
 */

public interface Drawer {
    void drawIntoCanvas(Canvas canvas, float x, float y);
    Paint getPaint();
}
