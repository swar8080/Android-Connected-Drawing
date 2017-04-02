package swar8080.collaborativedrawing.MessageTranslatorTests;

import junit.framework.Assert;

import org.junit.Before;

import java.util.LinkedList;

import swar8080.collaborativedrawing.message.EncodedMessage;
import swar8080.collaborativedrawing.message.MessageProgress;
import swar8080.collaborativedrawing.message.MessageStatus;
import swar8080.collaborativedrawing.message.MessageTranslator;
import swar8080.collaborativedrawing.message.MessageType;

/**
 *
 */

public class MessageTranslatorTest {

    private LinkedList<EncodedMessage> messagesToMerge;

    @Before
    public void setUp(){
        messagesToMerge = new LinkedList<>();
        for (int i=0; i<3; i++)
            messagesToMerge.add(MessageTranslator.encodeResetMessage());
    }


    @org.junit.Test
    public void mergeEncodingStatusesAreUpdated() throws Exception{

        EncodedMessage merged = MessageTranslator.mergeMessages(messagesToMerge);

        Assert.assertTrue("First merged message status is START",isDrawMessageStatusAtPayload(MessageStatus.START, merged, 0));
        Assert.assertTrue("Second merged message status is IN_PROGRESS",isDrawMessageStatusAtPayload(MessageStatus.IN_PROGRESS, merged, 1));
        Assert.assertTrue("Third merged message status is DONE",isDrawMessageStatusAtPayload(MessageStatus.DONE, merged, 2));

    }

    @org.junit.Test
    public void mergeEncodingIdentifierNumbersMatch() throws Exception{
        EncodedMessage merged = MessageTranslator.mergeMessages(messagesToMerge);

        byte messageNumberExpected = getPayloadMessageNumberIdentifier(merged, 0);

        int payloadCount = messagesToMerge.size();
        for (int i=1; i<payloadCount; i++){
            Assert.assertEquals("Merged payload message identifier numbers should all be the same",
                    messageNumberExpected,
                    getPayloadMessageNumberIdentifier(merged,i));
        }
    }

    public static boolean isDrawMessageStatusAtPayload(MessageStatus expectedMessageStatus, EncodedMessage message, int payloadIndex ) throws Exception {
        MessageProgress drawMessageProgressIdentifier = MessageTranslator.getMessageProgress("", message.getMessage()[payloadIndex]);
        return drawMessageProgressIdentifier.getMessageStatus().equals(expectedMessageStatus);
    }

    public static boolean isMessageTypeAtPayload(MessageType expectedMessageType, EncodedMessage message, int payloadIndex) throws Exception {
        MessageProgress drawMessageProgressIdentifier = MessageTranslator.getMessageProgress("", message.getMessage()[payloadIndex]);
        return drawMessageProgressIdentifier.getMessageType().equals(expectedMessageType);
    }

    public static byte getPayloadMessageNumberIdentifier(EncodedMessage message, int payloadIndex) throws Exception{
        return MessageTranslator.getMessageProgress("", message.getMessage()[payloadIndex]).getMessageIdentifier().getSenderMessageNumber();
    }


}
