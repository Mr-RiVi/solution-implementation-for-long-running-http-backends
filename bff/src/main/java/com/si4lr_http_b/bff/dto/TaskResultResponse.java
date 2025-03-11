package com.si4lr_http_b.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResultResponse {
    private String taskId;
    private String status;
    private Integer result;
    private String message;
}
