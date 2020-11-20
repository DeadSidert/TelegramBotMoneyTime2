package com.nikita.botter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    int id;
    String refUrl;
    String position;
    boolean auth;
    int countRefs;
    // тот, кто привел
    String referUrl;
    int money;

    public User(int id) {
        this.id = id;
        this.refUrl = "";
        this.position = "back";
        this.auth = false;
        this.countRefs = 0;
        this.referUrl = "";
        this.money = 0;
    }
}
