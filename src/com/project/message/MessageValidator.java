package com.project.message;

import com.project.Utils.Constants;

public class MessageValidator {

    public static boolean isValidHandshakeMessage(Message message, int neighbourID) {
        //TODO: populate peers from config
        int[] peers = {1001, 1002, 1003, 1004, 1005, 1006};
        if (message.getMessageString().startsWith(Constants.MSG_HEADER)) {
            for (int id : peers) {
                if (id == neighbourID)
                    return true;
            }
            return false;
        }
        return false;
    }
}
