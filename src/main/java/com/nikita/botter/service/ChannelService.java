package com.nikita.botter.service;

import com.nikita.botter.model.Channel;
import com.nikita.botter.repository.JpaChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

   private final JpaChannelRepository channelRepository;

    public List<Channel> findAllByStart(boolean start){
        return channelRepository.findAllByStart(start);
    }

    public List<Channel> getChannels(){
        return channelRepository.findAll();
    }

    public Channel update(Channel channel){
        return channelRepository.save(channel);
    }

    public Channel findById(String id){
        return channelRepository.findById(id).get();
    }

    public void delete(String id){
        channelRepository.deleteById(id);
    }


}
