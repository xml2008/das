package com.ppdai.das.tx;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class App {
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(App.class).run(args);

    }
}
