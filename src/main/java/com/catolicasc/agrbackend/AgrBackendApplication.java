package com.catolicasc.agrbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.config.**"
))
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.**.controller"
))
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.feature.**.service"
))
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.clients.**.service"
))
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.feature.**.repository"
))
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.feature.**.mapper"
))
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.clients.**.mapper"
))
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.feature.**.job"
))
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.feature.**.domain"
))
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.clients.**.dto"
))
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.feature.**.dto"
))
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.clients.**.enums"
))
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.feature.**.enums"
))
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASPECTJ,
        pattern = "com.catolicasc.agrbackend.shared.**"
))
public class AgrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrBackendApplication.class, args);
    }

}
