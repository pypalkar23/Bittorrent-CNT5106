package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.message.Message;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ConnectionInfo {
    private final Socket connection;
    private final int peerID;
    private boolean interested;
    private boolean choked;
    private boolean optimisticallyUnchoked;
    private double downloadRate;
    private final Message msg;
    private final PeerInfo peer;
    private final Logger logger;
    private final CommonConfig commonConfig;
    private final File directory;
    private final byte[][] filePieces;
    private final CommonDataStore commonDataStore;
    private Map<Integer,PeerInfo> peers;

    public ConnectionInfo(PeerInfo peer, Map<Integer, ConnectionInfo> peerConnections, Map<Integer,PeerInfo> peers, Socket conn, int peerID, Message msg, Logger logger, CommonConfig commonConfig, File directory, byte[][] filePieces, CommonDataStore commonDataStore) {
        this.commonDataStore = commonDataStore;
        this.connection = conn;
        this.peerID = peerID;
        this.msg = msg;
        this.logger = logger;
        this.commonConfig = commonConfig;
        this.directory = directory;
        this.filePieces = filePieces;
        this.peers = peers;
        this.peer = peer;
        choked = true;
        downloadRate = 0;
        (new ReaderThread(this,peerConnections,peer,peers, logger,filePieces, commonConfig, commonDataStore)).start();
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
            DataOutputStream opStream = new DataOutputStream(connection.getOutputStream());
            opStream.flush();
            switch (type) {
                case Constants.CHOKE:
                    opStream.write(msg.getChokeMessage());
                    break;
                case Constants.UNCHOKE:
                    opStream.write(msg.getUnchokeMessage());
                    break;
                case Constants.INTERESTED:
                    opStream.write(msg.getInterestedMessage());
                    break;
                case Constants.NOT_INTERESTED:
                    opStream.write(msg.getNotInterestedMessage());
                    break;
                case Constants.BITFIELD:
                    opStream.write(msg.getBitfieldMessage(peer.getBitfield()));
                    break;
                default:
                    break;
            }
            opStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(char type, int index) {
        try {
            DataOutputStream opStream = new DataOutputStream(connection.getOutputStream());
            opStream.flush();
            switch (type) {
                case Constants.HAVE:
                    opStream.write(msg.getHaveMessage(index));
                    break;
                case Constants.REQUEST:
                    opStream.write(msg.getRequestMessage(index));
                    break;
                case Constants.PIECE:
                    opStream.write(msg.getPieceMessage(index, filePieces[index]));
                    break;
                default:
                    break;
            }
            opStream.flush();
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

    public void compareBitfield(int[] peerBitFieldSelf, int[] peerBitFieldOther, int len) {
        int i;
        for (i = 0; i < len; i++) {
            if (peerBitFieldSelf[i] == 0 && peerBitFieldOther[i] == 1) {
                sendMessage(Constants.INTERESTED);
                break;
            }
        }
        if (i == len)
            sendMessage(Constants.NOT_INTERESTED);
    }

    public void getPieceIndex(int[] peerBitFieldSelf, int[] peerBitFieldOther, int len) {
        ArrayList<Integer> indices = new ArrayList<>();
        int i;
        for (i = 0; i < len; i++) {
            if (peerBitFieldSelf[i] == 0 && peerBitFieldOther[i] == 1) {
                indices.add(i);
            }
        }
        Random r = new Random();
        if (indices.size() > 0) {
            int index = indices.get(Math.abs(r.nextInt() % indices.size()));
            sendMessage(Constants.REQUEST, index);
        }
    }

    public void checkIfComplete() {
        int counter = 0;
        for (int bit : peer.getBitfield()) {
            if (bit == 1)
                counter++;
        }
        if (counter == peer.getBitfield().length) {
            logger.downloadCompleted(peer.getPeerID());
            counter = 0;
            byte[] merge = new byte[commonConfig.getFileSize()];
            for (byte[] piece : filePieces) {
                for (byte b : piece) {
                    merge[counter] = b;
                    counter++;
                }
            }
            try {
                FileOutputStream file = new FileOutputStream(directory.getAbsolutePath() + "/" + commonConfig.getFileName());
                BufferedOutputStream bos = new BufferedOutputStream(file);
                bos.write(merge);
                bos.close();
                file.close();
                //System.out.println("File Download Completed.");
                peer.setHaveFile(1);
                commonDataStore.incrementCompletedPeers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
