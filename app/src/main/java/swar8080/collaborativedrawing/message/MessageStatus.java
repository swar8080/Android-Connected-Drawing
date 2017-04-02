package swar8080.collaborativedrawing.message;

/**
 *
 */

public enum MessageStatus {
    START((byte)0),IN_PROGRESS((byte)1),DONE((byte)2);

    private final byte statusId;

    private MessageStatus(byte statusId){
        this.statusId = statusId;
    }

    public byte getStatusId(){return statusId;}
}
