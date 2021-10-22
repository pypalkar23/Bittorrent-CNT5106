package com.project.message;

import java.util.Collections;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.Arrays;
enum StatusOfPiece {
    PRESENT,
    REQUESTED,
    ABSENT
}


public class PieceMap {
    private final ArrayList<StatusOfPiece> pieceSet;
    public int piecesPresent = 0;

    public PieceMap(int size, final boolean isFull) {

        pieceSet = new ArrayList<>(Arrays.asList(new StatusOfPiece[size]));

        if (isFull) {
            piecesPresent = size;
            Collections.fill(pieceSet, StatusOfPiece.PRESENT);
        } else {
            Collections.fill(pieceSet, StatusOfPiece.ABSENT);
        }
    }

    //for creating   bitfield
    public static PieceMap formation(int size, final boolean isFull) {
        return new PieceMap(size, isFull);
    }

    //for getting status of piece
    public StatusOfPiece getStatus(int index) {
        return pieceSet.get(index);
    }

    //converting arraylist to bytearray
    public byte[] toByteArray() {

        BitSet tempBitset = new BitSet(this.pieceSet.size());

        for (int x = 0; x < this.pieceSet.size(); x++) {
            if (this.pieceSet.get(x) == StatusOfPiece.PRESENT) {
                tempBitset.set(x);
            }
        }
        return tempBitset.toByteArray();
    }

    //setting value for pieceMap
    public void setPieceMapIndex(final byte[] bytes) {

        BitSet bSet = BitSet.valueOf(bytes);
        piecesPresent = 0;

        for (int x = 0; x < pieceSet.size(); x++) {
            if (bSet.get(x)) {
                pieceSet.set(x, StatusOfPiece.PRESENT);
                piecesPresent++;
            } else {
                pieceSet.set(x, StatusOfPiece.ABSENT);
            }
        }
    }

    //setting status of piece to PRESENT
    public void setPresent(int index) throws PieceMapException {
        if (pieceSet.get(index) != StatusOfPiece.PRESENT) {
            pieceSet.set(index, StatusOfPiece.PRESENT);
            piecesPresent++;
        } else {
            throw new PieceMapException();
        }
    }

    //setting status of piece to ABSENT
    public void setAbsent(int index) throws PieceMapException {
        if (pieceSet.get(index) != StatusOfPiece.PRESENT) {
            pieceSet.set(index, StatusOfPiece.ABSENT);
        } else {
            throw new PieceMapException();
        }
    }

    //setting status of piece to requested
    public void setRequested(int index) throws PieceMapException {
        if (pieceSet.get(index) != StatusOfPiece.PRESENT) {
            pieceSet.set(index, StatusOfPiece.REQUESTED);
        } else {
            throw new PieceMapException();
        }
    }

    public boolean isFull() {
        return piecesPresent == pieceSet.size();
    }

}


