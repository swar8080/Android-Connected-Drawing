package swar8080.collaborativedrawing.drawing;

import android.support.v4.util.Pair;

/**
 *
 */

public class DrawingAction {
    private int mColour;
    private float mRelativeBrushSize;
    private Pair<Float,Float>[] mRelativePointsDrawn;

    public DrawingAction(int colour, float relativeBrushSize, Pair<Float,Float>[] relativePointsDrawn){
        this.mColour = colour;
        this.mRelativeBrushSize = relativeBrushSize;
        this.mRelativePointsDrawn = relativePointsDrawn;
    }

    public int getDrawColour() {
        return mColour;
    }

    public float getRelativeBrushSize() {
        return mRelativeBrushSize;
    }

    public Pair<Float, Float>[] getRelativePointsDrawn() {
        return mRelativePointsDrawn;
    }
}
