package com.project.bittorrent.master;

import com.project.bittorrent.peer.PeerInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Master extends Thread {
    private int port;
    private static volatile Master master;

    public int getPort() {
        return port;
    }

    //constructor
    private Master(int port) {
        this.port = port;
    }

    public static Master getInstance() {
        if (master == null) {
            synchronized (PeerInfo.class) {
                if (master == null) {
                    PeerInfo peerInfo = PeerInfo.getInstance();
                    master = new Master(peerInfo.getPort());
                }
            }
        }
        return master;
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
                ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(clientConnection);
                clientConnectionHandler.start();
                //read peer connection info and store it
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
