package com.project.bittorrent.peer;

import java.net.Socket;

public class Node {
    private final Socket socket;
    private final PeerInfo peerInfo;

    public Node(Socket socket, PeerInfo peerInfo) {
        this.socket = socket;
        this.peerInfo = peerInfo;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public PeerInfo getPeerInfo() {
        return this.peerInfo;
    }

    @Override
    public String toString() {
        return "Node{" +
                "socket=" + socket +
                ", peerInfo=" + peerInfo +
                '}';
    }
}
