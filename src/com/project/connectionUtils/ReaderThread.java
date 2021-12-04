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

public class ReaderThread extends Thread{
    private final byte[][] filePieces;
    private final ConnectionInfo peer;
    private final Logger logger;
    private final PeerInfo peerSelf;
    private final CommonConfig commonConfig;
    private final CommonDataStore commonDataStore;
    private final Map<Integer,PeerInfo> peers;
    private final Map<Integer, ConnectionInfo> peerConnections;

    public ReaderThread(ConnectionInfo peer, Map<Integer, ConnectionInfo> peerConnections, PeerInfo peerSelf, Map<Integer,PeerInfo> peers, Logger logger, byte[][] filePieces, CommonConfig commonConfig, CommonDataStore commonDataStore){
        this.peer = peer;
        this.peerSelf = peerSelf;
        this.peers = peers;
        this.filePieces = filePieces;
        this.logger = logger;
        this.commonConfig = commonConfig;
        this.commonDataStore = commonDataStore;
        this.peerConnections = peerConnections;
    }

    @Override
    public void run(){
        double startTime;
        double endTime;
        synchronized (this)
        {
            try{
                DataInputStream ipStream = new DataInputStream(peer.getConnection().getInputStream());
                peer.sendMessage(Constants.BITFIELD);
                while(commonDataStore.getCompletedPeers() /*completedPeers*/ < peers.size()){
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
                        case Constants.CHOKE:
                            logger.choked(peerSelf.getPeerID(), peer.getPeerID());
                            peer.choke();
                            break;
                        case Constants.UNCHOKE:
                            peer.unchoke();
                            logger.unchoked(peerSelf.getPeerID(), peer.getPeerID());
                            peer.getPieceIndex(peerSelf.getBitfield(), peers.get(peer.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            break;
                        case Constants.INTERESTED:
                            logger.receiveInterested(peerSelf.getPeerID(), peer.getPeerID());
                            peer.setInterested();
                            break;
                        case Constants.NOT_INTERESTED:
                            logger.receiveNotInterested(peerSelf.getPeerID(), peer.getPeerID());
                            peer.setNotInterested();
                            if(!peer.isChoked()){
                                peer.choke();
                                peer.sendMessage(Constants.CHOKE);
                            }
                            break;
                        case Constants.HAVE:
                            index = ByteBuffer.wrap(msg).getInt();
                            peers.get(peer.getPeerID()).updateBitfield(index);
                            bits = 0;
                            for(int x : peers.get(peer.getPeerID()).getBitfield()){
                                if(x == 1)
                                    bits++;
                            }
                            if(bits == peerSelf.getBitfield().length){
                                peers.get(peer.getPeerID()).setHaveFile(1);
                                //completedPeers++;
                                commonDataStore.incrementCompletedPeers();
                            }
                            peer.compareBitfield(peerSelf.getBitfield(), peers.get(peer.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            logger.receiveHave(peerSelf.getPeerID(), peer.getPeerID(), index);
                            break;
                        case Constants.BITFIELD:
                            int[] bitfield = new int[msg.length/4];
                            counter = 0;
                            for(int i = 0; i < msg.length; i += 4){
                                bitfield[counter] = ByteBuffer.wrap(Arrays.copyOfRange(msg, i, i + 4)).getInt();
                                counter++;
                            }
                            peers.get(peer.getPeerID()).setBitfield(bitfield);
                            bits = 0;
                            for(int x : peers.get(peer.getPeerID()).getBitfield()){
                                if(x == 1)
                                    bits++;
                            }
                            if(bits == peerSelf.getBitfield().length){
                                peers.get(peer.getPeerID()).setHaveFile(1);
                                //completedPeers++;
                                commonDataStore.incrementCompletedPeers();
                            }
                            else{
                                peers.get(peer.getPeerID()).setHaveFile(0);
                            }
                            peer.compareBitfield(peerSelf.getBitfield(), bitfield, bitfield.length);
                            break;
                        case Constants.REQUEST:
                            peer.sendMessage(Constants.PIECE, ByteBuffer.wrap(msg).getInt());
                            break;
                        case Constants.PIECE:
                            index = ByteBuffer.wrap(Arrays.copyOfRange(msg, 0, 4)).getInt();
                            counter = 0;
                            filePieces[index] = new byte[msg.length - 4];
                            for(int i = 4; i < msg.length; i++){
                                filePieces[index][counter] = msg[i];
                                counter++;
                            }
                            peerSelf.updateBitfield(index);
                            peerSelf.updateNumOfPieces();
                            if(!peer.isChoked()){
                                peer.getPieceIndex(peerSelf.getBitfield(), peers.get(peer.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            }
                            double rate = ((double)(msg.length + 5) / (endTime - startTime));
                            if(peers.get(peer.getPeerID()).getHaveFile() == 1){
                                peer.setDownloadRate(-1);
                            }
                            else{
                                peer.setDownloadRate(rate);
                            }
                            logger.downloadingPiece(peerSelf.getPeerID(), peer.getPeerID(), index, peerSelf.getNumOfPieces());
                            int downloaded = (peerSelf.getNumOfPieces() * 100) / (int)Math.ceil((double) commonConfig.getFileSize()/ commonConfig.getPieceSize());
                            StringBuffer sb = new StringBuffer();
                            sb.append("\r").append("Downloaded: ");
                            sb.append(downloaded).append("%").append(" Number of Pieces: ").append(peerSelf.getNumOfPieces());
                            System.out.print(sb);
                            peer.checkIfComplete();
                            for(int connection : peerConnections.keySet()){
                                peerConnections.get(connection).sendMessage(Constants.HAVE, index);
                            }
                            break;
                        default:
                            break;
                    }
                }
                Thread.sleep(1000);
                System.exit(0);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}