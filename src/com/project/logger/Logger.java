package com.project.logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Logger {
    private final DateFormat timeFormat;
    private Date time = new Date();
    private final PrintWriter printWriter;

    public Logger(int hostID) throws IOException {
        String FILE_EXTENSION = ".log";
        String FILENAME_PREFIX = "log_peer_";
        String filePath = System.getProperty("user.dir") + File.separator + FILENAME_PREFIX + hostID + FILE_EXTENSION;
        printWriter = new PrintWriter(filePath);
        printWriter.flush();
        timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public void printToConsoleAndLog(String s){
        printWriter.printf(s);
        //System.out.print(s);
    }

    public void connectionTo(int peerIdSelf, int peerIdOther) { //hasMadeConnection
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s makes a connection to Peer %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void connectionFrom(int peerIdSelf, int peerIdOther) { //isConnected{
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s is connected from Peer %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void changePreferredNeighbors(int peerIdSelf, int[] neighborList) { //preferredNeighboursChanged
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s has the preferred neighbors %s \n", timeFormat.format(time), peerIdSelf, Arrays.toString(neighborList).replaceAll("\\[|\\]|,|\\s", "")));
    }

    public void changeOptimisticallyUnchokedNeighbor(int peerIdSelf, int peerIdOther) { // hasOptimisticallyUnchockedNeighbour
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s has the optimistically unchoked neighbor %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void unchoked(int peerIdSelf, int peerIdOther) { //hasBeenUnchocked
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s is unchoked by %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void choked(int peerIdSelf, int neighbor) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s is choked by %s.\n", timeFormat.format(time), peerIdSelf, neighbor));
    }

    public void receiveHave(int peerIdSelf, int peerIdOther, int pieceIndex) { //hasReceivedHave
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s received the 'have' message from %s for the piece %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther, pieceIndex));
    }

    public void receiveInterested(int peerIdSelf, int peerIdOther) { //hasReceivedInterested
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s received the 'interested' message to %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void receiveNotInterested(int peerIdSelf, int peerIdOther) { //hasReceivedNotInterested
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s received the 'not interested' message from %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void downloadingPiece(int peerIdSelf, int peerIdOther, int pieceIndex, int numOfPieces) { //hasDownloaded
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s has downloaded the piece %s from %s. Now the number of pieces it has is %s.\n", timeFormat.format(time), peerIdSelf, pieceIndex, peerIdOther,  numOfPieces));
    }

    public void downloadCompleted(int peerIdSelf) { //operationCompleted
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s has downloaded the complete file.\n",timeFormat.format(time), peerIdSelf));
    }
}
