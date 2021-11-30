package com.project.parserutils.dto;

public class PeerInfo {
    private int peerID;
    private String hostName;
    private int portNumber;
    private int haveFile;
    private int[] bitfield;
    private int numOfPieces = 0;
    public PeerInfo(){

    }
    public PeerInfo (String peerId, String hostName,String portNumber,String haveFile){
         this.peerID = Integer.parseInt(peerId);
         this.hostName = hostName;
         this.portNumber = Integer.parseInt(portNumber);
         this.haveFile = Integer.parseInt(haveFile);
    }

    public void printBitfield(){
        for(int bit : bitfield)
            System.out.print(bit);
    }

    public int getNumOfPieces() {
        return numOfPieces;
    }

    public void updateNumOfPieces() {
        this.numOfPieces++;
        if(this.numOfPieces == bitfield.length)
            this.haveFile = 1;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public int getHaveFile() {
        return haveFile;
    }

    public void setHaveFile(int haveFile) {
        this.haveFile = haveFile;
    }

    public int[] getBitfield() {
        return bitfield;
    }

    public void setBitfield(int[] bitfield) {
        this.bitfield = bitfield;
    }

    public void updateBitfield(int index){
        bitfield[index] = 1;
    }
}
