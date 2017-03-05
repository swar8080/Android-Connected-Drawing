package swar8080.collaborativedrawing;

import android.util.Pair;

/**
 * Created by Steven on 2017-02-23.
 */

public class DrawMessage {
    private int mColour;
    private float mRelativeBrushSize;
    private Pair<Float,Float>[] mRelativePointsDrawn;

    DrawMessage(int colour, float relativeBrushSize, Pair<Float,Float>[] relativePointsDrawn){
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
