package swar8080.collaborativedrawing.message;

import android.support.v4.util.Pair;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import swar8080.collaborativedrawing.drawing.DrawingAction;

/**
 *
 */

public class MessageTranslator {


    private static final int INT_BYTES = Integer.SIZE/8;
    private static final int FLOAT_BYTES = Float.SIZE/8;

    private static final byte DRAW_EVENT = MessageType.DRAW_EVENT.eventId();
    private static final byte RESET_EVENT = MessageType.RESET_EVENT.eventId();

    private static byte mSendersMessageID = 0;
    private static byte getSenderMessageID() { return mSendersMessageID++; }


    public static MessageProgress getMessageProgress(String senderId, byte[] payload) throws MessageDecodingException {
        PayloadHeader header = PayloadHeader.getHeaderFromPayload(payload);
        return new MessageProgress(senderId, header.messageType, header.messageStatus, header.headerBytes, header.senderMessageId);
    }

    private static MessageStatus getMessageStatusBasedOnPayload(int currentPayload, int numberOfPayloads){
        MessageStatus status;

        if ((currentPayload == numberOfPayloads)){
            status = MessageStatus.DONE;
        }
        else if (currentPayload == 1){
            status = MessageStatus.START;
        }
        else {
            status = MessageStatus.IN_PROGRESS;
        }

        return status;
    }

    public static EncodedMessage mergeMessages(Collection<EncodedMessage> messages){
        byte[][] nextMessage;
        byte[] nextPayload;
        int currentPayload;
        int totalPayloads;
        byte messageIdentifier;

        EncodedMessage merged = new EncodedMessage();
        LinkedList<byte[]> payloadsToMerge = new LinkedList<>();


        Iterator<EncodedMessage> messageIterator = messages.iterator();
        while(messageIterator.hasNext()){
            nextMessage = messageIterator.next().getMessage();
            for (byte[] payload : nextMessage)
                payloadsToMerge.add(payload);
        }

        currentPayload = 1;
        totalPayloads = payloadsToMerge.size();
        messageIdentifier = getSenderMessageID();

        Iterator<byte[]> payloadIterator = payloadsToMerge.iterator();
        while (payloadIterator.hasNext()){
            nextPayload = payloadIterator.next();

            PayloadHeader.setMessageStatus(nextPayload, getMessageStatusBasedOnPayload(currentPayload, totalPayloads));
            PayloadHeader.setSenderMessageId(nextPayload, messageIdentifier);

            merged.addPayload(nextPayload);

            currentPayload++;
        }

        return merged;
    }

    public static void decodeMessage(EncodedMessage encodedMessage, DecodedMessageHandler handler) throws MessageDecodingException{
        byte[][] payloads = encodedMessage.getMessage();
        PayloadHeader firstHeader;

        if (payloads == null || payloads.length == 0)
            return;


        firstHeader = PayloadHeader.getHeaderFromPayload(payloads[0]);
        switch(firstHeader.messageType){
            case DRAW_EVENT:
                handler.onDrawMessageReceived(decodeDrawMessage(payloads));
                break;
            case RESET_EVENT:
                handler.onResetMessageReceived();
                break;
        }
    }


    public static EncodedMessage encodeDrawMessages(int color, float relativeBrushSize,
                                              Pair<Float,Float>[] relativePointsDrawn, int maximumBytesPerMessage){
        int fixedByteSize, variableByteSize;
        int totalVariableBytesRequired, relativePointPairsPerFullPayload, variableBytesPerFullPayload, payloadsRequired;
        int remainingVariableBytesRequired, indexOfNextPair;
        ByteBuffer fixedByteBuffer, nextPayload;
        PayloadHeader header;
        byte headerMessageNumberIdentifier;
        int payloadCount = 0;

        EncodedMessage message = new EncodedMessage();

        //create the static part of the bytes known to be of fixed size for each payload
        fixedByteSize = PayloadHeader.BYTE_COUNT + INT_BYTES + FLOAT_BYTES;
        fixedByteBuffer = ByteBuffer.allocate(fixedByteSize);
        fixedByteBuffer.putInt(PayloadHeader.BYTE_COUNT, color) ;
        fixedByteBuffer.putFloat(PayloadHeader.BYTE_COUNT+INT_BYTES,relativeBrushSize);

        //get part of identifier used to determine which message each payload belongs to
        headerMessageNumberIdentifier = getSenderMessageID();

        //calculate the number of point pairs that can fit into each payload
        //so that the total number of payloads required can be determined
        totalVariableBytesRequired = 2*relativePointsDrawn.length*FLOAT_BYTES;
        relativePointPairsPerFullPayload = (maximumBytesPerMessage-fixedByteSize)/(2*FLOAT_BYTES);
        variableBytesPerFullPayload = 2*relativePointPairsPerFullPayload*FLOAT_BYTES;
        payloadsRequired = (int)Math.ceil((double)totalVariableBytesRequired/variableBytesPerFullPayload);

        indexOfNextPair = 0;
        remainingVariableBytesRequired = totalVariableBytesRequired ;
        while (remainingVariableBytesRequired > 0){
            variableByteSize = (remainingVariableBytesRequired >= variableBytesPerFullPayload)? variableBytesPerFullPayload : remainingVariableBytesRequired;

            //add the header to the payload without altering the Buffer's position
            header = new PayloadHeader(MessageType.DRAW_EVENT,
                    getMessageStatusBasedOnPayload(++payloadCount, payloadsRequired),
                    headerMessageNumberIdentifier);

            for (int i = 0; i<header.headerBytes.length; i++)
                fixedByteBuffer.put(i, header.headerBytes[i]);

            //add fixed bytes to payload
            nextPayload = ByteBuffer.allocate(fixedByteSize+variableByteSize);
            nextPayload.put(fixedByteBuffer.array());

            //add variable bytes to payload
            int pointsToAdd = variableByteSize / (2*FLOAT_BYTES);
            for (int i=0; i< pointsToAdd; i++){
                nextPayload.putFloat(relativePointsDrawn[indexOfNextPair].first);
                nextPayload.putFloat(relativePointsDrawn[indexOfNextPair].second);
                indexOfNextPair++;
            }

            message.addPayload(nextPayload.array());

            remainingVariableBytesRequired -= variableByteSize;
        }

        return message;
    }

    public static EncodedMessage encodeResetMessage(){
        EncodedMessage message = new EncodedMessage();
        PayloadHeader header = new PayloadHeader(MessageType.RESET_EVENT, MessageStatus.DONE, getSenderMessageID());
        message.addPayload(header.headerBytes);
        return message;
    }

    private static DrawingAction[] decodeDrawMessage(byte[][]
                                                        message) throws MessageDecodingException {
        ByteBuffer buffer;
        int colour;
        float relativeBrushSize;
        Pair<Float,Float>[] relativeDrawPointPairs;

        DrawingAction[] drawingActions = new DrawingAction[message.length];

        try {
            int actionCount = 0;
            for (byte[] action : message){
                buffer = ByteBuffer.wrap(action);

                buffer = (ByteBuffer)buffer.position(PayloadHeader.BYTE_COUNT);
                colour = buffer.getInt();
                relativeBrushSize = buffer.getFloat();

                int relativeDrawPointPairCount = buffer.remaining() / (2*FLOAT_BYTES);
                relativeDrawPointPairs = new Pair[relativeDrawPointPairCount];
                for (int i=0; i < relativeDrawPointPairCount; i++){
                    relativeDrawPointPairs[i] = new Pair<Float,Float>(buffer.getFloat(), buffer.getFloat());
                }

                drawingActions[actionCount++] = new DrawingAction(colour, relativeBrushSize, relativeDrawPointPairs);
            }
        }
        catch (BufferUnderflowException bue){
            throw new MessageDecodingException("Decoding draw message", DRAW_EVENT);
        }

        return drawingActions;
    }


    public static class MessageDecodingException extends Exception{

        private Byte messageType;

        private MessageDecodingException(String message){
            super(message);
            messageType = null;
        }

        private MessageDecodingException(String message, Byte messageType) {
            super(message);
            this.messageType = messageType;
        }

    }

    private static class PayloadHeader {

        byte[] headerBytes;
        byte senderMessageId;
        MessageType messageType;
        MessageStatus messageStatus;

        private static final int BYTE_COUNT = 3;
        private static final int TYPE_INDEX = 0;
        private static final int STATUS_INDEX = 1;
        private static final int MESSAGE_ID_INDEX = 2;

        PayloadHeader(MessageType messageType, MessageStatus status, byte senderMessageId){
            this.messageType = messageType;
            this.messageStatus = status;
            this.senderMessageId = senderMessageId;
            this.headerBytes = new byte[]{messageType.eventId(), status.getStatusId(), senderMessageId};
        }

        private static PayloadHeader getHeaderFromPayload(byte[] payload) throws MessageDecodingException {
            MessageType messageType = null;
            MessageStatus messageStatus = null;

            if (payload == null || payload.length < PayloadHeader.BYTE_COUNT)
                throw new MessageDecodingException("Payload header is too small or empty");

            for (MessageType mt : MessageType.values()){
                if (mt.eventId() == payload[TYPE_INDEX]){
                    messageType = mt;
                    break;
                }
            }
            if (messageType == null)
                throw new MessageDecodingException("Header missing message type");

            for (MessageStatus ms : MessageStatus.values()){
                if (ms.getStatusId() == payload[STATUS_INDEX]){
                    messageStatus = ms;
                    break;
                }
            }
            if (messageType == null)
                throw new MessageDecodingException("Header missing message status");

            return new PayloadHeader(messageType, messageStatus, payload[MESSAGE_ID_INDEX]);
        }

        private static void setMessageType(byte[] payload, MessageType newMessageType){
            payload[TYPE_INDEX] = newMessageType.eventId();
        }

        private static void setMessageStatus(byte[] payload, MessageStatus newStatus){
            payload[STATUS_INDEX] = newStatus.getStatusId();
        }


        private static void setSenderMessageId(byte[] payload, byte newSenderMessageId) {
            payload[MESSAGE_ID_INDEX] = newSenderMessageId;
        }

    }

}
