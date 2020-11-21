package com.nikita.botter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BotMoneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotMoneyApplication.class, args);
    }
}