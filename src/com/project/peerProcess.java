package com.project;


import com.project.connectionUtils.*;
import com.project.logger.Logger;
import com.project.message.MessageUtil;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.parserutils.parsers.CommonConfigReader;
import com.project.parserutils.parsers.PeerConfigReader;
import com.project.utils.Helpers;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class peerProcess {
    private static int hostID;

    public static void main(String[] args) {
        CommonDataStore commonDataStore = new CommonDataStore();
        System.out.println("Starting Process");

        try {
            hostID = Integer.parseInt(args[0]);
            commonDataStore.setHostID(hostID);
        } catch (Exception e) {
            System.out.println("The hostID should be node ID that is mentioned in the PeerInfo.cfg file");
        }

        try {
            //Creating a file directory for the peer if it does not exist.
            File directory = new File("peer_" + hostID);
            if (!directory.exists()) {
                boolean isMade = directory.mkdir();
            }

            commonDataStore.setDirectory(directory);

            Logger logger = new Logger(hostID);
            commonDataStore.setLogger(logger);

            Map<Integer, PeerInfo> peers = PeerConfigReader.getConfiguration();
            commonDataStore.setPeers(peers);

            MessageUtil msg = new MessageUtil();
            commonDataStore.setMsg(msg);

            CommonConfig commonConfig = CommonConfigReader.loadFile();
            commonDataStore.setCommonConfig(commonConfig);
            logger.hasConfig(hostID, commonConfig.toString());

            Helpers.setBitfieldForPeer(commonDataStore);
            logger.hasBitfield(hostID, peers.get(hostID).getBitfield());

            byte[][] filePieces = Helpers.getFilePieces(commonDataStore);
            commonDataStore.setFilePieces(filePieces);

            Thread.sleep(2000L);

            Map<Integer, ConnectionInfo> connectedPeers = new ConcurrentHashMap<>();
            commonDataStore.setConnections(connectedPeers);
            Helpers.startHelperThreads(commonDataStore);

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
