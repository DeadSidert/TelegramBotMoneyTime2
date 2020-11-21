package com.nikita.botter.service;


import com.nikita.botter.model.Usr;
import com.nikita.botter.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        user.setReferUrl(botUrl + "?start=" + user.getId());
        return userRepository.save(user);
    }
}
