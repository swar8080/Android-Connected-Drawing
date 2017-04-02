package swar8080.collaborativedrawing.message;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 */

public class EncodedMessage {

    private LinkedList<byte[]> payloads;

    public EncodedMessage(){
        init();
    }

    public EncodedMessage(byte[] payload){
        init();
        addPayload(payload);
    }

    private void init(){
        payloads = new LinkedList<>();
    }

    public void addPayload(byte[] payload){
        payloads.add(payload);
    }

    public byte[][] getMessage(){
        byte[][] message = new byte[payloads.size()][];

        Iterator<byte[]> iterator = payloads.iterator();
        int i = 0;
        while (iterator.hasNext())
            message[i++] = iterator.next();

        return message;
    }

}
