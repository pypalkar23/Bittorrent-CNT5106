package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PeerUnchokeScheduler extends Thread{
    private final Logger logger;
    private final PeerInfo selfPeer;
    private final CommonConfig commonConfig;
    private final CommonDataStore commonDataStore;
    private final Map<Integer, PeerInfo> peers;
    private final Map<Integer, ConnectionInfo> connectedPeers;

    public PeerUnchokeScheduler(CommonDataStore commonDataStore){
        this.commonDataStore = commonDataStore;
        this.peers = commonDataStore.getPeers();
        this.connectedPeers = commonDataStore.getConnections();
        this.commonConfig = commonDataStore.getCommonConfig();
        this.logger = commonDataStore.getLogger();
        this.selfPeer = this.peers.get(commonDataStore.getHostID());
    }

    @Override
    public void run(){
        while(commonDataStore.getCompletedPeers() < peers.size()){
            ArrayList<Integer> connections = new ArrayList<>(connectedPeers.keySet());
            int[] preferredNeighbors = new int[commonConfig.getNumberOfPreferredNeighbors()];
            if(selfPeer.getHaveFile() == 1) {
                ArrayList<Integer> interestedPeers = new ArrayList<>();
                for (int peer : connections) {
                    if(connectedPeers.get(peer).isInterested())
                        interestedPeers.add(peer);
                }
                if (interestedPeers.size() > 0) {
                    if (interestedPeers.size() <= commonConfig.getNumberOfPreferredNeighbors()) {
                        for (Integer peer : interestedPeers) {
                            if(connectedPeers.get(peer).isChoked()){
                                connectedPeers.get(peer).unchoke();
                                connectedPeers.get(peer).sendCommand(Constants.UNCHOKE);
                            }
                        }
                    } else {
                        Random r = new Random();
                        for (int i = 0; i < commonConfig.getNumberOfPreferredNeighbors(); i++) {
                            preferredNeighbors[i] = (interestedPeers.remove(Math.abs(r.nextInt() % interestedPeers.size())));
                        }
                        for (int peer : preferredNeighbors) {
                            if(connectedPeers.get(peer).isChoked()){
                                connectedPeers.get(peer).unchoke();
                                connectedPeers.get(peer).sendCommand(Constants.UNCHOKE);
                            }
                        }
                        for (Integer peer : interestedPeers) {
                            if(!connectedPeers.get(peer).isChoked() && !connectedPeers.get(peer).isOptimisticallyUnchoked()){
                                connectedPeers.get(peer).choke();
                                connectedPeers.get(peer).sendCommand(Constants.CHOKE);
                            }
                        }
                    }
                }
            }
            else{
                ArrayList<Integer> interestedPeers = new ArrayList<>();
                int counter = 0;
                for (int peer : connections) {
                    if(connectedPeers.get(peer).isInterested() && connectedPeers.get(peer).getDownloadRate() >= 0)
                        interestedPeers.add(peer);
                }
                if(interestedPeers.size() <= commonConfig.getNumberOfPreferredNeighbors()){
                    for(int peer : interestedPeers){
                        preferredNeighbors[counter++] = peer;
                        if(connectedPeers.get(peer).isChoked()){
                            connectedPeers.get(peer).unchoke();
                            connectedPeers.get(peer).sendCommand(Constants.UNCHOKE);
                        }
                    }
                }
                else {
                    for (int i = 0; i < commonConfig.getNumberOfPreferredNeighbors(); i++) {
                        int max = interestedPeers.get(0);
                        for(int j = 1; j < interestedPeers.size(); j++){
                            if(connectedPeers.get(max).getDownloadRate() <= connectedPeers.get(interestedPeers.get(j)).getDownloadRate()){
                                max = interestedPeers.get(j);
                            }
                        }
                        if(connectedPeers.get(max).isChoked()) {
                            connectedPeers.get(max).unchoke();
                            connectedPeers.get(max).sendCommand(Constants.UNCHOKE);
                        }
                        preferredNeighbors[i] = max;
                        interestedPeers.remove(Integer.valueOf(max));
                    }
                    for (Integer peer : interestedPeers) {
                        if(!connectedPeers.get(peer).isChoked() && !connectedPeers.get(peer).isOptimisticallyUnchoked()){
                            connectedPeers.get(peer).choke();
                            connectedPeers.get(peer).sendCommand(Constants.CHOKE);
                        }
                    }
                }
            }
            logger.hasChangedPreferredNeighbours(selfPeer.getPeerID(), preferredNeighbors);
            try{
                Thread.sleep(commonConfig.getUnchokingInterval() * 1000L);
            }
            catch(Exception e){
                //e.printStackTrace();
            }
        }
        try{
            Thread.sleep(5000);
        }
        catch(Exception e){
            //e.printStackTrace();
        }
        System.exit(0);
    }
}