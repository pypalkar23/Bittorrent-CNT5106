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
                for (int connection : connections) {
                    if(connectedPeers.get(connection).isInterested())
                        interestedPeers.add(connection);
                }
                if (interestedPeers.size() > 0) {
                    if (interestedPeers.size() <= commonConfig.getNumberOfPreferredNeighbors()) {
                        for (Integer interestedPeer : interestedPeers) {
                            if(connectedPeers.get(interestedPeer).isChoked()){
                                connectedPeers.get(interestedPeer).unchoke();
                                connectedPeers.get(interestedPeer).sendCommand(Constants.UNCHOKE_MSG);
                            }
                        }
                    } else {
                        Random r = new Random();
                        for (int i = 0; i < commonConfig.getNumberOfPreferredNeighbors(); i++) {
                            preferredNeighbors[i] = (interestedPeers.remove(Math.abs(r.nextInt() % interestedPeers.size())));
                        }
                        for (int preferredNeighbour : preferredNeighbors) {
                            if(connectedPeers.get(preferredNeighbour).isChoked()){
                                connectedPeers.get(preferredNeighbour).unchoke();
                                connectedPeers.get(preferredNeighbour).sendCommand(Constants.UNCHOKE_MSG);
                            }
                        }
                        for (Integer interestedPeer : interestedPeers) {
                            if(!connectedPeers.get(interestedPeer).isChoked() && !connectedPeers.get(interestedPeer).isOptimisticallyUnchoked()){
                                connectedPeers.get(interestedPeer).choke();
                                connectedPeers.get(interestedPeer).sendCommand(Constants.CHOKE_MSG);
                            }
                        }
                    }
                }
            }
            else{
                ArrayList<Integer> interestedPeers = new ArrayList<>();
                int counter = 0;
                for (int connection : connections) {
                    if(connectedPeers.get(connection).isInterested() && connectedPeers.get(connection).getDownloadRate() >= 0)
                        interestedPeers.add(connection);
                }
                if(interestedPeers.size() <= commonConfig.getNumberOfPreferredNeighbors()){
                    for(int interestedPeer : interestedPeers){
                        preferredNeighbors[counter++] = interestedPeer;
                        if(connectedPeers.get(interestedPeer).isChoked()){
                            connectedPeers.get(interestedPeer).unchoke();
                            connectedPeers.get(interestedPeer).sendCommand(Constants.UNCHOKE_MSG);
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
                            connectedPeers.get(max).sendCommand(Constants.UNCHOKE_MSG);
                        }
                        preferredNeighbors[i] = max;
                        interestedPeers.remove(Integer.valueOf(max));
                    }
                    for (Integer peer : interestedPeers) {
                        if(!connectedPeers.get(peer).isChoked() && !connectedPeers.get(peer).isOptimisticallyUnchoked()){
                            connectedPeers.get(peer).choke();
                            connectedPeers.get(peer).sendCommand(Constants.CHOKE_MSG);
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