package com.project.ParserUtils.Parsers;

import com.project.ParserUtils.DTO.PeerConnectionDetails;
import com.project.Utils.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

//Parses peer config details and stores it in a list
public class PeerConfigReader {
    public static final String peerConfigFileProp= "PEER_CONF_LOCATION";
    private static ArrayList<PeerConnectionDetails> peers;

    public static ArrayList<PeerConnectionDetails> getConfiguration() {
        Properties properties = new Properties();
        InputStream inputStream = PeerConfigReader.class.getClassLoader().getResourceAsStream(Constants.PROPERTY_FILE_NAME);
        try{
            if(inputStream != null)
                properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String peerConfigPath = properties.getProperty(peerConfigFileProp);
        String st;
        peers = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader( peerConfigPath));
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                peers.add(new PeerConnectionDetails(tokens[0], tokens[1], tokens[2], tokens[3]));
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return peers;
    }
}
