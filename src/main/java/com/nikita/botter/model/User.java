package com.nikita.botter.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
public class User {

    @Value("${bot.botUrl}")
    private String botUrl;

    int id;
    String refUrl;
    String position;
    boolean auth;

    public User(int id) {
        this.id = id;
        this.refUrl = botUrl + "?start=" + id;
        this.position = "back";
        this.auth = false;
    }
}
