package com.nikita.botter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Channel {

    String id;
    int userId;
    String url;
    boolean start;
}
