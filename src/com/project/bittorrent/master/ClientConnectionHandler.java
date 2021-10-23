package com.project.bittorrent.master;


import com.project.parserutils.dto.PeerConnectionDetails;
import com.project.bittorrent.peer.PeerInfo;
import com.project.message.Message;
import com.project.message.MessageBuilder;
import com.project.message.MessageValidator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import static com.project.utils.Constants.EXIT_MSG;

public class ClientConnectionHandler extends Thread {
    private static int REMOTE_PEER_ID_INDEX = 28;
    private final Socket client;
    private int peerIdSelf = PeerInfo.getInstance().getPeerID();
    private static int remotePeerID;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private final MessageBuilder messageBuilder;


    public ClientConnectionHandler(Socket client) {
        this.client = client;
        this.messageBuilder = new MessageBuilder();
        try {
            this.objectOutputStream = new ObjectOutputStream(client.getOutputStream());
            this.objectInputStream = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClient() {
        return client;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    @Override
    public void run() {
        try {
            System.out.println(String.format("A client has connected at: %s %s", client.getInetAddress(), client.getPort()));

            getAndSendHandshake();

            updateActivePeerConnections();

            while (true) {
                Message inputMessage = (Message) objectInputStream.readObject();
                System.out.println("Received msg: " + inputMessage.getMessageString());

                // close the socket after file transfer is done
                if (inputMessage.getMessageString().equals(EXIT_MSG))
                    break;
                Message outputMessage = messageBuilder.createUpperCase(inputMessage);

                objectOutputStream.flush();
                objectOutputStream.writeObject(outputMessage);
                System.out.println(String.format("Message %s has been sent to %s", outputMessage.getMessageString(), client.getInetAddress()));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                objectOutputStream.close();
                objectInputStream.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAndSendHandshake() {
        Message inputMessage = null;
        try {
            inputMessage = (Message) objectInputStream.readObject();
            System.out.println(String.format("Handshake Message received : %s", inputMessage.getMessageString()));

            remotePeerID = Integer.parseInt(inputMessage.getMessageString().substring(REMOTE_PEER_ID_INDEX));
            boolean isValidHandshakeMsg = MessageValidator.isValidHandshakeMessage(inputMessage, remotePeerID);
            //TODO: write functionality for invalidated msg as well

            Message outputMessage = messageBuilder.createHandshake(peerIdSelf);
            objectOutputStream.writeObject(outputMessage);
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateActivePeerConnections() {
        ArrayList<PeerConnectionDetails> totalPeerConnections = PeerInfo.getInstance().getTotalPeerConnections();
        for (PeerConnectionDetails peerConnectionInfo : totalPeerConnections) {
            if (peerConnectionInfo.getId() == remotePeerID)
                PeerInfo.getInstance().setActivePeerConnections(peerConnectionInfo);
        }
        System.out.println(String.format("Updated Neighbours..%s",PeerInfo.getInstance().getActivePeerConnections()));
    }
}
