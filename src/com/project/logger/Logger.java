package com.project.logger;


import com.project.utils.Constants;
import com.project.message.PieceMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.ArrayList;

public class Logger {
    PrintWriter printWriter;

    public Logger(String filePath) {
        try {
            printWriter = new PrintWriter(filePath);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void hasMadeConnection(int peerIdSelf, int peerIdOther) throws IOException {
        printWriter.printf("%s : Peer %s makes a connection to Peer %s.\n", LocalTime.now(), peerIdSelf, peerIdOther);
    }

    //connected
    public synchronized void isConnected(int peerIdSelf, int peerIdOther) {
        printWriter.printf("%s : Peer %s is connected from Peer %s.\n", LocalTime.now(), peerIdSelf, peerIdOther);
    }


    public synchronized void hasSentPieceMap(int neigh,PieceMap pieceMap) {
        printWriter.printf("%s : sent a PieceMap to %s with file %s \n", LocalTime.now(), neigh, pieceMap.isFull() ? Constants.FULL_STATUS : Constants.EMPTY_STATUS);
    }


    public synchronized void hasReceivedPieceMap(int neigh,PieceMap pieceMap) {
        printWriter.printf("%s : received a PieceMap from %s with file %s \n", LocalTime.now(), neigh, pieceMap.isFull() ? Constants.FULL_STATUS : Constants.EMPTY_STATUS);
    }


    public synchronized void preferredNeighboursChanged(int peerIdSelf,ArrayList<Integer> neighborList) {
        printWriter.printf("%s : Peer %s has the preferred neighbors ", LocalTime.now(), peerIdSelf);
        neighborList.forEach(neighbor -> { printWriter.printf(", %s", neighbor); });
        printWriter.printf(".\n");
    }


    public synchronized void hasOptimisticallyUnchockedNeighbour(int peerIdSelf, int neighbor) {
        printWriter.printf("%s : Peer %s has the optimistically unchoked neighbor %s.\n", LocalTime.now(), peerIdSelf, neighbor);
    }


    public synchronized void hasSentUnchokeSignal(int peerIdSelf, int neighbor) {
        printWriter.printf("%s : Peer %s sent unchoke to %s.\n", LocalTime.now(),peerIdSelf, neighbor);
    }

    public synchronized void hasSentChokeSignal(int peerIdSelf, int neighbor) {
        printWriter.printf("%s : Peer %s sent choke to %s.\n", LocalTime.now(), peerIdSelf, neighbor);
    }

    public synchronized void hasBeenChocked(int peerIdSelf, int neighbor) {
        printWriter.printf("%s : Peer %s is choked by %s.\n", LocalTime.now(), peerIdSelf, neighbor);
    }

    public synchronized void hasBeenUnchocked(int peerIdSelf, int neighbor) {
        printWriter.printf("%s : Peer %s is unchoked by %s.\n", LocalTime.now(), peerIdSelf, neighbor);
    }

    public synchronized void hasSentHave(int peer, int index) {
        printWriter.printf("%s : sent have to %s index: %s \n", LocalTime.now(), peer, index);
    }

    public synchronized void hasreceivedHave(int peerIdSelf, int peerIdOther, int pieceIndex) {
        printWriter.printf("%s : Peer %s received the 'have' message from %s for the piece %s.\n", LocalTime.now(), peerIdSelf, peerIdOther, pieceIndex);
    }

    public synchronized void hasSentInterested(int peerIdSelf, int peerIdOther) {
        printWriter.printf("%s : Peer %s send 'interested' message to %s.\n", LocalTime.now(), peerIdSelf, peerIdOther);
    }

    public synchronized void hasReceivedInterested(int peerIdSelf, int peerIdOther) {
        printWriter.printf("%s : Peer %s received the 'interested' message from %s.\n", LocalTime.now(), peerIdSelf, peerIdOther);
    }

    public synchronized void hasSentNotInterested(int peerIdSelf, int peerIdOther) {
        printWriter.printf("%s : Peer %s sent 'not interested' message to %s.\n", LocalTime.now(), peerIdSelf, peerIdOther);
    }

    public synchronized void hasReceivedNotInterested(int peerIdSelf, int peerIdOther) {
        printWriter.printf("%s : Peer %s received the 'not interested' message from %s.\n", LocalTime.now(), peerIdSelf, peerIdOther);
    }

    public synchronized void hasSentRequest(int peerIdSelf, int request) {
        printWriter.printf("%s : requesting %s from %s.\n", LocalTime.now(), request, peerIdSelf);
    }

    public synchronized void hasRequested(int peerIdSelf, int request) {
        printWriter.printf("%s : Peer %s requested %s.\n", LocalTime.now(), peerIdSelf, request);
    }

    public synchronized void hasSentPiece(int neigh, int pieceIndex) {
        printWriter.printf("%s : sent piece %s to %s \n", LocalTime.now(), pieceIndex, neigh);
    }

    public synchronized void hasDownloaded(int peerIdSelf, int peerIdOther, int pieceIndex, int numPieces) {
        printWriter.printf("%s : Peer %s has downloaded the piece %s from %s. Now the number of pieces it has is %s.\n", LocalTime.now(), peerIdSelf, pieceIndex, peerIdOther, numPieces);
    }


    public synchronized void operationCompleted(int peerIDSelf) {
        printWriter.printf("%s : Peer %s has downloaded the complete file.\n", LocalTime.now(), peerIDSelf);
    }

    public synchronized void hasDownloadedFile(int peerIdSelf, int neigh) {
        printWriter.printf("%s : Peer %s has downloaded the complete file from %s\n", LocalTime.now(), peerIdSelf, neigh);
    }

    public void close() {
        printWriter.close();
    }

    public void flush() {
        printWriter.flush();
    }
}
