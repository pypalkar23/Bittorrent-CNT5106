package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.message.Messages;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class ReceiveConnections extends Thread{
    PeerInfo peer;
    Map<Integer,PeerConnection> peerConnections;
    Map<Integer, PeerInfo> peers;
    Logger logger;
    Messages msg;
    int hostID;
    CommonInfo commonInfo;
    File directory;
    byte[][] filePieces;
    CommonData commonData;

    public ReceiveConnections(int hostID, Map<Integer, PeerInfo> peers, Map<Integer,PeerConnection> peerConnections, Logger logger, Messages msg, CommonInfo commonInfo, File directory, byte[][] filePieces, CommonData commonData){
        this.hostID = hostID;
        this.peers = peers;
        this.peer = peers.get(hostID);
        this.peerConnections = peerConnections;
        this.logger = logger;
        this.msg = msg;
        this.commonInfo = commonInfo;
        this.directory = directory;
        this.filePieces = filePieces;
        this.commonData = commonData;
    }

    @Override
    public void run(){
        byte[] buffer = new byte[32];
        try{
            ServerSocket serverSocket = new ServerSocket(peer.getPortNumber());
            while(peerConnections.size() < peers.size() - 1){
                Socket connection = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                dataInputStream.readFully(buffer);
                int peerID = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 28, 32)).getInt();
                logger.connectionFrom(hostID, peerID);
                StringBuilder handshakeMsg = new StringBuilder();
                handshakeMsg.append(new String(Arrays.copyOfRange(buffer, 0, 28)));
                handshakeMsg.append(peerID);
                System.out.println(handshakeMsg);
                peerConnections.put(peerID, new PeerConnection(peer, peerConnections, peers, connection, peerID, msg, logger,commonInfo,directory,filePieces,commonData));
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.flush();
                dataOutputStream.write(msg.getHandshakeMessage(hostID));
            }
        }
        catch(Exception e){
//                e.printStackTrace();
        }
    }
}