package com.project.logger;

import com.project.utils.Helpers;

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
        System.out.print(s);
    }

    public void hasMadeConnectionTo(int peerIdSelf, int peerIdOther) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s makes a connection to Peer %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void hasReceivedConnectionFrom(int peerIdSelf, int peerIdOther) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s is connected from Peer %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void hasChangedPreferredNeighbours(int peerIdSelf, int[] neighborList) {
        time = new Date();
        String neighbourString = Helpers.getNeighbourString(neighborList);
        if(neighbourString!=null){
            printToConsoleAndLog(String.format("%s : Peer %s has the preferred neighbors%s \n", timeFormat.format(time), peerIdSelf, neighbourString));
        }
    }

    public void hasOptimisticallyUnchokedNeighbour(int peerIdSelf, int peerIdOther) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s has the optimistically unchoked neighbor %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void hasBeenUnchokedBy(int peerIdSelf, int peerIdOther) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s is unchoked by %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void hasBeenChokedBy(int peerIdSelf, int neighbor) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s is choked by %s.\n", timeFormat.format(time), peerIdSelf, neighbor));
    }

    public void hasReceivedHaveMsg(int peerIdSelf, int peerIdOther, int pieceIndex) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s received the 'have' message from %s for the piece %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther, pieceIndex));
    }

    public void hasReceivedInterestedMsg(int peerIdSelf, int peerIdOther) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s received the 'interested' message to %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void hasReceivedNotInterestedMsg(int peerIdSelf, int peerIdOther) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s received the 'not interested' message from %s.\n", timeFormat.format(time), peerIdSelf, peerIdOther));
    }

    public void hasDownloadedMsg(int peerIdSelf, int peerIdOther, int pieceIndex, int numOfPieces) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s has downloaded the piece %s from %s. Now the number of pieces it has is %s.\n", timeFormat.format(time), peerIdSelf, pieceIndex, peerIdOther,  numOfPieces));
    }

    public void hasCompletedDownload(int peerIdSelf) {
        time = new Date();
        printToConsoleAndLog(String.format("%s : Peer %s has downloaded the complete file.\n",timeFormat.format(time), peerIdSelf));
    }
}
