package com.project.parserutils.dto;

public class PeerConnectionDetails {
    private int id;
    private String name;
    private int port;
    private boolean hasFile;

    public PeerConnectionDetails(String idStr, String name, String portStr, String hasFileStr) {
        this.id = Integer.parseInt(idStr);
        this.name = name;
        this.port = Integer.parseInt(portStr);
        this.hasFile = (Integer.parseInt(hasFileStr) == 1);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public boolean isHasFile() {
        return hasFile;
    }

    @Override
    public String toString() {
        return "PeerConnectionDetails{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", hasFile=" + hasFile +
                '}';
    }
}
