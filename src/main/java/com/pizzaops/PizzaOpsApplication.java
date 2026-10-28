package com.pizzaops;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class PizzaOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PizzaOpsApplication.class, args);
    }

    @Bean
    @Profile("test")
    PizzaOperations memoryServiceForUnitAndApiTests() {
        return new PizzaService();
    }

    @Bean
    @Profile("!test")
    PizzaOperations postgresService(JdbcTemplate jdbc, ObjectMapper mapper) {
        return new JdbcPizzaService(jdbc, mapper);
    }
}
