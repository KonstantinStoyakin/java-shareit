package ru.practicum.shareit;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ShareItServer {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ShareItServer.class)
                .properties("server.port=9090")
                .run(args);
    }
}
