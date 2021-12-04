package com.project.parserutils.dto;

import com.project.connectionUtils.ConnectionInfo;
import com.project.logger.Logger;
import com.project.message.Message;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//A Common Datastore to make required data accessible to all other classes
public class CommonDataStore {
    private final AtomicInteger completedPeers;
    private int hostID;
    private Map<Integer,PeerInfo> peers;
    private Map<Integer, ConnectionInfo> connections;
    private Logger logger;
    private Message msg;
    private CommonConfig commonConfig;
    private File directory;
    private byte[][] filePieces;

    public CommonDataStore(){
        completedPeers = new AtomicInteger(0);
    }

    public int getCompletedPeers(){
        return completedPeers.get();
    }

    public void incrementCompletedPeers(){
        while(true){
            int existingValue = getCompletedPeers();
            int newValue = existingValue+1;
            if(completedPeers.compareAndSet(existingValue,newValue)){
                return;
            }
        }
    }

    public int getHostID() {
        return hostID;
    }

    public void setHostID(int hostID) {
        this.hostID = hostID;
    }

    public Map<Integer, PeerInfo> getPeers() {
        return peers;
    }

    public void setPeers(Map<Integer, PeerInfo> peers) {
        this.peers = peers;
    }

    public Map<Integer, ConnectionInfo> getConnections() {
        return connections;
    }

    public void setConnections(Map<Integer, ConnectionInfo> connections) {
        this.connections = connections;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }

    public void setCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public byte[][] getFilePieces() {
        return filePieces;
    }

    public void setFilePieces(byte[][] filePieces) {
        this.filePieces = filePieces;
    }

}
