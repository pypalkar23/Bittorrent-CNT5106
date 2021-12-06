package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.message.MessageUtil;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class ConnectionReceiver extends Thread{
    int hostID;
    PeerInfo peer;
    Logger logger;
    MessageUtil msg;
    File directory;
    byte[][] filePieces;
    Map<Integer, ConnectionInfo> connectedPeers;
    Map<Integer, PeerInfo> peers;
    CommonConfig commonConfig;
    CommonDataStore commonDataStore;

    public ConnectionReceiver(CommonDataStore commonDataStore){
        this.commonDataStore = commonDataStore;
        this.hostID = commonDataStore.getHostID();
        this.peers = commonDataStore.getPeers();
        this.peer = this.peers.get(this.hostID);
        this.connectedPeers = commonDataStore.getConnections();
        this.logger = commonDataStore.getLogger();
        this.msg = commonDataStore.getMsg();
        this.commonConfig = commonDataStore.getCommonConfig();
        this.directory = commonDataStore.getDirectory();
        this.filePieces = commonDataStore.getFilePieces();
    }

    @Override
    public void run(){
        byte[] buffer = new byte[Constants.BYTE_SIZE];
        try{
            ServerSocket serverSocket = new ServerSocket(peer.getPortNumber());
            while(connectedPeers.size() < peers.size() - 1){
                Socket connection = serverSocket.accept();
                DataInputStream ipStream = new DataInputStream(connection.getInputStream());
                ipStream.readFully(buffer);
                int otherPeerID = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 28, 32)).getInt();
                logger.hasReceivedConnectionFrom(hostID, otherPeerID);
                StringBuilder handshakeMsg = new StringBuilder();
                handshakeMsg.append(new String(Arrays.copyOfRange(buffer, 0, 28)));
                handshakeMsg.append(otherPeerID);
                System.out.println(handshakeMsg);
                connectedPeers.put(otherPeerID, new ConnectionInfo(commonDataStore,connection, otherPeerID));
                DataOutputStream opStream = new DataOutputStream(connection.getOutputStream());
                opStream.flush();
                opStream.write(msg.prepareHandshakeMessage(hostID));
            }
        }
        catch(Exception e){
            //e.printStackTrace();
        }
    }
}