package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class UnchokePeers extends Thread{
    private CommonData commonData;
    private Map<Integer, PeerInfo> peers;
    private Map<Integer,PeerConnection> peerConnections;
    private CommonInfo common;
    private Logger logger;
    private PeerInfo selfPeer;

    public UnchokePeers(Map<Integer,PeerInfo> peers,Map<Integer,PeerConnection> peerConnections,PeerInfo selfPeer, CommonInfo common, CommonData data, Logger log){
        this.commonData = data;
        this.peers = peers;
        this.peerConnections = peerConnections;
        this.common = common;
        this.logger = log;
        this.selfPeer = selfPeer;
    }

    @Override
    public void run(){
        while(commonData.getCompletedPeers() < peers.size()){
            ArrayList<Integer> connections = new ArrayList<>(peerConnections.keySet());
            int[] preferredNeighbors = new int[common.getNumberOfPreferredNeighbors()];
            if(selfPeer.getHaveFile() == 1) {
                ArrayList<Integer> interestedPeers = new ArrayList<>();
                for (int peer : connections) {
                    if(peerConnections.get(peer).isInterested())
                        interestedPeers.add(peer);
                }
                if (interestedPeers.size() > 0) {
                    if (interestedPeers.size() <= common.getNumberOfPreferredNeighbors()) {
                        for (Integer peer : interestedPeers) {
                            if(peerConnections.get(peer).isChoked()){
                                peerConnections.get(peer).unchoke();
                                peerConnections.get(peer).sendMessage(Constants.UNCHOKE);
                            }
                        }
                    } else {
                        Random r = new Random();
                        for (int i = 0; i < common.getNumberOfPreferredNeighbors(); i++) {
                            preferredNeighbors[i] = (interestedPeers.remove(Math.abs(r.nextInt() % interestedPeers.size())));
                        }
                        for (int peer : preferredNeighbors) {
                            if(peerConnections.get(peer).isChoked()){
                                peerConnections.get(peer).unchoke();
                                peerConnections.get(peer).sendMessage(Constants.UNCHOKE);
                            }
                        }
                        for (Integer peer : interestedPeers) {
                            if(!peerConnections.get(peer).isChoked() && !peerConnections.get(peer).isOptimisticallyUnchoked()){
                                peerConnections.get(peer).choke();
                                peerConnections.get(peer).sendMessage(Constants.CHOKE);
                            }
                        }
                    }
                }
            }
            else{
                ArrayList<Integer> interestedPeers = new ArrayList<>();
                int counter = 0;
                for (int peer : connections) {
                    if(peerConnections.get(peer).isInterested() && peerConnections.get(peer).getDownloadRate() >= 0)
                        interestedPeers.add(peer);
                }
                if(interestedPeers.size() <= common.getNumberOfPreferredNeighbors()){
                    for(int peer : interestedPeers){
                        preferredNeighbors[counter++] = peer;
                        if(peerConnections.get(peer).isChoked()){
                            peerConnections.get(peer).unchoke();
                            peerConnections.get(peer).sendMessage(Constants.UNCHOKE);
                        }
                    }
                }
                else {
                    for (int i = 0; i < common.getNumberOfPreferredNeighbors(); i++) {
                        int max = interestedPeers.get(0);
                        for(int j = 1; j < interestedPeers.size(); j++){
                            if(peerConnections.get(max).getDownloadRate() <= peerConnections.get(interestedPeers.get(j)).getDownloadRate()){
                                max = interestedPeers.get(j);
                            }
                        }
                        if(peerConnections.get(max).isChoked()) {
                            peerConnections.get(max).unchoke();
                            peerConnections.get(max).sendMessage(Constants.UNCHOKE);
                        }
                        preferredNeighbors[i] = max;
                        interestedPeers.remove(Integer.valueOf(max));
                    }
                    for (Integer peer : interestedPeers) {
                        if(!peerConnections.get(peer).isChoked() && !peerConnections.get(peer).isOptimisticallyUnchoked()){
                            peerConnections.get(peer).choke();
                            peerConnections.get(peer).sendMessage(Constants.CHOKE);
                        }
                    }
                }
            }
            logger.changePreferredNeighbors(selfPeer.getPeerID(), preferredNeighbors);
            try{
                Thread.sleep(common.getUnchokingInterval() * 1000);
            }
            catch(Exception e){
//                    e.printStackTrace();
            }
        }
        try{
            Thread.sleep(5000);
        }
        catch(Exception e){

        }
        System.exit(0);
    }
}