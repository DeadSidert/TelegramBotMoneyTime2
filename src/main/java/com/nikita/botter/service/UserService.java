package com.nikita.botter.service;


import com.nikita.botter.model.User;
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

    public User findById(int id){
        return userRepository.findById(id).
                orElse(update(new User(id)));
    }

    public User update(User user){
        user.setReferUrl(botUrl + "?start=" + user.getId());
        return userRepository.save(user);
    }
}
