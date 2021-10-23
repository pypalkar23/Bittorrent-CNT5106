package com.project.bittorrent.server;

import com.project.bittorrent.peer.PeerInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNode extends Thread {
    private int port;
    private static volatile ServerNode serverNode;

    public int getPort() {
        return port;
    }

    //constructor
    private ServerNode(int port) {
        this.port = port;
    }

    public static ServerNode getInstance() {
        if (serverNode == null) {
            synchronized (PeerInfo.class) {
                if (serverNode == null) {
                    PeerInfo peerInfo = PeerInfo.getInstance();
                    serverNode = new ServerNode(peerInfo.getPort());
                }
            }
        }
        return serverNode;
    }

    @Override
    public void run() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            ServerSocket listener = new ServerSocket(this.getPort());
            System.out.printf("\n Master running on port: %s IP: %s Thread: %s%n", this.getPort(), InetAddress.getLoopbackAddress(), Thread.currentThread().getName());
            while (true) {
                Socket clientConnection = listener.accept();
                ClientNodeConnectionHandler clientNodeConnectionHandler = new ClientNodeConnectionHandler(clientConnection);
                clientNodeConnectionHandler.start();
                //read peer connection info and store it
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
