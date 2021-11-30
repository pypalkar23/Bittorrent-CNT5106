package com.project.connectionUtils;

import com.project.logger.Logs;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class ReaderThread extends Thread{
    private PeerConnection peer;
    private PeerInfo peerSelf;
    private Logs logs;
    private Map<Integer,PeerInfo> peers;
    Map<Integer,PeerConnection> peerConnections;
    byte[][] filePieces;
    CommonInfo commonInfo;
    CommonData commonData;
    public ReaderThread(PeerConnection peer,Map<Integer,PeerConnection> peerConnections, PeerInfo peerSelf, Map<Integer,PeerInfo> peers, Logs logs, byte[][] filePieces, CommonInfo commonInfo, CommonData commonData){
        this.peer = peer;
        this.peerSelf = peerSelf;
        this.peers = peers;
        this.filePieces = filePieces;
        this.logs = logs;
        this.commonInfo = commonInfo;
        this.commonData = commonData;
        this.peerConnections = peerConnections;
    }

    @Override
    public void run(){
        double startTime;
        double endTime;
        synchronized (this)
        {
            try{
                DataInputStream dataInputStream = new DataInputStream(peer.getConnection().getInputStream());
                peer.sendMessage(Constants.BITFIELD);
                while(commonData.getValue() /*completedPeers*/ < peers.size()){
                    int msgLength = dataInputStream.readInt();
                    byte[] buffer = new byte[msgLength];
                    startTime = (double)System.nanoTime() / 100000000;
                    dataInputStream.readFully(buffer);
                    endTime = (double)System.nanoTime() / 100000000;
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
                            logs.choked(peerSelf.getPeerID(), peer.getPeerID());
                            peer.choke();
                            break;
                        case Constants.UNCHOKE:
                            peer.unchoke();
                            logs.unchoked(peerSelf.getPeerID(), peer.getPeerID());
                            peer.getPieceIndex(peerSelf.getBitfield(), peers.get(peer.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            break;
                        case Constants.INTERESTED:
                            logs.receiveInterested(peerSelf.getPeerID(), peer.getPeerID());
                            peer.setInterested();
                            break;
                        case Constants.NOT_INTERESTED:
                            logs.receiveNotInterested(peerSelf.getPeerID(), peer.getPeerID());
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
                                commonData.increment();
                            }
                            peer.compareBitfield(peerSelf.getBitfield(), peers.get(peer.getPeerID()).getBitfield(), peerSelf.getBitfield().length);
                            logs.receiveHave(peerSelf.getPeerID(), peer.getPeerID(), index);
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
                                commonData.increment();
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
                            logs.downloadingPiece(peerSelf.getPeerID(), peer.getPeerID(), index, peerSelf.getNumOfPieces());
                            int downloaded = (peerSelf.getNumOfPieces() * 100) / (int)Math.ceil((double)commonInfo.getFileSize()/commonInfo.getPieceSize());
                            StringBuffer sb = new StringBuffer();
                            sb.append("\r").append("Downloaded: ");
                            sb.append(downloaded).append("%").append(" Number of Pieces: ").append(peerSelf.getNumOfPieces());
                            System.out.print(sb);
                            peer.checkCompleted();
                            for(int connection : peerConnections.keySet()){
                                peerConnections.get(connection).sendMessage(Constants.HAVE, index);
                            }
                            break;
                        default:
                            break;
                    }
                }
                Thread.sleep(5000);
                System.exit(0);
            }
            catch(Exception e){
//                        e.printStackTrace();
            }
        }
    }
}