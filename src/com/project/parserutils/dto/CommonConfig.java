package com.project.parserutils.dto;

public class CommonConfig {
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;

    public int getNumberOfPreferredNeighbors() {
        return numberOfPreferredNeighbors;
    }

    public void setNumberOfPreferredNeighbors(int numberOfPreferredNeighbors) {
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public void setUnchokingInterval(int unchokingInterval) {
        this.unchokingInterval = unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }


    @Override
    public String toString() {
        return " numberOfPreferredNeighbors = " + numberOfPreferredNeighbors +
                        "\n unchokingInterval = " + unchokingInterval +
                        "\n optimisticUnchokingInterval =" + optimisticUnchokingInterval +
                        "\n fileName = '" + fileName + '\'' +
                        "\n fileSize = " + fileSize +
                        "\n pieceSize = " + pieceSize +
                        "\n\n";
    }
}
