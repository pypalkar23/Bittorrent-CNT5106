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
        int pieceCount = (int) Math.ceil((double) fileSize / pieceSize);
        int bitMap[] = new int[pieceCount];
        PeerInfo thisPeer = commonDataStore.getPeers().get(peerID);
        if (thisPeer.getHaveFile() == 1) {
            commonDataStore.incrementCompletedPeers();
            Arrays.fill(bitMap, 1);
            thisPeer.setBitfield(bitMap);
        } else {
            Arrays.fill(bitMap, 0);
            thisPeer.setBitfield(bitMap);
        }
    }

    public static byte[][] getFilePieces(CommonDataStore commonDataStore){
        int peerID = commonDataStore.getHostID();
        int fileSize = commonDataStore.getCommonConfig().getFileSize();
        int pieceSize = commonDataStore.getCommonConfig().getPieceSize();
        int pieceCount = (int) Math.ceil((double) fileSize / pieceSize);
        PeerInfo thisPeer = commonDataStore.getPeers().get(peerID);
        byte[][] fileMap = new byte[pieceCount][];
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
                        fileMap[part] = Arrays.copyOfRange(fileBytes, counter, counter + pieceSize);
                    else
                        fileMap[part] = Arrays.copyOfRange(fileBytes, counter, fileSize);
                    part++;
                    thisPeer.updateNumOfPieces();
                }
            }catch(Exception e){
                //e.printStackTrace();
            }
        }
        return fileMap;
    }

    public static void startHelperThreads(CommonDataStore commonDataStore){
        ConnectionSender connectionSender = new ConnectionSender(commonDataStore);
        connectionSender.start();

        ConnectionReceiver connectionReceiver = new ConnectionReceiver(commonDataStore);
        connectionReceiver.start();

        PeerUnchokeScheduler peerUnchokeScheduler = new PeerUnchokeScheduler(commonDataStore);
        peerUnchokeScheduler.start();

        OptimisticUnchokeScheduler optimisticUnchokeScheduler = new OptimisticUnchokeScheduler(commonDataStore);
        optimisticUnchokeScheduler.start();
    }
}
