package com.project.parserutils.parsers;

import com.project.parserutils.dto.CommonConfig;
import com.project.utils.Constants;

import java.io.BufferedReader;
import java.io.FileReader;

//Parses Common config details and stores it in object.
public class CommonConfigReader {
    public static CommonConfig loadFile() {
        CommonConfig commonConfig = new CommonConfig();
        String st;
        try {
            BufferedReader in = new BufferedReader(new FileReader(Constants.COMMON_CONFIG_FILE_NAME));
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                if (tokens[0].equalsIgnoreCase(Constants.NO_OF_PREF_NEIGHBOURS_FIELD)) {
                   commonConfig.setNumberOfPreferredNeighbors(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.UNCHOKING_INTERVAL_FIELD)) {
                    commonConfig.setUnchokingInterval(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.OPTIMISTIC_UNCHOKING_INTERVAL)) {
                    commonConfig.setOptimisticUnchokingInterval(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.FILENAME_FIELD)) {
                    commonConfig.setFileName(tokens[1]);
                } else if (tokens[0].equalsIgnoreCase(Constants.FILESIZE_FIELD)) {
                    commonConfig.setFileSize(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase(Constants.PIECE_SIZE_FIELD)) {
                    commonConfig.setPieceSize(Integer.parseInt(tokens[1]));
                }
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        return commonConfig;
    }
}
