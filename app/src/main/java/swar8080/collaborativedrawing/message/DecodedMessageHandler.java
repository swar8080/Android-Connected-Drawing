package swar8080.collaborativedrawing.message;

import swar8080.collaborativedrawing.drawing.DrawingAction;

/**
 *
 */

public interface DecodedMessageHandler {
    void onDrawMessageReceived(DrawingAction[] drawingAction);
    void onResetMessageReceived();
}
