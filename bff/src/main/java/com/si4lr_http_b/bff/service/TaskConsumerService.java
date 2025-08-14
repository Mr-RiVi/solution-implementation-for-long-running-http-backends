package com.si4lr_http_b.bff.service;

import com.si4lr_http_b.bff.config.MessagingConfig;
import com.si4lr_http_b.bff.controller.WebSocketController;
import com.si4lr_http_b.bff.event.TaskPlacedEvent;
import com.si4lr_http_b.bff.model.Area;
import com.si4lr_http_b.bff.repository.AreaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TaskConsumerService {
    private final AreaRepository areaRepository;
    private final WebSocketController webSocketController;
    private final RedisTemplate<String, Integer> redisTemplate;

    public TaskConsumerService(AreaRepository areaRepository, WebSocketController webSocketController, RedisTemplate<String, Integer> redisTemplate) {
        this.areaRepository = areaRepository;
        this.webSocketController = webSocketController;
        this.redisTemplate = redisTemplate;
    }


    @RabbitListener(queues = MessagingConfig.QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void consumeMessageFromQueue(TaskPlacedEvent taskPlacedEvent) {
        System.out.println("Message received from queue is: " + taskPlacedEvent);

        String taskId = taskPlacedEvent.getTaskId();
        int height = taskPlacedEvent.getHeight();
        int width = taskPlacedEvent.getWidth();

        // Generate a unique cache key based on task parameters
        String cacheKey = generateCacheKey(height, width);

        try {
            // Check if the result is already cached in Redis
            Integer cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                handleCacheHit(taskId, height, width, cachedResult);
                return; // Skip processing
            }

            // Process the task if not cached
            processTask(taskId, height, width, cacheKey);

        } catch (Exception e) {
            System.err.println("Task " + taskId + " failed: " + e.getMessage());

            // Save/update FAILED status only once
            Area area = areaRepository.findByTaskId(taskId)
                    .orElseGet(() -> createAreaEntity(taskId, height, width, 0, "FAILED"));
            area.setStatus("FAILED");
            areaRepository.save(area);

            // Rethrow to trigger retry & DLQ if retries exhausted
            throw new RuntimeException("Processing failed for task " + taskId, e);
        }
    }

    /**
     * Handles the scenario when the result is found in the cache.
     *
     * @param taskId       The task ID.
     * @param height       The height parameter.
     * @param width        The width parameter.
     * @param cachedResult The cached result.
     */
    private void handleCacheHit(String taskId, int height, int width, int cachedResult) {
        System.out.println("Cache hit! Returning cached result for Task ID: " + taskId);

        // Update the database with the cached result
        Area cachedArea = createAreaEntity(taskId, height, width, cachedResult, "COMPLETED");
        areaRepository.save(cachedArea);

        // Send WebSocket update
        webSocketController.sendUpdate(taskId, "COMPLETED");
    }

    /**
     * Processes the task if the result is not found in the cache.
     *
     * @param taskId   The task ID.
     * @param height   The height parameter.
     * @param width    The width parameter.
     * @param cacheKey The cache key.
     */
    private void processTask(String taskId, int height, int width, String cacheKey) throws Exception {
        // Task validator
        validateTask(height, width);

        // Update initial task status in the database (e.g., IN_PROGRESS)
        Area area = createAreaEntity(taskId, height, width, null, "IN_PROGRESS");
        areaRepository.save(area);

        // Calculate the area (this now includes the simulated delay)
        int result = calculateArea(height, width);
        area.setArea(result);
        area.setStatus("COMPLETED");
        // Update database with the result
        areaRepository.save(area);

        // Store result in Redis Cache
        cacheResult(cacheKey, result);

        System.out.println("Task " + taskId + " completed. Result: " + result);

        // Send WebSocket update
        webSocketController.sendUpdate(taskId, "COMPLETED");
    }

    /**
     * Calculates the area based on height and width, simulating a long-running calculation.
     *
     * @param height The height parameter.
     * @param width  The width parameter.
     * @return The calculated area.
     * @throws InterruptedException If the thread is interrupted during the delay.
     */
    private int calculateArea(int height, int width) throws InterruptedException {
        // Simulate a long-running calculation (20 seconds)
        Thread.sleep(20000); // 20-second delay

        // Perform the actual calculation
        return height * width;
    }

    /**
     * Caches the result in Redis with a TTL of 1 hour.
     *
     * @param cacheKey The cache key.
     * @param result   The result to cache.
     */
    private void cacheResult(String cacheKey, int result) {
        redisTemplate.opsForValue().set(cacheKey, result, 1, TimeUnit.HOURS);
    }

    private void validateTask(int height, int width) {
        if (height == 0) {
            throw new RuntimeException("Simulated failure");
        }

        // Other validation rules here
    }

    /**
     * Creates an Area entity with the given parameters.
     *
     * @param taskId The task ID.
     * @param height The height parameter.
     * @param width  The width parameter.
     * @param area   The calculated area.
     * @param status The task status.
     * @return The created Area entity.
     */
    private Area createAreaEntity(String taskId, int height, int width, Integer area, String status) {
        Area areaEntity = new Area();
        areaEntity.setTaskId(taskId);
        areaEntity.setHeight(height);
        areaEntity.setWidth(width);
        areaEntity.setArea(area);
        areaEntity.setStatus(status);
        return areaEntity;
    }

    /**
     * Generates a unique cache key based on task parameters.
     *
     * @param height The height parameter.
     * @param width  The width parameter.
     * @return A unique cache key.
     */
    private String generateCacheKey(int height, int width) {
        return "task_result:height=" + height + "&width=" + width;
    }
}
