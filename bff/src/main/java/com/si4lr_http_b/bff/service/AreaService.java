package com.si4lr_http_b.bff.service;

import com.si4lr_http_b.bff.config.MessagingConfig;
import com.si4lr_http_b.bff.dto.AreaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AreaService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    public String calculate_area(AreaRequest areaRequest){
        String taskId = UUID.randomUUID().toString();
        areaRequest.setTaskId(taskId);

        /*
          send user sent request to messaging queue(areaRequest - which include taskId, height, width(request body))
          then consumer can read it after some point and process the calculation.
        */
        rabbitTemplate.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.ROUTING_KEY, areaRequest);

        return taskId;
    }
}
