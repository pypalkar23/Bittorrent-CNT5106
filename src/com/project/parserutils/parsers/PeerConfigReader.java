package com.project.parserutils.parsers;

import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

//Parses peer config details and stores it in a list
public class PeerConfigReader {
    private static HashMap<Integer,PeerInfo> peers;

    public static Map<Integer,PeerInfo> getConfiguration() {

        String st;
        peers = new LinkedHashMap<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(Constants.PEER_CONFIG_FILE_NAME));
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                PeerInfo peer = new PeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]);
                peers.put(peer.getPeerID(),peer);
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return peers;
    }
}
