package swar8080.collaborativedrawing.message;

/**
 *
 */

public enum HandshakeIdentifier {
    PARTICIPANT((byte)0), USER_COUNT_REQUEST((byte)1);

    private final byte id;

    private HandshakeIdentifier(byte id){
        this.id = id;
    }

    public byte getId(){
        return id;
    }


}
