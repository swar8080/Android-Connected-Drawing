package swar8080.collaborativedrawing.MessageTranslatorTests;

import junit.framework.Assert;

import org.junit.Before;

import java.util.LinkedList;

import swar8080.collaborativedrawing.message.EncodedMessage;
import swar8080.collaborativedrawing.message.MessageProgress;
import swar8080.collaborativedrawing.message.MessageStatus;
import swar8080.collaborativedrawing.message.DrawingMessagesTranslator;
import swar8080.collaborativedrawing.message.MessageType;

/**
 *
 */

public class DrawingMessagesTranslatorTest {

    private LinkedList<EncodedMessage> messagesToMerge;

    @Before
    public void setUp(){
        messagesToMerge = new LinkedList<>();
        for (int i=0; i<3; i++)
            messagesToMerge.add(DrawingMessagesTranslator.encodeResetMessage());
    }


    @org.junit.Test
    public void mergeEncodingStatusesAreUpdated(){

        EncodedMessage merged = DrawingMessagesTranslator.mergeMessages(messagesToMerge);

        Assert.assertTrue("First merged message status is START",isDrawMessageStatusAtPayload(MessageStatus.START, merged, 0));
        Assert.assertTrue("Second merged message status is IN_PROGRESS",isDrawMessageStatusAtPayload(MessageStatus.IN_PROGRESS, merged, 1));
        Assert.assertTrue("Third merged message status is DONE",isDrawMessageStatusAtPayload(MessageStatus.DONE, merged, 2));

    }

    @org.junit.Test
    public void mergeEncodingIdentifierNumbersMatch() {
        EncodedMessage merged = DrawingMessagesTranslator.mergeMessages(messagesToMerge);

        byte messageNumberExpected = getPayloadMessageNumberIdentifier(merged, 0);

        int payloadCount = messagesToMerge.size();
        for (int i=1; i<payloadCount; i++){
            Assert.assertEquals("Merged payload message identifier numbers should all be the same",
                    messageNumberExpected,
                    getPayloadMessageNumberIdentifier(merged,i));
        }
    }

    static boolean isDrawMessageStatusAtPayload(MessageStatus expectedMessageStatus, EncodedMessage message, int payloadIndex )  {
        MessageProgress drawMessageProgressIdentifier = DrawingMessagesTranslator.getMessageProgress("", message.getMessage()[payloadIndex]);
        return drawMessageProgressIdentifier.getMessageStatus().equals(expectedMessageStatus);
    }

    static boolean isMessageTypeAtPayload(MessageType expectedMessageType, EncodedMessage message, int payloadIndex)  {
        MessageProgress drawMessageProgressIdentifier = DrawingMessagesTranslator.getMessageProgress("", message.getMessage()[payloadIndex]);
        return drawMessageProgressIdentifier.getMessageType().equals(expectedMessageType);
    }

    static byte getPayloadMessageNumberIdentifier(EncodedMessage message, int payloadIndex) {
        return DrawingMessagesTranslator.getMessageProgress("", message.getMessage()[payloadIndex]).getMessageIdentifier().getSenderMessageNumber();
    }


}
