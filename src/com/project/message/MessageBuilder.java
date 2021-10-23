package com.project.message;

import com.project.utils.Constants;

public class MessageBuilder {

    private static final String bytePadding = "0000000000"; //successive zero bytes

    public Message createUpperCase(Message message) {
        return new Message(message.getMessageString().toUpperCase());
    }

    public Message createPeerID(int peerID) {
        return new Message(new String("peerID " + peerID));
    }

    public Message createHandshake(int peerID) {
        String handshakeStr = new StringBuilder(Constants.MSG_HEADER)
                .append(bytePadding)
                .append(String.valueOf(peerID))
                .toString();
        Message handshakeMessage = new Message(handshakeStr);
        return handshakeMessage;
    }

}
