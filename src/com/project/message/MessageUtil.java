package com.project.message;

import com.project.utils.Constants;

import java.nio.ByteBuffer;

public class MessageUtil {
    public byte[] prepareMessage(int len, char type, byte[] payload){
        byte[] message;
        byte[] length;
        byte msgType = (byte)type;
        int counter;
        switch(type){
            case Constants.CHOKE_MSG:
            case Constants.UNCHOKE_MSG:
            case Constants.INTERESTED_MSG:
            case Constants.NOT_INTERESTED_MSG:
            case Constants.EXIT_MSG:
                message = new byte[len + 4];
                length = ByteBuffer.allocate(4).putInt(len).array();
                counter = 0;
                for(byte x : length) {
                    message[counter] = x;
                    counter++;
                }
                message[counter] = msgType;
                break;
            case Constants.HAVE_MSG:
            case Constants.BITFIELD_MSG:
            case Constants.REQUEST_MSG:
            case Constants.PIECE_MSG:
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

    public byte[] prepareChokeMessage(){
        return prepareMessage(1, Constants.CHOKE_MSG, null);
    }

    public byte[] prepareUnchokeMessage(){
        return prepareMessage(1, Constants.UNCHOKE_MSG, null);
    }

    public byte[] prepareInterestedMessage(){
        return prepareMessage(1, Constants.INTERESTED_MSG, null);
    }

    public byte[] prepareNotInterestedMessage(){
        return prepareMessage(1, Constants.NOT_INTERESTED_MSG, null);
    }

    public byte[] prepareExitMessage(){
        return prepareMessage(1, Constants.EXIT_MSG, null);
    }

    public byte[] prepareHaveMessage(int pieceIndex){
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        return prepareMessage(5, Constants.HAVE_MSG, payload);
    }

    public byte[] prepareBitfieldMessage(int[] bitfield){
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
        return prepareMessage(len, Constants.BITFIELD_MSG, payload);
    }

    public byte[] prepareRequestMessage(int pieceIndex){
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        return prepareMessage(5, Constants.REQUEST_MSG, payload);
    }

    public byte[] preparePieceMessage(int pieceIndex, byte[] piece){
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
        return prepareMessage((5 + piece.length), Constants.PIECE_MSG, payload);
    }

    public byte[] prepareHandshakeMessage(int peerID){
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
