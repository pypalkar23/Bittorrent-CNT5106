package com.project.parserutils.dto;

import java.util.concurrent.atomic.AtomicInteger;

public class CommonData {
    private final AtomicInteger completedPeers;

    public CommonData(){
        completedPeers = new AtomicInteger(0);
    }

    public int getCompletedPeers(){
        return completedPeers.get();
    }

    public void incrementCompletedPeers(){
        while(true){
            int existingValue = getCompletedPeers();
            int newValue = existingValue+1;
            if(completedPeers.compareAndSet(existingValue,newValue)){
                return;
            }
        }
    }

}
