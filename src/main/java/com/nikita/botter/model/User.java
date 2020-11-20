package com.nikita.botter.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
public class User {

    @Id
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
