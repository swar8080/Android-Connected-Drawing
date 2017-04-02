package swar8080.collaborativedrawing.MessageTranslatorTests;

import swar8080.collaborativedrawing.message.DecodedMessageHandler;
import swar8080.collaborativedrawing.drawing.DrawingAction;

/**
 *
 */

public class TestableDecodedMessageHandler implements DecodedMessageHandler {

    boolean resetCallbackMade;
    boolean drawCallbackMade;
    DrawingAction[] lastDrawingAction;

    @Override
    public void onDrawMessageReceived(DrawingAction[] drawingAction) {
        drawCallbackMade = true;
        lastDrawingAction = drawingAction;
    }

    @Override
    public void onResetMessageReceived() {
        resetCallbackMade = true;
    }
}
