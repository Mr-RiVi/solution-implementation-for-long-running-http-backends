package com.si4lr_http_b.bff.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

@Configuration
public class MessagingConfig {
    public static final String QUEUE = "long_running_task_queue";
    public static final String EXCHANGE = "long_running_task_exchange";
    public static final String ROUTING_KEY = "long_running_task_routingkey";

    //Creating a queue.
    @Bean
    public Queue queue(){
        return new Queue(QUEUE);
    }

    //Creating the Exchange.
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE);
    }

    //Binding queue and exchange using routing key.
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
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
    public AmqpTemplate template(ConnectionFactory connectionFactory){
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setMessageConverter(converter());
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);  // Prefetch count is set to 1 to ensure only 1 message is consumed at a time
        return factory;
    }
}
