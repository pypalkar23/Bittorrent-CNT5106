package com.project;

import com.project.bittorrent.client.ClientNode;
import com.project.bittorrent.server.ServerNode;

public class Initiator {
    public void init() {
        ServerNode serverNode = ServerNode.getInstance();
        serverNode.start();

        ClientNode clientNode = new ClientNode();
        clientNode.init();
    }
}
