package com.project.connectionUtils;

import com.project.logger.Logger;
import com.project.parserutils.dto.CommonDataStore;
import com.project.parserutils.dto.CommonConfig;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class MessageReader extends Thread{
    private final byte[][] filePieces;
    private final ConnectionInfo connectionInfo;
    private final Logger logger;
    private final PeerInfo peerSelf;
    private final CommonConfig commonConfig;
    private final CommonDataStore commonDataStore;
    private final Map<Integer,PeerInfo> peers;
    private final Map<Integer, ConnectionInfo> connectedPeers;

    public MessageReader(ConnectionInfo connectionInfo, CommonDataStore commonDataStore){
        this.connectionInfo = connectionInfo;
        this.commonDataStore = commonDataStore;
        this.peers = commonDataStore.getPeers();
        this.peerSelf = commonDataStore.getPeers().get(commonDataStore.getHostID());
        this.filePieces = commonDataStore.getFilePieces();
        this.logger = commonDataStore.getLogger();
        this.commonConfig = commonDataStore.getCommonConfig();
        this.connectedPeers = commonDataStore.getConnections();
    }

    @Override
    public void run(){
        double startTime;
        double endTime;
        synchronized (this)
        {
            try{
                DataInputStream ipStream = new DataInputStream(connectionInfo.getConnection().getInputStream());
                connectionInfo.sendCommand(Constants.BITFIELD_MSG);
                while(commonDataStore.getCompletedPeers() < peers.size()){
                    int msgLength = ipStream.readInt();
                    byte[] buffer = new byte[msgLength];
                    startTime = (double)System.nanoTime() / Constants.NANOSECOND_DIVISOR;
                    ipStream.readFully(buffer);
                    endTime = (double)System.nanoTime() / Constants.NANOSECOND_DIVISOR;
                    char msgType = (char)buffer[0];
                    byte[] msg = new byte[msgLength - 1];
                    int counter = 0;
                    for(int i = 1; i < msgLength; i++){
                        msg[counter] = buffer[i];
                        counter++;
                    }
                    int index;
                    int bits;
                    switch (msgType){
                        case Constants.EXIT_MSG:
                            //System.out.println("****Exit Msg Received*****");
                            System.exit(0);
                            break;
                        case Constants.CHOKE_MSG:
                            logger.hasBeenChokedBy(peerSelf.getPeerID(), connectionInfo.getPeerID());
                            connectionInfo.choke();
                            break;
                        case Constants.UNCHOKE_MSG:
                            connectionInfo.unchoke();
                            logger.hasBeenUnchokedBy(peerSelf.getPeerID(), connectionInfo.getPeerID());
                            connectionInfo.getPieceIndex(peerSelf.getBitfield(), peers.get(connectionInfo.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            break;
                        case Constants.INTERESTED_MSG:
                            logger.hasReceivedInterestedMsg(peerSelf.getPeerID(), connectionInfo.getPeerID());
                            connectionInfo.setInterested();
                            break;
                        case Constants.NOT_INTERESTED_MSG:
                            logger.hasReceivedNotInterestedMsg(peerSelf.getPeerID(), connectionInfo.getPeerID());
                            connectionInfo.setNotInterested();
                            if(!connectionInfo.isChoked()){
                                connectionInfo.choke();
                                connectionInfo.sendCommand(Constants.CHOKE_MSG);
                            }
                            break;
                        case Constants.HAVE_MSG:
                            index = ByteBuffer.wrap(msg).getInt();
                            peers.get(connectionInfo.getPeerID()).updateBitfield(index);
                            bits = 0;
                            for(int x : peers.get(connectionInfo.getPeerID()).getBitfield()){
                                if(x == 1)
                                    bits++;
                            }
                            if(bits == peerSelf.getBitfield().length){
                                peers.get(connectionInfo.getPeerID()).setHaveFile(1);
                                //completedPeers++;
                                commonDataStore.incrementCompletedPeers();
                            }
                            connectionInfo.compareBitfield(peerSelf.getBitfield(), peers.get(connectionInfo.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            logger.hasReceivedHaveMsg(peerSelf.getPeerID(), connectionInfo.getPeerID(), index);
                            break;
                        case Constants.BITFIELD_MSG:
                            int[] bitfield = new int[msg.length/4];
                            counter = 0;
                            for(int i = 0; i < msg.length; i += 4){
                                bitfield[counter] = ByteBuffer.wrap(Arrays.copyOfRange(msg, i, i + 4)).getInt();
                                counter++;
                            }
                            peers.get(connectionInfo.getPeerID()).setBitfield(bitfield);
                            bits = 0;
                            for(int x : peers.get(connectionInfo.getPeerID()).getBitfield()){
                                if(x == 1)
                                    bits++;
                            }
                            if(bits == peerSelf.getBitfield().length){
                                peers.get(connectionInfo.getPeerID()).setHaveFile(1);
                                commonDataStore.incrementCompletedPeers();
                            }
                            else{
                                peers.get(connectionInfo.getPeerID()).setHaveFile(0);
                            }
                            connectionInfo.compareBitfield(peerSelf.getBitfield(), bitfield, bitfield.length);
                            break;
                        case Constants.REQUEST_MSG:
                            connectionInfo.sendFileRelatedMessage(Constants.PIECE_MSG, ByteBuffer.wrap(msg).getInt());
                            break;
                        case Constants.PIECE_MSG:
                            index = ByteBuffer.wrap(Arrays.copyOfRange(msg, 0, 4)).getInt();
                            counter = 0;
                            filePieces[index] = new byte[msg.length - 4];
                            for(int i = 4; i < msg.length; i++){
                                filePieces[index][counter] = msg[i];
                                counter++;
                            }
                            peerSelf.updateBitfield(index);
                            peerSelf.updateNumOfPieces();
                            if(!connectionInfo.isChoked()){
                                connectionInfo.getPieceIndex(peerSelf.getBitfield(), peers.get(connectionInfo.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            }
                            double rate = ((double)(msg.length + 5) / (endTime - startTime));
                            if(peers.get(connectionInfo.getPeerID()).getHaveFile() == 1){
                                connectionInfo.setDownloadRate(-1);
                            }
                            else{
                                connectionInfo.setDownloadRate(rate);
                            }
                            logger.hasDownloadedMsg(peerSelf.getPeerID(), connectionInfo.getPeerID(), index, peerSelf.getNumOfPieces());
                            int downlaadPercentage = (peerSelf.getNumOfPieces() * 100) / (int)Math.ceil((double) commonConfig.getFileSize()/ commonConfig.getPieceSize());
                            connectionInfo.checkIfComplete();
                            for(int connection : connectedPeers.keySet()){
                                connectedPeers.get(connection).sendFileRelatedMessage(Constants.HAVE_MSG, index);
                            }
                            break;
                        default:
                            break;
                    }
                }
                if(peers.get(peerSelf.getPeerID()).getHaveFile() == 1){
                    for(int connection : connectedPeers.keySet()){
                        connectedPeers.get(connection).sendCommand(Constants.EXIT_MSG);
                    }
                }
                Thread.sleep(1000);
                System.exit(0);
            }
            catch(Exception e){
                //e.printStackTrace();
            }
        }
    }
}