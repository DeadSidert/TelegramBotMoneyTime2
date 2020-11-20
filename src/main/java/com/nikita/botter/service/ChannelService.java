package com.nikita.botter.service;

import com.nikita.botter.model.Channel;

import java.util.ArrayList;
import java.util.List;

public class ChannelService {

    private List<Channel> channelsStart = new ArrayList<>();
    public List<Channel> findAll(boolean start){
        List<Channel> channels = new ArrayList<>();

        for (Channel c : channelsStart){
            if (c.isStart()){
                channels.add(c);
            }
        }
        return channels;
    }

    public Channel update(Channel channel){
        channelsStart.add(channel);
        return channel;
    }
}
