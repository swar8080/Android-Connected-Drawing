package swar8080.collaborativedrawing.message;

/**
 *
 */

public enum MessageType {
    DRAW_EVENT((byte)0), RESET_EVENT((byte)1);


    private final byte eventId;

    private MessageType(byte eventId){
        this.eventId = eventId;
    }

    public byte eventId(){
        return eventId;
    }
}

