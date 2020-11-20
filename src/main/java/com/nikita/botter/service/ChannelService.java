package com.nikita.botter.service;

import com.nikita.botter.model.Channel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ChannelService {

    private final HashMap<String, Channel> channelsStart = new HashMap();
    public List<Channel> findAll(boolean start){
        List<Channel> channels = new ArrayList<>();

        for (Channel c : channelsStart.values()){
            if (c.isStart()){
                channels.add(c);
            }
        }
        return channels;
    }

    public Channel update(Channel channel){
        channelsStart.put(channel.getId(), channel);
        return channel;
    }

    public boolean isExist(String id){
        return channelsStart.containsKey(id);
    }

    public void delete(String id){
        channelsStart.remove(id);
    }
}
