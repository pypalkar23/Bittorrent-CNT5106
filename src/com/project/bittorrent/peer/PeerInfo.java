package com.project.bittorrent.peer;

import com.project.ParserUtils.DTO.CommonConfigDetails;
import com.project.ParserUtils.DTO.PeerConnectionDetails;
import com.project.ParserUtils.Parsers.CommonConfigReader;
import com.project.ParserUtils.Parsers.PeerConfigReader;

import java.util.ArrayList;

public class PeerInfo {
    private static volatile PeerInfo peerInfo;
    private  int preferredNeighbours;
    private  int unChokingInterval;
    private  int optimisticUnChokingInterval;
    private  String fileName;
    private  int fileSize;
    private  int pieceSize;
    private  int pieceCount;
    private  int peerID;
    private  int port;

    private ArrayList<PeerConnectionDetails> activePeerConnections;
    //full list of PeersFrom File
    private ArrayList<PeerConnectionDetails> totalPeerConnections;
    private ArrayList<Node> nodes;

    private PeerInfo() {}

    public int getPreferredNeighbours() {
        return preferredNeighbours;
    }

    public void setPreferredNeighbours(int preferredNeighbours) {
        this.preferredNeighbours = preferredNeighbours;
    }

    public int getUnChokingInterval() {
        return unChokingInterval;
    }

    public void setUnChokingInterval(int unChokingInterval) {
        this.unChokingInterval = unChokingInterval;
    }

    public int getOptimisticUnChokingInterval() {
        return optimisticUnChokingInterval;
    }

    public void setOptimisticUnChokingInterval(int optimisticUnChokingInterval) {
        this.optimisticUnChokingInterval = optimisticUnChokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public void setPieceCount(int pieceCount) {
        this.pieceCount = pieceCount;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public synchronized ArrayList<PeerConnectionDetails> getActivePeerConnections() {
        return activePeerConnections;
    }

    public synchronized void setActivePeerConnections(PeerConnectionDetails peerConnectionDetails) {
        if(this.getActivePeerConnections() == null)
            this.activePeerConnections = new ArrayList<>();

        this.activePeerConnections.add(peerConnectionDetails);

    }

    public ArrayList<PeerConnectionDetails> getTotalPeerConnections() {
        return totalPeerConnections;
    }

    public void setTotalPeerConnections(PeerConnectionDetails peerConnectionInfo) {
        if(this.totalPeerConnections == null)
            this.totalPeerConnections = new ArrayList<>();

        this.totalPeerConnections.add(peerConnectionInfo);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }


    public void setPeerNodes(Node node) {
        if(this.nodes == null)
            this.nodes = new ArrayList<>();

        this.nodes.add(node);
    }

    public static PeerInfo getInstance(){

        if(peerInfo == null){
            synchronized (PeerInfo.class){
                if(peerInfo == null){
                    peerInfo = new PeerInfo();
                    peerInfo.setDataFromConfig();
                }
            }
        }
        return peerInfo;
    }

    private void setDataFromConfig(){
        CommonConfigDetails commonConfigDetails = CommonConfigReader.loadFile();
        this.setPreferredNeighbours(commonConfigDetails.getNumberOfPreferredNeighbors());
        this.setUnChokingInterval(commonConfigDetails.getUnchokingInterval());
        this.setOptimisticUnChokingInterval(commonConfigDetails.getOptimisticUnchokingInterval());
        this.setFileName(commonConfigDetails.getFileName());
        this.setFileSize(commonConfigDetails.getFileSize());
        this.setPieceSize(commonConfigDetails.getChunkSize());
        this.setPieceCount((this.fileSize / this.pieceSize) + (this.fileSize % this.pieceSize == 0 ? 0 : 1));
    }

    public void setPeerConnectionInfo(){
        this.totalPeerConnections = PeerConfigReader.getConfiguration();
        for(PeerConnectionDetails peerConnectionInfo: totalPeerConnections){
            if(this.getPeerID() == peerConnectionInfo.getId()){
                this.setPort(peerConnectionInfo.getPort());
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "preferredNeighbours=" + preferredNeighbours +
                ", unChokingInterval=" + unChokingInterval +
                ", optimisticUnChokingInterval=" + optimisticUnChokingInterval +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", pieceSize=" + pieceSize +
                ", pieceCount=" + pieceCount +
                ", peerID=" + peerID +
                ", port=" + port +
                '}';
    }
}
