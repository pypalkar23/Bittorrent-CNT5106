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

            Message msg = new Message();
            commonDataStore.setMsg(msg);

            CommonConfig commonConfig = CommonConfigReader.loadFile();
            commonDataStore.setCommonConfig(commonConfig);

            Helpers.setBitfieldForPeer(commonDataStore);

            byte[][] filePieces = Helpers.getFilePieces(commonDataStore);
            commonDataStore.setFilePieces(filePieces);

            Map<Integer, ConnectionInfo> connectedPeers = new ConcurrentHashMap<>();
            commonDataStore.setConnections(connectedPeers);
            Helpers.startHelperThreads(commonDataStore);

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
