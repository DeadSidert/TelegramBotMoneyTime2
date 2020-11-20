package com.nikita.botter.service;


import com.nikita.botter.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class UserService {

    @Value("${bot.botUrl}")
    private String botUrl;

    private final HashMap<Integer, User> userHashMap = new HashMap<>();

    public boolean userExist(int id){
        return userHashMap.containsKey(id);
    }

    public User findById(int id){
        return userHashMap.get(id);
    }

    public User update(User user){
        user.setReferUrl(botUrl + "?start=" + user.getId());
        return userHashMap.put(user.getId(), user);
    }
}
