package com.project.utils;

import com.project.connectionUtils.OptimisticUnchokeScheduler;
import com.project.connectionUtils.ConnectionReceiver;
import com.project.connectionUtils.ConnectionSender;
import com.project.connectionUtils.PeerUnchokeScheduler;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.PeerInfo;

import java.io.*;
import java.util.Arrays;

public class Helpers {
    public static void setBitfieldForPeer(CommonDataStore commonDataStore){
        int peerID = commonDataStore.getHostID();
        int fileSize = commonDataStore.getCommonConfig().getFileSize();
        int pieceSize = commonDataStore.getCommonConfig().getPieceSize();
        int numOfPieces = (int) Math.ceil((double) fileSize / pieceSize);
        int bitfield[] = new int[numOfPieces];
        PeerInfo thisPeer = commonDataStore.getPeers().get(peerID);
        if (thisPeer.getHaveFile() == 1) {
            commonDataStore.incrementCompletedPeers();
            Arrays.fill(bitfield, 1);
            thisPeer.setBitfield(bitfield);
        } else {
            Arrays.fill(bitfield, 0);
            thisPeer.setBitfield(bitfield);
        }
    }

    public static byte[][] getFilePieces(CommonDataStore commonDataStore) throws IOException {
        int peerID = commonDataStore.getHostID();
        int fileSize = commonDataStore.getCommonConfig().getFileSize();
        int pieceSize = commonDataStore.getCommonConfig().getPieceSize();
        int numOfPieces = (int) Math.ceil((double) fileSize / pieceSize);
        PeerInfo thisPeer = commonDataStore.getPeers().get(peerID);
        byte[][] filePieces = new byte[numOfPieces][];
        File directory = commonDataStore.getDirectory();

        if (thisPeer.getHaveFile() == 1) {
            try {
                BufferedInputStream file = new BufferedInputStream(new FileInputStream(directory.getAbsolutePath() + File.separator + commonDataStore.getCommonConfig().getFileName()));
                byte[] fileBytes = new byte[fileSize];
                file.read(fileBytes);
                file.close();
                int part = 0;
                for (int counter = 0; counter < fileSize; counter += pieceSize) {
                    if (counter + pieceSize <= fileSize)
                        filePieces[part] = Arrays.copyOfRange(fileBytes, counter, counter + pieceSize);
                    else
                        filePieces[part] = Arrays.copyOfRange(fileBytes, counter, fileSize);
                    part++;
                    thisPeer.updateNumOfPieces();
                }
            }catch(Exception e){
                //e.printStackTrace();
            }
        }
        return filePieces;
    }

    public static void startHelperThreads(CommonDataStore commonDataStore){
        ConnectionSender sendConnections = new ConnectionSender(commonDataStore);
        sendConnections.start();

        ConnectionReceiver connectionReceiver = new ConnectionReceiver(commonDataStore);
        connectionReceiver.start();

        PeerUnchokeScheduler peerUnchokeScheduler = new PeerUnchokeScheduler(commonDataStore);
        peerUnchokeScheduler.start();

        OptimisticUnchokeScheduler optimisticUnchokeScheduler = new OptimisticUnchokeScheduler(commonDataStore);
        optimisticUnchokeScheduler.start();
    }
}
