package com.project.bittorrent.client;

import com.project.bittorrent.peer.PeerInfo;
import com.project.message.Message;
import com.project.message.MessageBuilder;
import com.project.message.MessageValidator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MasterConnectionHandler extends Thread {
    private final Socket serverConnectionSocket;
    private int peerIDSelf = PeerInfo.getInstance().getPeerID();
    private int remotePeerID;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final MessageBuilder messageBuilder;

    public MasterConnectionHandler(Socket serverConnectionSocket, int remotePeerID) {
        this.serverConnectionSocket = serverConnectionSocket;
        this.remotePeerID = remotePeerID;
        this.messageBuilder = new MessageBuilder();
        try {
            this.objectOutputStream = new ObjectOutputStream(serverConnectionSocket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(serverConnectionSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            objectOutputStream.flush();
            sendAndGetHandshake();

            while (true) {
                Message inputMessage = (Message) objectInputStream.readObject();
                System.out.println(String.format("Message has been received : %s from %s", inputMessage.getMessageString(), serverConnectionSocket.getInetAddress()));
                break;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendAndGetHandshake() {
        Message outputMessage = messageBuilder.createHandshake(peerIDSelf);
        System.out.println(String.format("HandshakeMsg created: %s for id %d", outputMessage.getMessageString(), remotePeerID));
        try {
            objectOutputStream.writeObject(outputMessage);
            objectOutputStream.flush();

            Message inputMessage = (Message) objectInputStream.readObject();
            System.out.println(String.format("message received : %s from id: %d", inputMessage.getMessageString(), remotePeerID));

            boolean isValidHandshakeMsg = MessageValidator.isValidHandshakeMessage(inputMessage, remotePeerID);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("msg: %s sent to address: %s", outputMessage.getMessageString(), serverConnectionSocket.getInetAddress()));
    }
}
