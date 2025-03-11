package com.si4lr_http_b.bff.controller;
import com.si4lr_http_b.bff.model.Area;
import com.si4lr_http_b.bff.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskStatusController {
    private final AreaRepository areaRepository;

    /**
     * Endpoint to check the status of a task.
     *
     * @param taskId The task ID.
     * @return A response containing the task ID and status.
     */
    @GetMapping("/task/{taskId}/status")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        Optional<Area> area = areaRepository.findByTaskId(taskId);

        if (area.isPresent()) {
            // Return the task ID and status
            Map<String, String> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("status", area.get().getStatus());
            return ResponseEntity.ok(response);
        } else {
            // Return an error if the task is not found
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("taskId", taskId);
            errorResponse.put("error", "Task not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
