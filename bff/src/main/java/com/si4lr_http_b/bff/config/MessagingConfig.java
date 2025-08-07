package com.si4lr_http_b.bff.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class MessagingConfig {
    public static final String QUEUE = "long.running.task.queue"; // Main queue name
    public static final String DLQ = "long.running.task.queue.dlq"; // Dead Letter Queue name
    public static final String EXCHANGE = "long.running.task.exchange"; // Main Exchange name
    public static final String DL_EXCHANGE = "long.running.task.dl.exchange"; // Main Exchange name
    public static final String ROUTING_KEY = "long.running.task.routingKey"; // Routing key for main queue
    public static final String DLQ_ROUTING_KEY = "long.running.task.routingKey.dlq"; // Routing key for DLQ

    //Creating the queues.
    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    //Creating the Exchanges.
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DL_EXCHANGE);
    }

    // Binds the queue to the exchange using a routing key.
    @Bean
    public Binding taskQueueBinding() {
        return BindingBuilder.bind(taskQueue()).to(exchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterQueueBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ_ROUTING_KEY);
    }

    /**
     * Defines a MessageConverter bean that converts Java objects into JSON format when sending messages
     * and convert JSON back into Java objects when receiving messages.
     */
    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures the RabbitTemplate bean that is used for sending and receiving messages
     * from RabbitMQ. The RabbitTemplate simplifies interactions with RabbitMQ by providing
     * a high-level API(provides methods for sending and receiving messages from RabbitMQ).
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        // Enable retry
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder
                .stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer()) // Send to DLQ
                .build();
        factory.setAdviceChain(retryInterceptor); // THIS is what makes retry work
        factory.setDefaultRequeueRejected(false); // Prevent infinite retry loops

        factory.setMessageConverter(converter());
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);  // Prefetch count is set to 1 to ensure only 1 message is consumed at a time
        return factory;
    }
}
