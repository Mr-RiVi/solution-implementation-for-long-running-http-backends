package com.si4lr_http_b.bff.controller;

import com.si4lr_http_b.bff.dto.AreaRequest;
import com.si4lr_http_b.bff.service.TaskProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {
    private final TaskProducerService taskProducerService;
    @PostMapping("/start")
    @ResponseStatus(HttpStatus.ACCEPTED) // Immediately send the ACCEPTED status
    public Map<String, String> calculateArea(@RequestBody AreaRequest areaRequest){
        String taskId = taskProducerService.taskProcessor(areaRequest); // Async task

        // Respond to the client with taskId
        Map<String, String> response = new HashMap<>();
        response.put("status", "accepted");
        response.put("taskId", taskId);

        return response;
    }
}
