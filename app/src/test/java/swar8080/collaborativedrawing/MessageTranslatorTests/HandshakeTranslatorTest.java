package swar8080.collaborativedrawing.MessageTranslatorTests;

import org.junit.Assert;

import swar8080.collaborativedrawing.message.HandshakeTranslator;
import swar8080.collaborativedrawing.Result;
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

    @org.junit.Test
    public void decodeInvalidUserCountResponse(){
        Result<UserCountResponse> response;

        byte[] invalidHeader = new byte[]{Byte.MAX_VALUE};
        response = HandshakeTranslator.decodeUserCountResponse(invalidHeader);
        Assert.assertFalse("Invalid header should return false result",response.isSuccesful());

        byte[] nullResponse = null;
        response = HandshakeTranslator.decodeUserCountResponse(nullResponse);
        Assert.assertFalse("Null response should return false result",response.isSuccesful());

        //TODO add test for missing/incorrect content

    }


}
