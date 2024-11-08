package com.catolicasc.agrbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// Habilitar o uso de Feign Clients (requisições HTTP)
@EnableFeignClients
// Habilitar o uso de agendamento de tarefas
@EnableScheduling
public class AgrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrBackendApplication.class, args);
    }
}
