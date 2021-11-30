package com.project;


import com.project.connectionUtils.PeerConnection;
import com.project.connectionUtils.ReceiveConnections;
import com.project.connectionUtils.SendConnections;
import com.project.logger.Logs;
import com.project.message.Messages;
import com.project.parserutils.dto.CommonData;
import com.project.parserutils.dto.CommonInfo;
import com.project.parserutils.dto.PeerInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class peerProcess {
    private static final char CHOKE = '0';
    private static final char UNCHOKE = '1';
    private static final char INTERESTED = '2';
    private static final char NOT_INTERESTED = '3';
    private static final char HAVE = '4';
    private static final char BITFIELD = '5';
    private static final char REQUEST = '6';
    private static final char PIECE = '7';
    private static int hostID;
    private static Map<Integer, PeerInfo> peers;
    private static byte[][] filePieces;
    private static Messages msg = new Messages();
    private static File log_file;
    private static Logs logs;
    private static Map<Integer, PeerConnection> peerConnections;
    private static PeerInfo thisPeer;
    private static CommonInfo common;
    private static int completedPeers = 0;
    private static File directory;
    public static void main(String[] args) {
        CommonData commonData = new CommonData();
        System.out.println("Starting Process");
        hostID = Integer.parseInt(args[0]);
        try {
            /*
             *   Create the file directory corresponding to this peer.
             */
            directory = new File("peer_" + hostID);
            if (directory.exists() == false) {
                directory.mkdir();
            }
            log_file = new File(System.getProperty("user.dir") + "/" + "log_peer_" + hostID + ".log");
            if (log_file.exists() == false)
                log_file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(log_file.getAbsolutePath(), true));
            writer.flush();
            logs = new Logs(writer);
            /*
             *   Read PeerInfo.cfg file. Each line in this file contains details about a peer.
             *   Create PeerInfo object for each line in this file.
             *   Enter details of each peer in corresponding PeerInfo object.
             *   Put PeerInfo objects in a LinkedHashMap with their peerIDs as keys.
             *   LinkedHashMap is used to maintain the order of stored PeerInfo objects according to the file.
             */
            BufferedReader peerInfo = new BufferedReader(new FileReader("PeerInfo.cfg"));
            peers = new LinkedHashMap<>();
            for (Object line : peerInfo.lines().toArray()) {
                String[] parts = ((String) line).split(" ");
                PeerInfo peer = new PeerInfo();
                peer.setPeerID(Integer.parseInt(parts[0]));
                peer.setHostName(parts[1]);
                peer.setPortNumber(Integer.parseInt(parts[2]));
                peer.setHaveFile(Integer.parseInt(parts[3]));
                peers.put(peer.getPeerID(), peer);
            }
            peerInfo.close();

//            for(PeerInfo peer : peers.values()){
//                System.out.println(peer.getHost() + ":" + peer.getPort() + ":" + peer.getHaveFile());
//            }

            /*
             *   Read Common.cfg file. Each line contains values of some variables.
             *   Create object of CommonInfo class to store the values of those variables in corresponding variables of CommonInfo object.
             */
            BufferedReader commonInfo = new BufferedReader(new FileReader("Common.cfg"));
            common = new CommonInfo();
            Object[] commonInfoLines = commonInfo.lines().toArray();
            common.setNumberOfPreferredNeighbors(Integer.parseInt(((String) commonInfoLines[0]).split(" ")[1]));
            common.setUnchokingInterval(Integer.parseInt(((String) commonInfoLines[1]).split(" ")[1]));
            common.setOptimisticUnchokingInterval(Integer.parseInt(((String) commonInfoLines[2]).split(" ")[1]));
            common.setFileName(((String) commonInfoLines[3]).split(" ")[1]);
            common.setFileSize(Integer.parseInt(((String) commonInfoLines[4]).split(" ")[1]));
            common.setPieceSize(Integer.parseInt(((String) commonInfoLines[5]).split(" ")[1]));
            commonInfo.close();

            /*
             *   Add the Bitfield to this peer.
             *   If this peer has file, all bits in Bitfield are set to 1, else they are set to 0.
             *   If this peer has file, new pieces of files are created.
             *   All those pieces are stored in array of pieces corresponding to their indices.
             *   Size of pieces is given in Common.cfg
             *   Number of pieces = Ceil(FileSize/PieceSize)
             *   Size of Bitfield = Number of pieces
             */
            thisPeer = peers.get(hostID);
            int fileSize = common.getFileSize();
            int pieceSize = common.getPieceSize();
            int numOfPieces = (int) Math.ceil((double) fileSize / pieceSize);
            filePieces = new byte[numOfPieces][];
            int bitfieldSize = numOfPieces;
            int[] bitfield = new int[bitfieldSize];
            if (thisPeer.getHaveFile() == 1) {
                //completedPeers++;
                commonData.increment();
                Arrays.fill(bitfield, 1);
                thisPeer.setBitfield(bitfield);
                //Dividing File into pieces and storing them into array of pieces.
                BufferedInputStream file = new BufferedInputStream(new FileInputStream(directory.getAbsolutePath() + "/" + common.getFileName()));
                byte[] fileBytes = new byte[fileSize];
                file.read(fileBytes);
                file.close();
                int part = 0;

                for (int counter = 0; counter < fileSize; counter += pieceSize) {
                    //byte[] pieceBytes = Arrays.copyOfRange(fileBytes, counter, counter + pieceSize);
                    if (counter + pieceSize <= fileSize)
                        filePieces[part] = Arrays.copyOfRange(fileBytes, counter, counter + pieceSize);
                    else
                        filePieces[part] = Arrays.copyOfRange(fileBytes, counter, fileSize);
//                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("Common_" + part + ".cfg"));
//                    bos.write(pieces[part]);
//                    bos.close();
                    part++;
                    thisPeer.updateNumOfPieces();
                }
            } else {
                Arrays.fill(bitfield, 0);
                thisPeer.setBitfield(bitfield);
            }
            //System.out.println(thisPeer.getNumOfPieces());

            /*
             *   Connections are established with peers.
             *   There are two types of connections.
             *   0 - Peer makes connection with all the previous peers.
             *   1 - Peer accepts connection from all the new peers.
             */

            peerConnections = new ConcurrentHashMap<>();
            //unchokedPeers = new ArrayList<>();
            SendConnections sendConnections = new SendConnections(hostID,peers,peerConnections,logs,msg,common,directory,filePieces,commonData);
            sendConnections.start();
            ReceiveConnections receiveConnections = new ReceiveConnections(hostID,peers,peerConnections,logs,msg,common,directory,filePieces,commonData);
            receiveConnections.start();
            UnchokePeers unchokePeers = new UnchokePeers();
            unchokePeers.start();
            OptimisticUnchokePeer optimisticUnchokePeer = new OptimisticUnchokePeer();
            optimisticUnchokePeer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private static class UnchokePeers extends Thread{
        @Override
        public void run(){
            while(completedPeers < peers.size()){
                ArrayList<Integer> connections = new ArrayList<>(peerConnections.keySet());
                int[] preferredNeighbors = new int[common.getNumberOfPreferredNeighbors()];
                if(thisPeer.getHaveFile() == 1) {
                    ArrayList<Integer> interestedPeers = new ArrayList<>();
                    for (int peer : connections) {
                        if(peerConnections.get(peer).isInterested())
                            interestedPeers.add(peer);
                    }
                    if (interestedPeers.size() > 0) {
                        if (interestedPeers.size() <= common.getNumberOfPreferredNeighbors()) {
                            for (Integer peer : interestedPeers) {
                                if(peerConnections.get(peer).isChoked()){
                                    peerConnections.get(peer).unchoke();
                                    peerConnections.get(peer).sendMessage(UNCHOKE);
                                }
                            }
                        } else {
                            Random r = new Random();
                            for (int i = 0; i < common.getNumberOfPreferredNeighbors(); i++) {
                                preferredNeighbors[i] = (interestedPeers.remove(Math.abs(r.nextInt() % interestedPeers.size())));
                            }
                            for (int peer : preferredNeighbors) {
                                if(peerConnections.get(peer).isChoked()){
                                    peerConnections.get(peer).unchoke();
                                    peerConnections.get(peer).sendMessage(UNCHOKE);
                                }
                            }
                            for (Integer peer : interestedPeers) {
                                if(!peerConnections.get(peer).isChoked() && !peerConnections.get(peer).isOptimisticallyUnchoked()){
                                    peerConnections.get(peer).choke();
                                    peerConnections.get(peer).sendMessage(CHOKE);
                                }
                            }
                        }
                    }
                }
                else{
                    ArrayList<Integer> interestedPeers = new ArrayList<>();
                    int counter = 0;
                    for (int peer : connections) {
                        if(peerConnections.get(peer).isInterested() && peerConnections.get(peer).getDownloadRate() >= 0)
                            interestedPeers.add(peer);
                    }
                    if(interestedPeers.size() <= common.getNumberOfPreferredNeighbors()){
                        for(int peer : interestedPeers){
                            preferredNeighbors[counter++] = peer;
                            if(peerConnections.get(peer).isChoked()){
                                peerConnections.get(peer).unchoke();
                                peerConnections.get(peer).sendMessage(UNCHOKE);
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < common.getNumberOfPreferredNeighbors(); i++) {
                            int max = interestedPeers.get(0);
                            for(int j = 1; j < interestedPeers.size(); j++){
                                if(peerConnections.get(max).getDownloadRate() <= peerConnections.get(interestedPeers.get(j)).getDownloadRate()){
                                    max = interestedPeers.get(j);
                                }
                            }
                            if(peerConnections.get(max).isChoked()) {
                                peerConnections.get(max).unchoke();
                                peerConnections.get(max).sendMessage(UNCHOKE);
                            }
                            preferredNeighbors[i] = max;
                            interestedPeers.remove(Integer.valueOf(max));
                        }
                        for (Integer peer : interestedPeers) {
                            if(!peerConnections.get(peer).isChoked() && !peerConnections.get(peer).isOptimisticallyUnchoked()){
                                peerConnections.get(peer).choke();
                                peerConnections.get(peer).sendMessage(CHOKE);
                            }
                        }
                    }
                }
                logs.changePreferredNeighbors(thisPeer.getPeerID(), preferredNeighbors);
                try{
                    Thread.sleep(common.getUnchokingInterval() * 1000);
                }
                catch(Exception e){
//                    e.printStackTrace();
                }
            }
            try{
                Thread.sleep(5000);
            }
            catch(Exception e){

            }
            System.exit(0);
        }
    }

    private static class OptimisticUnchokePeer extends Thread{
        @Override
        public void run(){
            while (completedPeers < peers.size()) {
                ArrayList<Integer> connections = new ArrayList<>(peerConnections.keySet());
                ArrayList<Integer> interested = new ArrayList<>();
                for(int connection : connections){
                    if(peerConnections.get(connection).isInterested()){
                        interested.add(connection);
                    }
                }
                if(interested.size() > 0){
                    Random r = new Random();
                    int randomNumber = Math.abs(r.nextInt() % interested.size());
                    int connection = interested.get(randomNumber);
                    peerConnections.get(connection).unchoke();
                    peerConnections.get(connection).sendMessage(UNCHOKE);
                    peerConnections.get(connection).optimisticallyUnchoke();
                    logs.changeOptimisticallyUnchokedNeighbor(thisPeer.getPeerID(), peerConnections.get(connection).getPeerID());
                    try {
                        Thread.sleep(common.getOptimisticUnchokingInterval() * 1000);
                        peerConnections.get(connection).optimisticallyChoke();
                    }
                    catch(Exception e){
//                        e.printStackTrace();
                    }
                }
            }
            try{
                Thread.sleep(5000);
            }
            catch(Exception e){

            }
            System.exit(0);
        }
    }


}
