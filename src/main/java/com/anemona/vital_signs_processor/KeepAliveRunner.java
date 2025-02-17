package com.anemona.vital_signs_processor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KeepAliveRunner implements CommandLineRunner{

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Vital Signs Processor está luchando por su vida...");
        Thread.currentThread().join(); //zombificamos la app
    }
    
}
