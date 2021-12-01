package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class OptimisticUnchokePeer extends Thread {
    PeerInfo peer;
    CommonData commonData;
    Map<Integer, PeerInfo> peers;
    Map<Integer, PeerConnection> peerConnections;
    Logger logger;
    CommonInfo common;

    public OptimisticUnchokePeer(PeerInfo peer, Map<Integer, PeerInfo> peers, Map<Integer, PeerConnection> peerConnections, CommonInfo commonInfo, CommonData commonData, Logger logger) {
        this.peer = peer;
        this.peers = peers;
        this.peerConnections = peerConnections;
        this.commonData = commonData;
        this.common = commonInfo;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (commonData.getCompletedPeers() < peers.size()) {
            ArrayList<Integer> connections = new ArrayList<>(peerConnections.keySet());
            ArrayList<Integer> interested = new ArrayList<>();
            for (int connection : connections) {
                if (peerConnections.get(connection).isInterested()) {
                    interested.add(connection);
                }
            }
            if (interested.size() > 0) {
                Random r = new Random();
                int randomNumber = Math.abs(r.nextInt() % interested.size());
                int connection = interested.get(randomNumber);
                peerConnections.get(connection).unchoke();
                peerConnections.get(connection).sendMessage(Constants.UNCHOKE);
                peerConnections.get(connection).optimisticallyUnchoke();
                logger.changeOptimisticallyUnchokedNeighbor(peer.getPeerID(), peerConnections.get(connection).getPeerID());
                try {
                    Thread.sleep(common.getOptimisticUnchokingInterval() * 1000);
                    peerConnections.get(connection).optimisticallyChoke();
                } catch (Exception e) {
//                        e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }
        System.exit(0);
    }
}