package com.project;

import com.project.bittorrent.peer.PeerInfo;

public class PeerProcess {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Peer Process ID not given \n command should be \"java PeerProcess {peerId}\" \n ...Exiting!!!");
            System.exit(1);
        }
        PeerProcess peerProcess = new PeerProcess();
        peerProcess.setupPeerConfig(args[0]);

        /**
         * 1. start server sockets
         * 2. start client sockets
         *      2.1 send tcp connection to all peers above self peerID
         *      2.2 maintain list of all connected peers
         *      2.3. begin p2p protocol
         */

        Initiator initiator = new Initiator();
        initiator.init();
    }

    public void setupPeerConfig(String peerIdStr) {
        int peerId = Integer.parseInt( peerIdStr);
        System.out.println("peer Id given: " + peerId);

        PeerInfo peerInfo = PeerInfo.getInstance();
        peerInfo.setPeerID(peerId);
        peerInfo.setPeerConnectionInfo();
        System.out.println(peerInfo);
    }

}
