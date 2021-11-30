package com.project.connectionUtils;

import com.project.logger.Logs;
import com.project.message.Messages;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;
import com.project.peerProcess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;


public class SendConnections extends Thread{
    private int hostID;
    private Map<Integer,PeerInfo> peers;
    private Map<Integer,PeerConnection> peerConnections;
    private Logs logs;
    private Messages msg;
    private CommonInfo commonInfo;
    private File directory;
    private byte[][] filePieces;
    private CommonData commonData;

    public SendConnections(int hostID, Map<Integer, PeerInfo> peers, Map<Integer, PeerConnection> peerConnections, Logs logs, Messages msg, CommonInfo commonInfo, File directory,byte[][] filePieces,CommonData commonData){
        this.hostID = hostID;
        this.peers = peers;
        this.peerConnections = peerConnections;
        this.logs = logs;
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
            for(int id : peers.keySet()){
                if(id == hostID)
                    break;
                else{
                    PeerInfo connPeer = peers.get(id);
                    Socket connection = new Socket(connPeer.getHostName(), connPeer.getPortNumber());
                    DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    dataOutputStream.flush();
                    dataOutputStream.write(msg.getHandshakeMessage(hostID));
                    dataOutputStream.flush();
                    DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                    dataInputStream.readFully(buffer);
                    int peerID = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 28, 32)).getInt();
                    if(peerID != id)
                        connection.close();
                    else{
                        logs.connectionTo(hostID, id);
                        StringBuilder handshakeMsg = new StringBuilder();
                        handshakeMsg.append(new String(Arrays.copyOfRange(buffer, 0, 28)));
                        handshakeMsg.append(peerID);
                        System.out.println(handshakeMsg);
                        peerConnections.put(id, new PeerConnection(peers.get(hostID),peerConnections, peers, connection, id, msg,logs,commonInfo,directory,filePieces,commonData));
                    }
                }
            }
        }
        catch(Exception e){
//                e.printStackTrace();
        }
    }
}
