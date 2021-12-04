package com.project.message;

import com.project.utils.Constants;

import java.nio.ByteBuffer;

public class Message {
    public byte[] makeMessage(int len, char type, byte[] payload){
        byte[] message;
        byte[] length;
        byte msgType = (byte)type;
        int counter;
        switch(type){
            case Constants.CHOKE:
            case Constants.UNCHOKE:
            case Constants.INTERESTED:
            case Constants.NOT_INTERESTED:
                message = new byte[len + 4];
                length = ByteBuffer.allocate(4).putInt(len).array();
                counter = 0;
                for(byte x : length) {
                    message[counter] = x;
                    counter++;
                }
                message[counter] = msgType;
                break;
            case Constants.HAVE:
            case Constants.BITFIELD:
            case Constants.REQUEST:
            case Constants.PIECE:
                message = new byte[len + 4];
                length = ByteBuffer.allocate(4).putInt(len).array();
                counter = 0;
                for(byte x : length) {
                    message[counter] = x;
                    counter++;
                }
                message[counter++] = msgType;
                for(byte x : payload) {
                    message[counter] = x;
                    counter++;
                }
                break;
            default:
                message = new byte[0];
                System.out.println("Corrupt Msg: " + type);
        }
        return message;
    }

    public byte[] getChokeMessage(){
        return makeMessage(1, Constants.CHOKE, null);
    }

    public byte[] getUnchokeMessage(){
        return makeMessage(1, Constants.UNCHOKE, null);
    }

    public byte[] getInterestedMessage(){
        return makeMessage(1, Constants.INTERESTED, null);
    }

    public byte[] getNotInterestedMessage(){
        return makeMessage(1, Constants.NOT_INTERESTED, null);
    }

    public byte[] getHaveMessage(int pieceIndex){
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        return makeMessage(5, Constants.HAVE, payload);
    }

    public byte[] getBitfieldMessage(int[] bitfield){
        int len = 1 + (4 * bitfield.length);
        byte[] payload = new byte[len - 1];
        int counter = 0;
        for(int bit : bitfield){
            byte[] bitBytes = ByteBuffer.allocate(4).putInt(bit).array();
            for(byte b : bitBytes){
                payload[counter] = b;
                counter++;
            }
        }
        return makeMessage(len, Constants.BITFIELD, payload);
    }

    public byte[] getRequestMessage(int pieceIndex){
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        return makeMessage(5, Constants.REQUEST, payload);
    }

    public byte[] getPieceMessage(int pieceIndex, byte[] piece){
        byte[] payload = new byte[4 + piece.length];
        int counter = 0;
        byte[] indexBytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        for(byte bit : indexBytes){
            payload[counter] = bit;
            counter++;
        }
        for(byte bit : piece){
            payload[counter] = bit;
            counter++;
        }
        return makeMessage((5 + piece.length), Constants.PIECE, payload);
    }

    public byte[] getHandshakeMessage(int peerID){
        byte[] message = new byte[32];
        byte[] header = Constants.MSG_HEADER.getBytes();
        byte[] zerobits = Constants.PADDED_STRING.getBytes();
        byte[] id = ByteBuffer.allocate(4).putInt(peerID).array();
        int counter = 0;
        for(byte b : header){
            message[counter] = b;
            counter++;
        }
        for(byte b : zerobits){
            message[counter] = b;
            counter++;
        }
        for(byte b : id){
            message[counter] = b;
            counter++;
        }
        return message;
    }
}
