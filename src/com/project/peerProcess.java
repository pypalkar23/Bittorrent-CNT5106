package com.project;


import com.project.connectionUtils.*;
import com.project.logger.Logger;
import com.project.message.Message;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.parserutils.parsers.CommonConfigReader;
import com.project.parserutils.parsers.PeerConfigReader;
import com.project.utils.Helpers;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class peerProcess {
    private static int hostID;
    private static Map<Integer, PeerInfo> peers;
    private static byte[][] filePieces;
    private static Logger logger;
    private static Map<Integer, ConnectionInfo> peerConnections;
    private static CommonConfig commonConfig;
    private static File directory;
    private static CommonDataStore commonDataStore;

    public static void main(String[] args) {
        commonDataStore = new CommonDataStore();
        System.out.println("Starting Process");

        try {
            hostID = Integer.parseInt(args[0]);
            commonDataStore.setHostID(hostID);
        } catch (NumberFormatException e) {
            System.out.println("The hostID should be node ID that is mentioned in the PeerInfo.cfg file");
        } catch (Exception e) {
            System.out.println("The hostID should be node ID that is mentioned in the PeerInfo.cfg file");
        }

        try {
            /*
             *   Create the file directory corresponding to this peer.
             */
            directory = new File("peer_" + hostID);
            if (directory.exists() == false) {
                directory.mkdir();
            }

            commonDataStore.setDirectory(directory);

            logger = new Logger(hostID);
            commonDataStore.setLogger(logger);

            peers = PeerConfigReader.getConfiguration();
            commonDataStore.setPeers(peers);

            Message msg = new Message();
            commonDataStore.setMsg(msg);

            commonConfig = CommonConfigReader.loadFile();
            commonDataStore.setCommonConfig(commonConfig);

            Helpers.setBitfieldForPeer(commonDataStore);

            filePieces = Helpers.getFilePieces(commonDataStore);
            commonDataStore.setFilePieces(filePieces);

            peerConnections = new ConcurrentHashMap<>();
            commonDataStore.setConnections(peerConnections);
            Helpers.startHelperThreads(commonDataStore);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
