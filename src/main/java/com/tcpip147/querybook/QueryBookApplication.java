package com.tcpip147.querybook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueryBookApplication implements CommandLineRunner {

    @Autowired
    private QueryBook queryBook;

    public static void main(String[] args) {
        SpringApplication.run(QueryBookApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(queryBook.getQuery("select").getSql());
    }
}
