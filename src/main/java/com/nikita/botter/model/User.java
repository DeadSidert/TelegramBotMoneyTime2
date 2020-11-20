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
    // тот, кто привел юзера
    String referUrl;
    int money;
    String qiwi;
    private boolean bonus;
    private int moneyFromPartners;

    public User(int id) {
        this.id = id;
        this.refUrl = "";
        this.position = "back";
        this.auth = false;
        this.countRefs = 0;
        this.referUrl = "";
        this.money = 0;
        this.qiwi = "";
        this.bonus = false;
        this.moneyFromPartners = 0;
    }
}
