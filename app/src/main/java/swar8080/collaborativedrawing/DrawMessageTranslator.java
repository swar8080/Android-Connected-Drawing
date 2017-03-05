package swar8080.collaborativedrawing;

import android.util.Pair;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by Steven on 2017-02-22.
 */

public class DrawMessageTranslator {

    public static final byte DRAW_EVENT = 0;
    public static final byte RESET_EVENT = 1;

    private static final int INT_BYTES = Integer.SIZE/8;
    private static final int FLOAT_BYTES = Float.SIZE/8;

    public interface onDrawMessageHandler {
        void onDrawMessageReceived(DrawMessage drawMessage);
        void onResetMessageReceived();
    }

    public static boolean isEvent(byte[] message, byte eventType){
        if (message == null || message.length == 0)
            return false;
        else
            return message[0] == eventType;
    }

    public static byte[][] encodeDrawMessages(int color, float relativeBrushSize,
                                              Pair<Float,Float>[] relativePointsDrawn, int maximumBytesPerMessage){
        int fixedByteSize, variableByteSize;
        int totalVariableBytesRequired, relativePointPairsPerFullMessage, variableBytesPerFullMessage;
        int remainingVariableBytesRequired, indexOfNextPair;
        ByteBuffer fixedByteBuffer, message;
        byte[] fixedBytes;
        byte[][] messages;
        int messageCount = 0;

        fixedByteSize = 1 + INT_BYTES + FLOAT_BYTES;
        fixedByteBuffer = ByteBuffer.allocate(fixedByteSize);
        fixedByteBuffer.put(DRAW_EVENT);
        fixedByteBuffer.putInt(color);
        fixedByteBuffer.putFloat(relativeBrushSize);
        fixedBytes = fixedByteBuffer.array();

        totalVariableBytesRequired = 2*relativePointsDrawn.length*FLOAT_BYTES;
        relativePointPairsPerFullMessage = (maximumBytesPerMessage-fixedByteSize)/(2*FLOAT_BYTES);
        variableBytesPerFullMessage = 2*relativePointPairsPerFullMessage*FLOAT_BYTES;
        messages = new byte[(int)Math.ceil((double)totalVariableBytesRequired/variableBytesPerFullMessage)][];

        indexOfNextPair = 0;
        remainingVariableBytesRequired = totalVariableBytesRequired ;
        while (remainingVariableBytesRequired > 0){
            variableByteSize = (remainingVariableBytesRequired >= variableBytesPerFullMessage)? variableBytesPerFullMessage : remainingVariableBytesRequired;
            message = ByteBuffer.allocate(fixedByteSize+variableByteSize);

            message.put(fixedBytes);

            int pointsToAdd = variableByteSize / (2*FLOAT_BYTES);
            for (int i=0; i< pointsToAdd; i++){
                message.putFloat(relativePointsDrawn[indexOfNextPair].first);
                message.putFloat(relativePointsDrawn[indexOfNextPair].second);
                indexOfNextPair++;
            }

            messages[messageCount++] = message.array();

            remainingVariableBytesRequired -= variableByteSize;
        }

        return messages;
    }

    public static byte[] encodeResetMessage(){
        return new byte[]{RESET_EVENT};
    }

    public static void decodeMessage(byte[] message, onDrawMessageHandler drawMessageHandler) throws DrawMessageDecodingException {
        if (message == null || message.length == 0)
            throw new DrawMessageDecodingException("Message was empty");


        byte messageType = message[0];

        switch (messageType){
            case DRAW_EVENT:
                DrawMessage dm = _decodeDrawMessage(message) ;
                drawMessageHandler.onDrawMessageReceived(dm);
                break;
            case RESET_EVENT:
                drawMessageHandler.onResetMessageReceived();
                break;
            default:
                throw new DrawMessageDecodingException(String.format("Unknown message received with code %d", messageType));
        }

    }

    private static DrawMessage _decodeDrawMessage(byte[] message) throws DrawMessageDecodingException {
        ByteBuffer buffer;
        int colour;
        float relativeBrushSize;
        Pair<Float,Float>[] relativeDrawPointPairs;

        try {
            buffer = ByteBuffer.wrap(message);
            if (buffer.get() != DRAW_EVENT)
                throw new DrawMessageDecodingException("Decoding invalid draw message", DRAW_EVENT);

            colour = buffer.getInt();
            relativeBrushSize = buffer.getFloat();

            int relativeDrawPointPairCount = buffer.remaining() / (2*FLOAT_BYTES);
            relativeDrawPointPairs = new Pair[relativeDrawPointPairCount];
            for (int i=0; i < relativeDrawPointPairCount; i++){
                relativeDrawPointPairs[i] = new Pair<Float,Float>(buffer.getFloat(), buffer.getFloat());
            }

        }
        catch (BufferUnderflowException bue){
            throw new DrawMessageDecodingException("Decoding draw message", DRAW_EVENT);
        }

        return new DrawMessage(colour, relativeBrushSize, relativeDrawPointPairs);
    }


    public static class DrawMessageDecodingException extends Exception{

        private Byte messageType;

        private DrawMessageDecodingException(String message){
            super(message);
            messageType = null;
        }

        private DrawMessageDecodingException(String message, Byte messageType) {
            super(message);
            messageType = messageType;
        }

        public Byte getMessageType(){
            return new Byte(messageType);
        }

        public boolean hasMessageType(){ return messageType != null; }
    }

}
