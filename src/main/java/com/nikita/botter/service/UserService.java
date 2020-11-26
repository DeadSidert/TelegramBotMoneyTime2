package com.nikita.botter.service;


import com.nikita.botter.model.Usr;
import com.nikita.botter.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${bot.botUrl}")
    private String botUrl;

    private final JpaUserRepository userRepository;

    public Usr findById(int id){
        return userRepository.findById(id).
                orElseGet(() -> update(new Usr(id)));
    }

    public Usr update(Usr user){
        if (user.getRegDate().equalsIgnoreCase("")){
            user.setRegDate(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        }
        if (user.getRefUrl().equalsIgnoreCase("")){
            user.setRefUrl(botUrl + "?start=" + user.getId());
        }
        return userRepository.save(user);
    }

    public List<Usr> updateAll(List<Usr> usrList){
        return userRepository.saveAll(usrList);
    }

    public int countPartners(int userId){
        return userRepository.countPartners(userId);
    }

    public List<Usr> getAllNotBonus(){
        return userRepository.getAllNotBonus();
    }

    public List<Usr> findAll(){
        return userRepository.findAll();
    }

    public int findCountByRegDate(String dateToday){
        return userRepository.findCountByRegDate(dateToday);
    }

    public int countMoney(){
        return userRepository.countMoney();
    }

    public int findCountUser(){
        return userRepository.findCountUser();
    }
}
