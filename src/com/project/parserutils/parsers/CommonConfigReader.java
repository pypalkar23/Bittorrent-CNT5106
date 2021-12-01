package com.project.parserutils.parsers;

import com.project.parserutils.dto.CommonConfigDetails;
import com.project.parserutils.dto.CommonInfo;
import com.project.utils.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//Parses Common config details and stores it in object.
public class CommonConfigReader {
    public static CommonInfo loadFile() {
        CommonInfo commonInfo = new CommonInfo();
        String st;
        try {
            BufferedReader in = new BufferedReader(new FileReader(Constants.COMMON_CONFIG_FILE_NAME));
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                if (tokens[0].equalsIgnoreCase(Constants.NO_OF_PREF_NEIGHBOURS_FIELD)) {
                   commonInfo.setNumberOfPreferredNeighbors(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.UNCHOKING_INTERVAL_FIELD)) {
                    commonInfo.setUnchokingInterval(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.OPTIMISTIC_UNCHOKING_INTERVAL)) {
                    commonInfo.setOptimisticUnchokingInterval(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.FILENAME_FIELD)) {
                    commonInfo.setFileName(tokens[1]);
                } else if (tokens[0].equalsIgnoreCase(Constants.FILESIZE_FIELD)) {
                    commonInfo.setFileSize(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.PIECE_SIZE_FIELD)) {
                    commonInfo.setPieceSize(Integer.parseInt(tokens[1]));
                }
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        return commonInfo;
    }
}
