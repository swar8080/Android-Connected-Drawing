package swar8080.collaborativedrawing;

import android.util.Pair;

/**
 * Created by Steven on 2017-02-23.
 */

public class DrawScalingUtil {

    public static Pair<Float,Float>[] getRelativePointLocations(Pair<Float,Float>[] scaledPoints, float drawingAreaHeight
            ,float drawingAreaWidth)
    {
        Pair<Float,Float>[] relativePointsDrawnAt = new Pair[scaledPoints.length];

        for (int i=0; i<scaledPoints.length; i++){
            relativePointsDrawnAt[i] = new Pair<Float, Float>(
                    scaledPoints[i].first/drawingAreaWidth,
                    scaledPoints[i].second/drawingAreaHeight);
        }

        return relativePointsDrawnAt;
    }


    public static Pair<Float,Float>[] scalePointsToScreenSize(Pair<Float,Float>[] relativePoints,
                                                              float drawingAreaHeight,
                                                              float drawingAreaWidth){
        int pointCount = relativePoints.length;
        float scaledX, scaledY;

        Pair<Float,Float>[] scaledPointPairs = new Pair[pointCount];

        for (int i = 0; i < pointCount; i++){
            scaledX = relativePoints[i].first * drawingAreaWidth;
            scaledY = relativePoints[i].second * drawingAreaHeight;

            scaledPointPairs[i] = new Pair<Float,Float>(scaledX, scaledY);
        }

        return scaledPointPairs;
    }
}
