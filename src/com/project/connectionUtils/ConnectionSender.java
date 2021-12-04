package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.message.Message;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;


public class ConnectionSender extends Thread{
    private final int hostID;
    private final File directory;
    private final byte[][] filePieces;
    private final Logger logger;
    private final Message msg;
    private final Map<Integer,PeerInfo> peers;
    private final Map<Integer, ConnectionInfo> connnectedPeers;
    private final CommonConfig commonConfig;
    private final CommonDataStore commonDataStore;

    public ConnectionSender(CommonDataStore commonDataStore){
        this.commonDataStore = commonDataStore;
        this.hostID = commonDataStore.getHostID();
        this.peers = commonDataStore.getPeers();
        this.connnectedPeers = commonDataStore.getConnections();
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
            for(int id : peers.keySet()){
                if(id == hostID)
                    break;
                else{
                    PeerInfo connPeer = peers.get(id);
                    Socket connection = new Socket(connPeer.getHostName(), connPeer.getPortNumber());
                    DataOutputStream opStream = new DataOutputStream(connection.getOutputStream());
                    opStream.flush();
                    opStream.write(msg.getHandshakeMessage(hostID));
                    opStream.flush();
                    DataInputStream ipStream = new DataInputStream(connection.getInputStream());
                    ipStream.readFully(buffer);
                    int peerID = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 28, 32)).getInt();
                    if(peerID != id)
                        connection.close();
                    else{
                        logger.hasMadeConnectionTo(hostID, id);
                        StringBuilder handshakeMsg = new StringBuilder();
                        handshakeMsg.append(new String(Arrays.copyOfRange(buffer, 0, 28)));
                        handshakeMsg.append(id);
                        System.out.println(handshakeMsg);
                        connnectedPeers.put(id, new ConnectionInfo(peers.get(hostID), connnectedPeers, peers, connection, id, msg, logger, commonConfig,directory,filePieces, commonDataStore));
                    }
                }
            }
        }
        catch(Exception e){
            //e.printStackTrace();
        }
    }
}
