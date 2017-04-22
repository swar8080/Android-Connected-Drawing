package swar8080.collaborativedrawing.MessageTranslatorTests;

import android.graphics.Color;
import android.support.v4.util.Pair;

import com.google.android.gms.nearby.connection.Connections;

import org.junit.*;

import swar8080.collaborativedrawing.drawing.DrawingAction;
import swar8080.collaborativedrawing.message.EncodedMessage;
import swar8080.collaborativedrawing.message.MessageStatus;
import swar8080.collaborativedrawing.message.DrawingMessagesTranslator;
import swar8080.collaborativedrawing.message.MessageType;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 *
 */

public class DrawMessageTranslatorTest {

    private final int BRUSH_COLOR = Color.YELLOW;
    private final float RELATIVE_BRUSH_SIZE = (float)0.5;
    private final int MAX_BYTES_PER_MESSAGE = Connections.MAX_RELIABLE_MESSAGE_LEN;
    private final String SENDER_STRING = "senderID";
    private final Pair<Float,Float>[] TWO_PAYLOAD_POINTS = new Pair[] {p(1,1), p(2,2)};


    EncodedMessage singlePayloadDrawMessage, twoPayloadDrawMessage, threePayloadDrawMessage;

    @Before
    public void setUp(){
        Pair<Float,Float>[] points;

        points = new Pair[] {p(1,1)};
        singlePayloadDrawMessage = DrawingMessagesTranslator.encodeDrawMessages(BRUSH_COLOR, RELATIVE_BRUSH_SIZE,
            points,
            MAX_BYTES_PER_MESSAGE
        );

        int messageSizeToHaveSinglePointPerMessage = singlePayloadDrawMessage.getMessage()[0].length;

        twoPayloadDrawMessage = DrawingMessagesTranslator.encodeDrawMessages(BRUSH_COLOR, RELATIVE_BRUSH_SIZE,
                TWO_PAYLOAD_POINTS,
                messageSizeToHaveSinglePointPerMessage
        );
        if (twoPayloadDrawMessage.getMessage().length != 2)
            fail("Error creating two payload draw message");

        points = new Pair[] {p(1,1), p(2,2), p(3,3)};
        threePayloadDrawMessage = DrawingMessagesTranslator.encodeDrawMessages(BRUSH_COLOR, RELATIVE_BRUSH_SIZE,
                points,
                messageSizeToHaveSinglePointPerMessage
        );

        if (threePayloadDrawMessage.getMessage().length != 3)
            fail("Error creating three payload draw message");

    }

    private static Pair<Float,Float> p(double x, double y){
        return new Pair<Float,Float>((float)x, (float)y);
    }

    @org.junit.Test
    public void encodeSinglePayloadDrawMessageHeader()  {
        Assert.assertTrue("Single payload draw message type must be DRAW_EVENT",
                DrawingMessagesTranslatorTest.isMessageTypeAtPayload(MessageType.DRAW_EVENT, singlePayloadDrawMessage, 0));
        Assert.assertTrue("Single payload draw message status must be DONE",
                DrawingMessagesTranslatorTest.isDrawMessageStatusAtPayload(MessageStatus.DONE, singlePayloadDrawMessage, 0));
    }

    @org.junit.Test
    public void encodeTwoPointDrawMessageHeader()  {
        Assert.assertTrue("Double payload draw message's first type must be DRAW_EVENT",
                DrawingMessagesTranslatorTest.isMessageTypeAtPayload(MessageType.DRAW_EVENT, twoPayloadDrawMessage, 0));
        Assert.assertTrue("Double payload draw message's first status must be START",
                DrawingMessagesTranslatorTest.isDrawMessageStatusAtPayload(MessageStatus.START, twoPayloadDrawMessage, 0));

        Assert.assertTrue("Double payload draw message's second type must be DRAW_EVENT",
                DrawingMessagesTranslatorTest.isMessageTypeAtPayload(MessageType.DRAW_EVENT, twoPayloadDrawMessage, 1));
        Assert.assertTrue("Double payload draw message's second status must be DONE",
                DrawingMessagesTranslatorTest.isDrawMessageStatusAtPayload(MessageStatus.DONE, twoPayloadDrawMessage, 1));
    }

    @org.junit.Test
    public void encodeThreePayloadDrawMessageHeader()  {
        Assert.assertTrue("Triple payload draw message's first type must be DRAW_EVENT",
                DrawingMessagesTranslatorTest.isMessageTypeAtPayload(MessageType.DRAW_EVENT, threePayloadDrawMessage, 0));
        Assert.assertTrue("Triple payload draw message's first status must be START",
                DrawingMessagesTranslatorTest.isDrawMessageStatusAtPayload(MessageStatus.START, threePayloadDrawMessage, 0));

        Assert.assertTrue("Triple payload draw message's second type must be DRAW_EVENT",
                DrawingMessagesTranslatorTest.isMessageTypeAtPayload(MessageType.DRAW_EVENT, threePayloadDrawMessage, 1));
        Assert.assertTrue("Triple payload draw message's second status must be IN_PROGRESS",
                DrawingMessagesTranslatorTest.isDrawMessageStatusAtPayload(MessageStatus.IN_PROGRESS, threePayloadDrawMessage, 1));

        Assert.assertTrue("Triple payload draw message's third type must be DRAW_EVENT",
                DrawingMessagesTranslatorTest.isMessageTypeAtPayload(MessageType.DRAW_EVENT, threePayloadDrawMessage, 2));
        Assert.assertTrue("Triple payload draw message's third status must be DONE",
                DrawingMessagesTranslatorTest.isDrawMessageStatusAtPayload(MessageStatus.DONE, threePayloadDrawMessage, 2));
    }

    @org.junit.Test
    public void decodeDrawMessage() {
        TestableDecodedMessageHandler handler = new TestableDecodedMessageHandler();
        DrawingMessagesTranslator.decodeMessage(twoPayloadDrawMessage, handler);

        DrawingAction[] decodedActions = handler.lastDrawingAction;

        assertNotNull("Decoded draw message actions is not null", decodedActions);
        assertTrue("Decoded draw message action length is 2", decodedActions.length == 2);

        DrawingAction firstAction = decodedActions[0];
        DrawingAction secondAction = decodedActions[1];

        assertEquals("Decoded brush colour is same as encoded", BRUSH_COLOR, firstAction.getDrawColour());
        assertEquals("Decoded brush size is same as encoded", RELATIVE_BRUSH_SIZE,  firstAction.getRelativeBrushSize());

        assertTrue("Both decoded drawing actions contain 1 point", firstAction.getRelativePointsDrawn().length == 1
            && secondAction.getRelativePointsDrawn().length == 1);
        assertEquals("First decoded point is same as encoded", firstAction.getRelativePointsDrawn()[0], TWO_PAYLOAD_POINTS[0]);
        assertEquals("Second decoded point is same as encoded", secondAction.getRelativePointsDrawn()[0], TWO_PAYLOAD_POINTS[1]);
    }



}
