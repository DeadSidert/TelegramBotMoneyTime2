package com.nikita.botter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Channel {

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
