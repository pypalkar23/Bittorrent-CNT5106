package com.project;

import com.project.bittorrent.client.Client;
import com.project.bittorrent.master.Master;

public class Initiator {
    public void init() {
        Master master = Master.getInstance();
        master.start();

        Client client = new Client();
        client.init();
    }
}
