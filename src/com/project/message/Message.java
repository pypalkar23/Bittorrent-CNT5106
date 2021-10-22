package com.project.message;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Message implements Serializable {
    private String messageString;

    public Message(String messageString) {
        this.messageString = messageString;
    }

    public String getMessageString() {
        return messageString;
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }

    public byte[] convertMessageToByteArray() {
        return this.messageString.getBytes(StandardCharsets.UTF_8);
    }

    public Message convertByteArrayToMessage(byte[] messageInBytes) {
        this.messageString = new String(messageInBytes, StandardCharsets.UTF_8);
        return this;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageString='" + messageString + '\'' +
                '}';
    }
}
