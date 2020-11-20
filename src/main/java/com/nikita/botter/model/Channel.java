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
public class Channel {

    @Id
    String id;
    String url;
    double price;
    boolean start;

    public Channel(String id, String url, double price, boolean start) {
        this.id = id;
        this.url = url;
        this.price = price;
        this.start = start;
    }
}
