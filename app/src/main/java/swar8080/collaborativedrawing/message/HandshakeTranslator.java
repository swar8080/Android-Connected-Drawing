package swar8080.collaborativedrawing.message;

import java.nio.ByteBuffer;

import swar8080.collaborativedrawing.Result;

/**
 *
 */

public class HandshakeTranslator {

    private HandshakeTranslator(){}

    private static final int HEADER_LENGTH = 1;


    public static byte[] encodeIdentifierHeader(HandshakeIdentifier identifier){
        return new byte[]{identifier.getId()};
    }

    public static byte[] encodeUserCountResponse(UserCountResponse userCountResponse){
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE/8);
        buffer.putInt(0,userCountResponse.getUserCount());
        return addHeaderAndContent(HandshakeIdentifier.USER_COUNT_REQUEST, buffer.array());
    }

    public static Result<UserCountResponse> decodeUserCountResponse(byte[] content){
        UserCountResponse response;
        HandshakeIdentifier ident = decodeMessageIdentifier(content);

        if (HandshakeIdentifier.USER_COUNT_REQUEST != ident){
            return new Result<>(false);
        }

        if (content.length < HEADER_LENGTH + Integer.SIZE/8){
            throw new MessageDecodingException(content, "Invalid user count response");
        }

        //TODO add error handling for correct identifier, wrong content
        ByteBuffer buffer = ByteBuffer.wrap(content);
        response = new UserCountResponse(buffer.getInt(HEADER_LENGTH));
        return new Result<>(response);
    }

    private static byte[] addHeaderAndContent(HandshakeIdentifier identifier, byte[] content){
        byte[] header = encodeIdentifierHeader(identifier);
        ByteBuffer buffer = ByteBuffer.allocate(header.length + content.length);
        buffer.put(header);
        buffer.put(content);
        return buffer.array();
    }

    public static HandshakeIdentifier decodeMessageIdentifier(byte[] message) {
        if (message == null || message.length < HEADER_LENGTH)
            throw new MessageDecodingException(message);

        byte id = message[0];

        for (HandshakeIdentifier iden : HandshakeIdentifier.values()){
            if (id == iden.getId()){
                return iden;
            }
        }
        return null;
    }

}
