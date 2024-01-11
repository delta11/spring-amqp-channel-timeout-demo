package com.example.springamqpchanneltimeoutdemo;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@RabbitListener(
        queues = "demo.queue",
        concurrency = "1-3"
)
@Component
public class DemoListener {
    private static final Logger log = Logger.getLogger(DemoListener.class.getName());
    @RabbitHandler(isDefault = true)
    public void process(Message message) throws InterruptedException {
        log.log(Level.FINEST, message.toString());
        Thread.sleep(100);
    }
}
