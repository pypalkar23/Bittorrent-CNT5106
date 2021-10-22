package com.project.bittorrent.client;

import com.project.ParserUtils.DTO.PeerConnectionDetails;
import com.project.bittorrent.peer.PeerInfo;

import java.io.IOException;
import java.net.Socket;

public class Client {

    public void init() {
        PeerInfo peerInfo = PeerInfo.getInstance();

        for (PeerConnectionDetails peerConnectionDetails : peerInfo.getTotalPeerConnections()) {
            if (peerConnectionDetails.getId() == PeerInfo.getInstance().getPeerID())
                break;
            try {
                System.out.println(String.format("Client details: %s", peerConnectionDetails));
                Socket toServer = new Socket(peerConnectionDetails.getName(), peerConnectionDetails.getPort());
                System.out.println(String.format("Socket created at %s:%s", toServer.getInetAddress(), toServer.getPort()));
                peerInfo.setActivePeerConnections(peerConnectionDetails);
                System.out.println(String.format("Active peer connection updated: %s",peerInfo.getActivePeerConnections()));
                MasterConnectionHandler masterConnectionHandler = new MasterConnectionHandler(toServer, peerConnectionDetails.getId());
                masterConnectionHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
