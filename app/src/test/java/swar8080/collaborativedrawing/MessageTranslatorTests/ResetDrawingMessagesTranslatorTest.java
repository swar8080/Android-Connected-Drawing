package swar8080.collaborativedrawing.MessageTranslatorTests;

import org.junit.Assert;
import org.junit.Before;

import swar8080.collaborativedrawing.message.EncodedMessage;
import swar8080.collaborativedrawing.message.MessageProgress;
import swar8080.collaborativedrawing.message.MessageStatus;
import swar8080.collaborativedrawing.message.DrawingMessagesTranslator;
import swar8080.collaborativedrawing.message.MessageType;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ResetDrawingMessagesTranslatorTest {

    private EncodedMessage resetMessage;

    @Before
    public void setUp(){
       resetMessage = DrawingMessagesTranslator.encodeResetMessage();
    }

    @org.junit.Test
    public void encodeResetMessageHeader()  {
        MessageProgress messageProgressIdentifier = DrawingMessagesTranslator.getMessageProgress("clientIdString", resetMessage.getMessage()[0]);
        Assert.assertEquals("Reset message type must be RESET_EVEN", MessageType.RESET_EVENT, messageProgressIdentifier.getMessageType());
        Assert.assertEquals("Reset message status must be DONE", MessageStatus.DONE, messageProgressIdentifier.getMessageStatus());
    }

    @org.junit.Test
    public void decodeResetMessage() {
        TestableDecodedMessageHandler handler = new TestableDecodedMessageHandler();
        DrawingMessagesTranslator.decodeMessage(resetMessage, handler);
        assertTrue("Reset callback made", handler.resetCallbackMade);
    }

}