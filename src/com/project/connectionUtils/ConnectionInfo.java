package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.message.MessageUtil;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class ConnectionInfo {
    private final Socket connection;
    private boolean interested;
    private boolean choked;
    private boolean optimisticallyUnchoked;
    private double downloadRate;
    private final PeerInfo peer;
    private final int peerID;
    private final MessageUtil msg;
    private final Logger logger;
    private final File directory;
    private final byte[][] filePieces;
    private final CommonConfig commonConfig;
    private final CommonDataStore commonDataStore;


    public ConnectionInfo(CommonDataStore commonDataStore, Socket conn, int peerID) {
        choked = true;
        downloadRate = 0;
        this.connection = conn;
        this.peerID = peerID;
        this.logger = commonDataStore.getLogger();
        this.peer = commonDataStore.getPeers().get(commonDataStore.getHostID());
        this.msg = commonDataStore.getMsg();
        this.directory = commonDataStore.getDirectory();
        this.filePieces = commonDataStore.getFilePieces();
        this.commonConfig = commonDataStore.getCommonConfig();
        this.commonDataStore = commonDataStore;
        (new MessageReader(this, commonDataStore)).start();
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

    public void sendCommand(char type) {
        try {
            DataOutputStream opStream = new DataOutputStream(connection.getOutputStream());
            opStream.flush();
            switch (type) {
                case Constants.CHOKE_MSG:
                    opStream.write(msg.prepareChokeMessage());
                    break;
                case Constants.UNCHOKE_MSG:
                    opStream.write(msg.prepareUnchokeMessage());
                    break;
                case Constants.INTERESTED_MSG:
                    opStream.write(msg.prepareInterestedMessage());
                    break;
                case Constants.NOT_INTERESTED_MSG:
                    opStream.write(msg.prepareNotInterestedMessage());
                    break;
                case Constants.BITFIELD_MSG:
                    opStream.write(msg.prepareBitfieldMessage(peer.getBitfield()));
                    break;
                case Constants.EXIT_MSG:
                    opStream.write(msg.prepareExitMessage());
                    break;
                default:
                    break;
            }
            opStream.flush();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void sendFileRelatedMessage(char type, int index) {
        try {
            DataOutputStream opStream = new DataOutputStream(connection.getOutputStream());
            opStream.flush();
            switch (type) {
                case Constants.HAVE_MSG:
                    opStream.write(msg.prepareHaveMessage(index));
                    break;
                case Constants.REQUEST_MSG:
                    opStream.write(msg.prepareRequestMessage(index));
                    break;
                case Constants.PIECE_MSG:
                    opStream.write(msg.preparePieceMessage(index, filePieces[index]));
                    break;
                default:
                    break;
            }
            opStream.flush();
        } catch (Exception e) {
                //e.printStackTrace();
        }
    }

    public void compareBitfield(int[] peerBitFieldSelf, int[] peerBitFieldOther, int len) {
        int i;
        for (i = 0; i < len; i++) {
            if (peerBitFieldSelf[i] == 0 && peerBitFieldOther[i] == 1) {
                sendCommand(Constants.INTERESTED_MSG);
                break;
            }
        }
        if (i == len)
            sendCommand(Constants.NOT_INTERESTED_MSG);
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
            sendFileRelatedMessage(Constants.REQUEST_MSG, index);
        }
    }

    public void checkIfComplete() {
        int counter = 0;
        for (int bit : peer.getBitfield()) {
            if (bit == 1)
                counter++;
        }
        if (counter == peer.getBitfield().length) {
            logger.hasCompletedDownload(peer.getPeerID());
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
                System.out.println("\n\n******File Download Completed By This Peer*******\n\n");
                peer.setHaveFile(1);
                commonDataStore.incrementCompletedPeers();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }


}
