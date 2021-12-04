package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class OptimisticUnchokeScheduler extends Thread {
    PeerInfo peer;
    Logger logger;
    CommonConfig commonConfig;
    CommonDataStore commonDataStore;
    Map<Integer, PeerInfo> peers;
    Map<Integer, ConnectionInfo> connectedPeers;

    public OptimisticUnchokeScheduler(CommonDataStore commonDataStore) {
        this.commonDataStore = commonDataStore;
        this.peers = commonDataStore.getPeers();
        this.peer = this.peers.get(commonDataStore.getHostID());
        this.connectedPeers = commonDataStore.getConnections();
        this.commonConfig = commonDataStore.getCommonConfig();
        this.logger = commonDataStore.getLogger();
    }

    @Override
    public void run() {
        while (commonDataStore.getCompletedPeers() < peers.size()) {
            ArrayList<Integer> connections = new ArrayList<>(connectedPeers.keySet());
            ArrayList<Integer> interested = new ArrayList<>();
            for (int connection : connections) {
                if (connectedPeers.get(connection).isInterested()) {
                    interested.add(connection);
                }
            }
            if (interested.size() > 0) {
                Random r = new Random();
                int randomNumber = Math.abs(r.nextInt() % interested.size());
                int connection = interested.get(randomNumber);
                connectedPeers.get(connection).unchoke();
                connectedPeers.get(connection).sendCommand(Constants.UNCHOKE);
                connectedPeers.get(connection).optimisticallyUnchoke();
                logger.hasOptimisticallyUnchokedNeighbour(peer.getPeerID(), connectedPeers.get(connection).getPeerID());
                try {
                    Thread.sleep(commonConfig.getOptimisticUnchokingInterval() * 1000L);
                    connectedPeers.get(connection).optimisticallyChoke();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
                //e.printStackTrace();
        }
        System.exit(0);
    }
}