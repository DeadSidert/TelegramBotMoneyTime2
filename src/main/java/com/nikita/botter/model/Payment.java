package com.nikita.botter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment {

    private int id;
    private int userId;
    private int sum;
    private String date;
    private String timePayment;
    private boolean successful;
}
