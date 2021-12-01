package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.message.Messages;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PeerConnection {
    private Socket connection;
    private int peerID;
    private boolean interested = false;
    private boolean choked = true;
    private boolean optimisticallyUnchoked = false;
    private double downloadRate = 0;
    private Messages msg;
    private PeerInfo peer;
    private Logger logger;
    private CommonInfo commonInfo;
    private File directory;
    private byte[][] filePieces;
    private CommonData commonData;
    Map<Integer,PeerInfo> peers;

    public PeerConnection(PeerInfo peer, Map<Integer,PeerConnection> peerConnections, Map<Integer,PeerInfo> peers, Socket conn, int id, Messages msg, Logger logger, CommonInfo commonInfo, File directory, byte[][] filePieces, CommonData commonData) {
        this.peer = peer;
        this.connection = conn;
        this.peerID = id;
        this.msg = msg;
        this.logger = logger;
        this.commonInfo = commonInfo;
        this.directory = directory;
        this.filePieces = filePieces;
        this.commonData = commonData;
        this.peers = peers;
        (new ReaderThread(this,peerConnections,peer,peers, logger,filePieces,commonInfo,commonData)).start();
    }
    public double getDownloadRate() {
        return downloadRate;
    }

    public void setDownloadRate(double rate) {
        this.downloadRate = rate;
    }

    public boolean isOptimisticallyUnchoked() {
        return optimisticallyUnchoked;
    }

    public void optimisticallyUnchoke() {
        optimisticallyUnchoked = true;
    }

    public void optimisticallyChoke() {
        optimisticallyUnchoked = false;
    }

    public boolean isInterested() {
        return interested;
    }

    public void setInterested() {
        interested = true;
    }

    public void setNotInterested() {
        interested = false;
    }

    public boolean isChoked() {
        return choked;
    }

    public void choke() {
        choked = true;
    }

    public void unchoke() {
        choked = false;
    }

    public int getPeerID() {
        return peerID;
    }

    public Socket getConnection() {
        return connection;
    }



    public void sendMessage(char type) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.flush();
            switch (type) {
                case Constants.CHOKE:
                    dataOutputStream.write(msg.getChokeMessage());
                    break;
                case Constants.UNCHOKE:
                    dataOutputStream.write(msg.getUnchokeMessage());
                    break;
                case Constants.INTERESTED:
                    dataOutputStream.write(msg.getInterestedMessage());
                    break;
                case Constants.NOT_INTERESTED:
                    dataOutputStream.write(msg.getNotInterestedMessage());
                    break;
                case Constants.BITFIELD:
                    dataOutputStream.write(msg.getBitfieldMessage(peer.getBitfield()));
                    break;
                default:
                    break;
            }
            dataOutputStream.flush();
        } catch (Exception e) {
//                e.printStackTrace();
        }
    }

    public void sendMessage(char type, int index) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.flush();
            switch (type) {
                case Constants.HAVE:
                    dataOutputStream.write(msg.getHaveMessage(index));
                    break;
                case Constants.REQUEST:
                    dataOutputStream.write(msg.getRequestMessage(index));
                    break;
                case Constants.PIECE:
                    dataOutputStream.write(msg.getPieceMessage(index, filePieces[index]));
                    break;
                default:
                    break;
            }
            dataOutputStream.flush();
        } catch (Exception e) {
//                e.printStackTrace();
        }
    }

    public void compareBitfield(int[] thisPeerBitfield, int[] connectedPeerBitfield, int len) {
        int i;
        for (i = 0; i < len; i++) {
            if (thisPeerBitfield[i] == 0 && connectedPeerBitfield[i] == 1) {
                sendMessage(Constants.INTERESTED);
                break;
            }
        }
        if (i == len)
            sendMessage(Constants.NOT_INTERESTED);
    }

    public void getPieceIndex(int[] thisPeerBitfield, int[] connectedPeerBitfield, int len) {
        ArrayList<Integer> indices = new ArrayList<>();
        int i;
        for (i = 0; i < len; i++) {
            if (thisPeerBitfield[i] == 0 && connectedPeerBitfield[i] == 1) {
                indices.add(i);
            }
        }
        Random r = new Random();
        if (indices.size() > 0) {
            int index = indices.get(Math.abs(r.nextInt() % indices.size()));
            sendMessage(Constants.REQUEST, index);
        }
    }

    public void checkCompleted() {
        int counter = 0;
        for (int bit : peer.getBitfield()) {
            if (bit == 1)
                counter++;
        }
        if (counter == peer.getBitfield().length) {
            logger.downloadCompleted(peer.getPeerID());
            counter = 0;
            byte[] merge = new byte[commonInfo.getFileSize()];
            for (byte[] piece : filePieces) {
                for (byte b : piece) {
                    merge[counter] = b;
                    counter++;
                }
            }
            try {
                FileOutputStream file = new FileOutputStream(directory.getAbsolutePath() + "/" + commonInfo.getFileName());
                BufferedOutputStream bos = new BufferedOutputStream(file);
                bos.write(merge);
                bos.close();
                file.close();
                System.out.println("File Download Completed.");
                peer.setHaveFile(1);
                //completedPeers++
                commonData.incrementCompletedPeers();
            } catch (IOException e) {
//                    e.printStackTrace();
            }
        }
    }


}
