package com.project.parserutils.dto;

import java.util.concurrent.atomic.AtomicInteger;

public class CommonData {
    private final AtomicInteger completedPeers;

    public CommonData(){
        completedPeers = new AtomicInteger(0);
    }

    public int getValue(){
        return completedPeers.get();
    }

    public void increment(){
        while(true){
            int existingValue = getValue();
            int newValue = existingValue+1;
            if(completedPeers.compareAndSet(existingValue,newValue)){
                return;
            }
        }
    }

}
