package com.nikita.botter.service;

import com.nikita.botter.model.ChannelCheck;
import com.nikita.botter.repository.JpaChannelCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelCheckService {

    private final JpaChannelCheckRepository channelCheckRepository;

    public List<ChannelCheck> findAll(int id){
        return channelCheckRepository.findAllByUserId(id);
    }

    public ChannelCheck update(ChannelCheck channelCheck){
        return channelCheckRepository.save(channelCheck);
    }
}
