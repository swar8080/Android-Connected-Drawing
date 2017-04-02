package swar8080.collaborativedrawing.message;

/**
 *
 */

public class MessageProgress {


    private final MessageStatus mMessageStatus;
    private final MessageType mMessageType;
    private final byte[] mEncodedMessage;
    private MessageProgressIdentifier mMessageProgressIdentifier;

   public MessageProgress(String senderId,
                          MessageType eventType, MessageStatus messageStatus,
                          byte[] message,
                          byte senderMessageNumber){
       mMessageType = eventType;
       mMessageStatus = messageStatus;
       mEncodedMessage = message;
       mMessageProgressIdentifier = new MessageProgressIdentifier(senderMessageNumber, senderId);
   }

    public MessageStatus getMessageStatus(){
        return mMessageStatus;
    }

    public MessageType getMessageType(){
        return mMessageType;
    }

    public byte[] getEncodedMessage(){ return mEncodedMessage; }

    public MessageProgressIdentifier getMessageIdentifier() { return mMessageProgressIdentifier; }

}
