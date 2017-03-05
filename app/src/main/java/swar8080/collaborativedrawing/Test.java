package swar8080.collaborativedrawing;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by Steven on 2017-02-23.
 */

public class Test {

    public static void main(String[] args){
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);

        byteBuffer.put((byte)1);
        byteBuffer.put((byte)2);

        byte a = byteBuffer.get();
        byte b = byteBuffer.get();

        return;

    }
}
