package com.nikita.botter.service;


import com.nikita.botter.model.Usr;
import com.nikita.botter.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${bot.botUrl}")
    private String botUrl;

    private final JpaUserRepository userRepository;

    public Usr findById(int id){
        return userRepository.findById(id).
                orElse(update(new Usr(id)));
    }

    public Usr update(Usr user){
        user.setRefUrl(botUrl + "?start=" + user.getId());
        return userRepository.save(user);
    }

    public int countPartners(int userId){
        return userRepository.countPartners(userId);
    }

    public List<Usr> getAllNotBonus(){
        return userRepository.getAllNotBonus();
    }
}
