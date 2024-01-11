package com.example.springamqpchanneltimeoutdemo;

import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.util.ErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Configuration
@EnableRabbit
public class RabbitMQConfiguration {
    @Bean
    public ConnectionFactory connectionFactory() {
        return getCachingConnectionFactory(2);
    }

    private CachingConnectionFactory getCachingConnectionFactory(int channelCacheSize) {
        com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        cachingConnectionFactory.setAddresses("localhost");
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setChannelCacheSize(channelCacheSize);
        cachingConnectionFactory.setChannelCheckoutTimeout(100);
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        cachingConnectionFactory.setPublisherReturns(true);
        return cachingConnectionFactory;
    }

    @Bean
    MessageConverter messageConverter() {
        return new SimpleMessageConverter();
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler();
    }

    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer> listenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            ErrorHandler errorHandler) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(100);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        factory.setMissingQueuesFatal(false);

        factory.setDefaultRequeueRejected(false);
        factory.setMessageConverter(messageConverter);
        factory.setConsumerTagStrategy(queue -> queue + " " + UUID.randomUUID());
        factory.setErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        CachingConnectionFactory conn = getCachingConnectionFactory(2);
        return new RabbitTemplate(conn);
    }

    @Bean
    public Queue gueue() {
        return new Queue("demo.queue");
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager() {
        return new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) throws TransactionException {

            }

            @Override
            public void rollback(TransactionStatus status) throws TransactionException {

            }
        };
    }



    @Bean
    public ApplicationRunner runner() {
        var conn = getCachingConnectionFactory(100);
        var template = new RabbitTemplate(conn);
        return args -> {
            while (true) {
                try {
                    var props = new MessageProperties();
                    props.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
                    props.setContentEncoding("UTF-8");
                    template.convertAndSend("demo.queue", new Message("foo".getBytes(StandardCharsets.UTF_8), props));
                } catch (AmqpTimeoutException e) {}
            }
        };
    }
}
