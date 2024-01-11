package com.example.springamqpchanneltimeoutdemo;

import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class SpringAmqpChannelTimeoutDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAmqpChannelTimeoutDemoApplication.class, args);
	}

}
