package swar8080.collaborativedrawing.message;

import java.util.HashMap;

import swar8080.collaborativedrawing.message.EncodedMessage;

/**
 *
 */

public class MessageAccumulator<ID> {

    private HashMap<ID, EncodedMessage> messages;

    public MessageAccumulator(){
        messages = new HashMap<>();
    }

    public void addMessage(ID id, byte[] payload){
        if (!messages.containsKey(id)){
            messages.put(id, new EncodedMessage());
        }
        messages.get(id).addPayload(payload);
    }

    public EncodedMessage removeMessage(ID id){
        if (messages.containsKey(id)){
            return messages.remove(id);
        }
        else
            return null;
    }

}
