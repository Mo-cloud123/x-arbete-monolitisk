package se.mo.xarbetemonolitisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XArbeteMonolitiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(XArbeteMonolitiskApplication.class, args);
    }

}
