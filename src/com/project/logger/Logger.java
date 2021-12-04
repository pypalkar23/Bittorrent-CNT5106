package com.project.logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Logger {
    private DateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date time = new Date();
    private PrintWriter printWriter;

    public Logger(int hostID) throws IOException {
        String filePath = System.getProperty("user.dir") + "/" + "log_peer_" + hostID + ".log";
        printWriter = new PrintWriter(filePath);
        printWriter.flush();
        timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public void connectionTo(int peerIdSelf, int peerIdOther) { //hasMadeConnection {
        time = new Date();
        printWriter.printf("%s : Peer %s makes a connection to Peer %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther);
    }

    public void connectionFrom(int peerIdSelf, int peerIdOther) { //isConnected{
        time = new Date();
        printWriter.printf("%s : Peer %s is connected from Peer %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther);
    }

    public void changePreferredNeighbors(int peerIdSelf, int[] neighborList) { //preferredNeigfhboursChanged{
        time = new Date();
        printWriter.printf("%s : Peer %s has the preferred neighbors ", timeFormat.format(time), peerIdSelf);
        printWriter.printf("%s",  Arrays.toString(neighborList).replaceAll("\\[|\\]|,|\\s", ""));
        printWriter.printf(".\n");
    }

    public void changeOptimisticallyUnchokedNeighbor(int peerIdSelf, int peerIdOther) { // hasOptimisticallyUnchockedNeighbour
        time = new Date();
        printWriter.printf("%s : Peer %s has the optimistically unchoked neighbor %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther);
    }

    public void unchoked(int peerIdSelf, int peerIdOther) { //hasBeenUnchocked
        time = new Date();
        printWriter.printf("%s : Peer %s is unchoked by %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther);
    }

    public void choked(int peerIdSelf, int neighbor) {
        time = new Date();
        printWriter.printf("%s : Peer %s is choked by %s.\n", timeFormat.format(time), peerIdSelf, neighbor);
    }

    public void receiveHave(int peerIdSelf, int peerIdOther, int pieceIndex) { //hasReceivedHave
        time = new Date();
        printWriter.printf("%s : Peer %s received the 'have' message from %s for the piece %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther, pieceIndex);
    }

    public void receiveInterested(int peerIdSelf, int peerIdOther) { //hasReceivedInterested
        time = new Date();
        printWriter.printf("%s : Peer %s received the 'interested' message to %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther);
    }

    public void receiveNotInterested(int peerIdSelf, int peerIdOther) { //hasReceivedNotInterested
        time = new Date();
        printWriter.printf("%s : Peer %s received the 'not interested' message from %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther);
    }

    public void downloadingPiece(int peerIdSelf, int peerIdOther, int pieceIndex, int numOfPieces) { //hasDownloaded
        time = new Date();
        printWriter.printf("%s : Peer %s has downloaded the piece %s from %s. Now the number of pieces it has is %s.\n", timeFormat.format(time), peerIdSelf, pieceIndex, peerIdOther,  numOfPieces);
    }

    public void downloadCompleted(int peerIdSelf) { //operationCompleted
        time = new Date();
        printWriter.printf("%s : Peer %s has downloaded the complete file.\n",timeFormat.format(time), peerIdSelf);
    }
}
