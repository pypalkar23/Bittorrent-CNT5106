package com.project.ParserUtils.Parsers;

import com.project.ParserUtils.DTO.CommonConfigDetails;
import com.project.Utils.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//Parses Common config details and stores it in object.
public class CommonConfigReader {
    public static final String commonConfigFileProp= "COMMON_CONF_LOCATION";

    public static CommonConfigDetails loadFile() {

        Properties properties = new Properties();
        InputStream inputStream = CommonConfigReader.class.getClassLoader().getResourceAsStream(Constants.PROPERTY_FILE_NAME);
        try{
            if(inputStream != null)
                properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String commonConfigPath = properties.getProperty(commonConfigFileProp);
        CommonConfigDetails commonConfigDetails = new CommonConfigDetails();
        String st;
        try {
            BufferedReader in = new BufferedReader(new FileReader(Constants.COMMON_CONFIG_FILE_NAME));
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                if (tokens[0].equalsIgnoreCase("NumberOfPreferredNeighbors")) {
                    commonConfigDetails.setNumberOfPreferredNeighbors(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase("UnchokingInterval")) {
                    commonConfigDetails.setUnchokingInterval(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase("OptimisticUnchokingInterval")) {
                    commonConfigDetails.setOptimisticUnchokingInterval(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase("FileName")) {
                    commonConfigDetails.setFileName(tokens[1]);
                } else if (tokens[0].equalsIgnoreCase("FileSize")) {
                    commonConfigDetails.setFileSize(Integer.parseInt(tokens[1]));
                } else if (tokens[0].equalsIgnoreCase("PieceSize")) {
                    commonConfigDetails.setChunkSize(Integer.parseInt(tokens[1]));
                }

            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        return commonConfigDetails;
    }
}
