package com.si4lr_http_b.bff.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskPlacedEvent {
    private String taskId;
    private Integer height;
    private Integer width;
}
