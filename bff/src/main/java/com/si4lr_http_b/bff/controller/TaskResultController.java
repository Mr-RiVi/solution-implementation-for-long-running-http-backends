package com.si4lr_http_b.bff.controller;

import com.si4lr_http_b.bff.dto.TaskResultResponse;
import com.si4lr_http_b.bff.model.Area;
import com.si4lr_http_b.bff.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TaskResultController {
    private final AreaRepository areaRepository;

    /**
     * Endpoint to retrieve the result of a task.
     *
     * @param taskId The task ID.
     * @return The result of the task if completed, or the status if still in progress or failed.
     */
    @GetMapping("/task/{taskId}/result")
    public ResponseEntity<TaskResultResponse> getTaskResult(@PathVariable String taskId) {
        Optional<Area> area = areaRepository.findByTaskId(taskId);

        if (area.isPresent()) {
            String status = area.get().getStatus();

            return switch (status) {
                case "COMPLETED" -> {
                    TaskResultResponse completedResponse = new TaskResultResponse(taskId, status, area.get().getArea(), "Task processing completed.");
                    yield ResponseEntity.ok(completedResponse);
                    // Return the result if the task is completed
                }
                case "FAILED" -> {
                    TaskResultResponse failedResponse = new TaskResultResponse(taskId, status, null, "Task processing failed.");
                    yield ResponseEntity.ok(failedResponse);
                    // Return the task ID and status if the task failed
                }
                default -> {
                    TaskResultResponse inProgressResponse = new TaskResultResponse(taskId, status, null, "Task is still in progress.");
                    yield ResponseEntity.ok(inProgressResponse);
                    // Return the task ID and status if the task is still in progress
                }
            };
        } else {
            // Return an error if the task is not found
            TaskResultResponse errorResponse = new TaskResultResponse(taskId, null, null, "Task not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}