package swar8080.collaborativedrawing.message;

/**
 *
 */

public class MessageDecodingException extends RuntimeException {

    private byte[] payload;

    public MessageDecodingException(byte[] payload){
        super();
        this.payload = payload;
    }

    public MessageDecodingException(byte[] payload, String message){
        super(message);
        this.payload = payload;
    }

    public MessageDecodingException(byte[] payload, String message, Throwable cause){
        super(message, cause);
        this.payload = payload;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (message == null){
            if (isMessageEmpty()) {
                message = "Empty message";
            }
        }
        return message;
    }

    public boolean isMessageEmpty(){
        return payload == null && payload.length == 0;
    }

    public boolean isMessageNull(){
        return payload == null;
    }

    public boolean isMessageLengthZero(){
        return payload != null && payload.length == 0;
    }
}
