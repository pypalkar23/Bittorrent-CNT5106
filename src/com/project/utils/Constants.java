package com.project.utils;

public class Constants {
    public static final String MSG_HEADER = "P2PFILESHARINGPROJ";
    public static final String PADDED_STRING = "0000000000";
    public static final String NO_OF_PREF_NEIGHBOURS_FIELD = "NumberOfPreferredNeighbors";
    public static final String UNCHOKING_INTERVAL_FIELD = "UnchokingInterval";
    public static final String OPTIMISTIC_UNCHOKING_INTERVAL = "OptimisticUnchokingInterval";
    public static final String FILENAME_FIELD = "FileName";
    public static final String FILESIZE_FIELD = "FileSize";
    public static final String PIECE_SIZE_FIELD = "PieceSize";
    public static final String COMMON_CONFIG_FILE_NAME = "Common.cfg";
    public static final String PEER_CONFIG_FILE_NAME = "PeerInfo.cfg";
    public static final char CHOKE_MSG = '1';
    public static final char UNCHOKE_MSG = '2';
    public static final char INTERESTED_MSG = '3';
    public static final char NOT_INTERESTED_MSG = '4';
    public static final char HAVE_MSG = '5';
    public static final char BITFIELD_MSG = '6';
    public static final char REQUEST_MSG = '7';
    public static final char PIECE_MSG = '8';
    public static final char EXIT_MSG ='9';
    public static final int NANOSECOND_DIVISOR = 100000000;
    public static final int BYTE_SIZE = 32;
}
