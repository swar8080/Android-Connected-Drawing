package swar8080.collaborativedrawing.MessageTranslatorTests;

import org.junit.Assert;

import swar8080.collaborativedrawing.message.HandshakeIdentifier;
import swar8080.collaborativedrawing.message.HandshakeTranslator;
import swar8080.collaborativedrawing.Result;
import swar8080.collaborativedrawing.message.MessageDecodingException;
import swar8080.collaborativedrawing.message.UserCountResponse;

/**
 *
 */

public class HandshakeTranslatorTest {


    @org.junit.Test
    public void encodeAndDecodeUserCountHandshake(){
        UserCountResponse original = new UserCountResponse(100);

        byte[] encodedMessage = HandshakeTranslator.encodeUserCountResponse(original);
        Result<UserCountResponse> decoded = HandshakeTranslator.decodeUserCountResponse(encodedMessage);

        Assert.assertEquals("Decoded UserCountResponse equals Encoded", original, decoded.getResult());
    }


    @org.junit.Test(expected = MessageDecodingException.class)
    public void decodeNullIdentifier(){
        HandshakeTranslator.decodeMessageIdentifier(null);
    }

    @org.junit.Test(expected = MessageDecodingException.class)
    public void decodeEmptyIdentifier(){
        HandshakeTranslator.decodeMessageIdentifier(new byte[]{});
    }

    @org.junit.Test
    public void decodeUserCountResponseInvalidIdent(){
        Result<UserCountResponse> response;

        byte[] wrongIdentHeader = HandshakeTranslator.encodeIdentifierHeader(HandshakeIdentifier.PARTICIPANT);
        response = HandshakeTranslator.decodeUserCountResponse(wrongIdentHeader);
        Assert.assertFalse("Invalid header identifier should return false result",response.isSuccesful());
    }

}
