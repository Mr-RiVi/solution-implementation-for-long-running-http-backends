package com.si4lr_http_b.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaRequest {
    private String taskId;
    private Integer height;
    private Integer width;
}
