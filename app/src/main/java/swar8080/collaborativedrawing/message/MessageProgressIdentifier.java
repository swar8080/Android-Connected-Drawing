package swar8080.collaborativedrawing.message;

/**
 *
 */

public class MessageProgressIdentifier {

    private String senderId;

    private byte senderMessageNumber;

    public MessageProgressIdentifier(byte senderMessageNumber, String senderId){
        this.senderId = senderId;
        this.senderMessageNumber = senderMessageNumber;
    }

    public String getSenderId() {
        return new String(senderId);
    }

    public byte getSenderMessageNumber() {
        return senderMessageNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MessageProgressIdentifier))
            return false;
        MessageProgressIdentifier other = (MessageProgressIdentifier)obj;
        return other.senderId.equals(this.senderId)
                && other.senderMessageNumber == this.senderMessageNumber;
    }

    @Override
    public int hashCode() {
        return (senderId + String.valueOf(senderMessageNumber)).hashCode();
    }
}
