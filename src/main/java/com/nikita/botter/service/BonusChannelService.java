package com.nikita.botter.service;

import com.nikita.botter.model.BonusChannel;
import com.nikita.botter.repository.JpaBonusChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BonusChannelService {

    private final JpaBonusChannel bonusChannelRepository;

    public BonusChannel update(BonusChannel bonusChannel){
        return bonusChannelRepository.save(bonusChannel);
    }

    public void delete(int id){
        bonusChannelRepository.deleteById(id);
    }
    public List<BonusChannel> findAll(){
        return bonusChannelRepository.findAll();
    }
}
