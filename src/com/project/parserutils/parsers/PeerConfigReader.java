package com.project.parserutils.parsers;

import com.project.parserutils.dto.PeerInfo;
import com.project.utils.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

//Parses peer config details and stores it in a list
public class PeerConfigReader {
    public static final String peerConfigFileProp= "PEER_CONF_LOCATION";
    private static ArrayList<PeerInfo> peers;

    public static ArrayList<PeerInfo> getConfiguration() {

        String st;
        peers = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(Constants.PEER_CONFIG_FILE_NAME));
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                peers.add(new PeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return peers;
    }
}
