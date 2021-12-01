package com.project;


import com.project.connectionUtils.*;
import com.project.logger.Logger;
import com.project.message.Messages;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;
import com.project.parserutils.parsers.CommonConfigReader;
import com.project.parserutils.parsers.PeerConfigReader;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class peerProcess {
    private static int hostID;
    private static Map<Integer, PeerInfo> peers;
    private static byte[][] filePieces;
    private static Messages msg = new Messages();
    private static File log_file;
    private static Logger logger;
    private static Map<Integer, PeerConnection> peerConnections;
    private static PeerInfo thisPeer;
    private static CommonInfo common;
    private static File directory;
    private static CommonData commonData;

    public static void main(String[] args) {
        commonData = new CommonData();
        System.out.println("Starting Process");
        hostID = Integer.parseInt(args[0]);
        try {
            /*
             *   Create the file directory corresponding to this peer.
             */
            directory = new File("peer_" + hostID);
            if (directory.exists() == false) {
                directory.mkdir();
            }


            logger = new Logger(hostID);

            peers = PeerConfigReader.getConfiguration();
            common = CommonConfigReader.loadFile();
            thisPeer = peers.get(hostID);
            int fileSize = common.getFileSize();
            int pieceSize = common.getPieceSize();
            int numOfPieces = (int) Math.ceil((double) fileSize / pieceSize);
            filePieces = new byte[numOfPieces][];
            int bitfieldSize = numOfPieces;
            int[] bitfield = new int[bitfieldSize];
            if (thisPeer.getHaveFile() == 1) {
                //completedPeers++;
                commonData.incrementCompletedPeers();
                Arrays.fill(bitfield, 1);
                thisPeer.setBitfield(bitfield);
                //Dividing File into pieces and storing them into array of pieces.
                BufferedInputStream file = new BufferedInputStream(new FileInputStream(directory.getAbsolutePath() + "/" + common.getFileName()));
                byte[] fileBytes = new byte[fileSize];
                file.read(fileBytes);
                file.close();
                int part = 0;

                for (int counter = 0; counter < fileSize; counter += pieceSize) {
                    //byte[] pieceBytes = Arrays.copyOfRange(fileBytes, counter, counter + pieceSize);
                    if (counter + pieceSize <= fileSize)
                        filePieces[part] = Arrays.copyOfRange(fileBytes, counter, counter + pieceSize);
                    else
                        filePieces[part] = Arrays.copyOfRange(fileBytes, counter, fileSize);
                    part++;
                    thisPeer.updateNumOfPieces();
                }
            } else {
                Arrays.fill(bitfield, 0);
                thisPeer.setBitfield(bitfield);
            }

            peerConnections = new ConcurrentHashMap<>();
            SendConnections sendConnections = new SendConnections(hostID, peers, peerConnections, logger, msg, common, directory, filePieces, commonData);
            sendConnections.start();
            ReceiveConnections receiveConnections = new ReceiveConnections(hostID, peers, peerConnections, logger, msg, common, directory, filePieces, commonData);
            receiveConnections.start();
            UnchokePeers unchokePeers = new UnchokePeers(peers, peerConnections, thisPeer, common, commonData, logger);
            unchokePeers.start();
            OptimisticUnchokePeer optimisticUnchokePeer = new OptimisticUnchokePeer(thisPeer, peers, peerConnections, common, commonData, logger);
            optimisticUnchokePeer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
