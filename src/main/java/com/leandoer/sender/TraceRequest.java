package com.leandoer.sender;

import com.leandoer.sender.annotation.PayloadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceRequest {
    private String value;
    private PayloadType type;
    private String application;
    private List<String> labels;
    private LocalDateTime generatedAt;
}
