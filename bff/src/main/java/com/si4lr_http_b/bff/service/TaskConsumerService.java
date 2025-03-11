package com.si4lr_http_b.bff.service;

import com.si4lr_http_b.bff.config.MessagingConfig;
import com.si4lr_http_b.bff.controller.WebSocketController;
import com.si4lr_http_b.bff.dto.AreaRequest;
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
    public void consumeMessageFromQueue(AreaRequest areaRequest) {
        System.out.println("Message received from queue is: " + areaRequest);

        String taskId = areaRequest.getTaskId();
        int height = areaRequest.getHeight();
        int width = areaRequest.getWidth();

        // Generate a unique cache key based on task parameters
        String cacheKey = generateCacheKey(height, width);

        // Check if the result is already cached in Redis
        Integer cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            handleCacheHit(taskId, height, width, cachedResult);
            return; // Skip processing
        }

        // Process the task if not cached
        processTask(taskId, height, width, cacheKey);
    }

    /**
     * Handles the scenario when the result is found in the cache.
     *
     * @param taskId      The task ID.
     * @param height      The height parameter.
     * @param width       The width parameter.
     * @param cachedResult The cached result.
     */
    private void handleCacheHit(String taskId, int height, int width, int cachedResult) {
        System.out.println("Cache hit! Returning cached result for Task ID: " + taskId);

        // Update the database with the cached result
        Area cachedArea = createAreaEntity(taskId, height, width, cachedResult, "COMPLETED");
        areaRepository.save(cachedArea);

        // Send WebSocket update
        webSocketController.sendUpdate(taskId);
    }

    /**
     * Processes the task if the result is not found in the cache.
     *
     * @param taskId   The task ID.
     * @param height   The height parameter.
     * @param width    The width parameter.
     * @param cacheKey The cache key.
     */
    private void processTask(String taskId, int height, int width, String cacheKey) {
        try {
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
            webSocketController.sendUpdate(taskId);

        } catch (InterruptedException e) {
            handleTaskFailure(taskId, height, width, e);
        }
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

    /**
     * Handles task failure by updating the database and logging the error.
     *
     * @param taskId The task ID.
     * @param height The height parameter.
     * @param width  The width parameter.
     * @param e      The exception that caused the failure.
     */
    private void handleTaskFailure(String taskId, int height, int width, InterruptedException e) {
        e.printStackTrace();

        // Update the status to FAILED
        Area failedArea = createAreaEntity(taskId, height, width, 0, "FAILED");
        areaRepository.save(failedArea);

        System.out.println("Task " + taskId + " failed.");
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
